package com.eye.cool.permission

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.WorkerThread
import com.eye.cool.permission.checker.Request
import com.eye.cool.permission.checker.Result
import com.eye.cool.permission.support.Permission
import com.eye.cool.permission.support.PermissionUtil
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by ycb on 2020/9/2
 */
class PermissionChecker(
    private val request: Request
) {

  private val ctx = request.context

  /**
   * @param callback run on ui-thread
   */
  fun check(callback: CancellableContinuation<Result>) {
    request.scope.plus(callback.context)
    check(request.scope) {
      callback.complete(it)
    }
  }

  /**
   * @param callback run on ui-thread
   */
  fun check(callback: (Result) -> Unit) {
    check(request.scope, callback)
  }

  /**
   * @param scope
   * @param callback run on ui-thread
   */
  fun check(scope: CoroutineScope, callback: (Result) -> Unit) {
    scope.launch(Dispatchers.Default) {
      try {
        ctx.proxyContext()
        val denied = checker()
        val result = Result(request.permissions, denied?.toList())
        withContext(Dispatchers.Main) {
          callback.invoke(result)
        }
      } finally {
        request.onDestroy()
      }
    }
  }

  /**
   * @return run on ui-thread
   */
  suspend fun check(
      scope: CoroutineScope = request.scope
  ): Result = suspendCancellableCoroutine {
    scope.launch(Dispatchers.Default) {
      try {
        ctx.proxyContext()
        val denied = checker()
        withContext(Dispatchers.Main) {
          it.complete(Result(request.permissions, denied?.toList()))
        }
      } finally {
        request.onDestroy()
      }
    }
  }

  /**
   * If targetApi or SDK is less than 23,
   * support checks for [camera | recorder | storage]'s permissions,
   * and returns true for all other permissions
   */
  private suspend fun checker(): Array<String>? {
    if (request.permissions.isNullOrEmpty()) {
      return null
    }
    val target = ctx.context().applicationInfo.targetSdkVersion
    return if (target >= Build.VERSION_CODES.M
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermission()
    } else {
      val denied = requestPermissionBelow23().toTypedArray()
      if (denied.isNullOrEmpty()) return null
      if (request.showRationaleSettingWhenDenied) {
        val allowed = request.rationaleSetting.request(request.scope, ctx.context(), denied)
        if (allowed) return ctx.startSettingForResult(denied)
      }
      denied
    }
  }

  private fun requestPermissionBelow23(): List<String> {
    val deniedPermissions = arrayListOf<String>()
    val registeredPermissions = PermissionUtil.getRequestedPermissions(ctx.context())
    request.permissions.forEach { permission ->
      val registered = registeredPermissions.contains(permission)
      val available = when (permission) {
        in Permission.CAMERA -> {
          registered && PermissionUtil.isCameraAvailable()
        }

        in Permission.STORAGE -> {
          registered && PermissionUtil.isCacheDirAvailable(ctx.context())
              && PermissionUtil.isExternalDirAvailable()
        }

        in Permission.MICROPHONE -> {
          registered && PermissionUtil.isRecordAvailable()
        }

        else -> registered
      }
      if (!available) {
        deniedPermissions.add(permission)
      }
    }
    return deniedPermissions
  }

  @TargetApi(Build.VERSION_CODES.M)
  private suspend fun requestPermission(): Array<String>? {

    val filtered = tryRequestFileAccess(request.permissions)

    val denied = PermissionUtil.getDeniedPermissions(ctx.context(), filtered)

    return when {
      denied.isEmpty() -> null
      else -> filterPermissions(denied)
    }
  }

  private suspend fun filterPermissions(permissions: Array<String>): Array<String>? {

    val filtered = requestInstallPackage(permissions)

    return when {
      filtered.isNullOrEmpty() -> null

      PermissionUtil.hasInstallPermissionOnly(permissions) -> filtered

      request.showRationaleWhenRequest -> {
        val result = request.rationale.request(request.scope, ctx.context(), filtered)
        return if (result) doRequestPermission(filtered) else filtered
      }

      else -> return doRequestPermission(filtered)
    }
  }

  private suspend fun tryRequestFileAccess(permissions: ArrayList<String>): Array<String> {

    val fileAccessPermission = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    if (!permissions.contains(fileAccessPermission)) return permissions.toTypedArray()

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      permissions.removeAll(arrayOf(fileAccessPermission))
    } else {
      val allPermissions = PermissionUtil.getRequestedPermissions(ctx.context())
      if (allPermissions.contains(fileAccessPermission) && ctx.requestAllFileAccess()) {
        permissions.removeAll(arrayOf(fileAccessPermission))
      }
    }

    return permissions.toTypedArray()
  }

  private suspend fun doRequestPermission(permissions: Array<String>): Array<String>? {
    val grantResults = ctx.requestPermission(permissions)
    return verifyPermissions(permissions, grantResults)
  }

  private suspend fun verifyPermissions(
      permissions: Array<String>,
      grantResults: IntArray
  ): Array<String>? {
    // Verify that each required permissions has been granted, otherwise all granted
    val deniedPermissions = arrayListOf<String>()
    grantResults.forEachIndexed { index, result ->
      if (result != PackageManager.PERMISSION_GRANTED) {
        deniedPermissions.add(permissions[index])
      }
    }

    val deniedArray = deniedPermissions.toTypedArray()

    return when {
      deniedArray.isEmpty() -> null

      request.showRationaleSettingWhenDenied
          && hasAlwaysDeniedPermission(deniedArray) -> {
        val result = request.rationaleSetting.request(request.scope, ctx.context(), deniedArray)
        if (result) ctx.startSettingForResult(deniedArray) else deniedArray
      }

      else -> deniedArray
    }
  }

  private suspend fun requestInstallPackage(permissions: Array<String>): Array<String> {
    return when {
      !permissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES)
          && !permissions.contains(Manifest.permission.INSTALL_PACKAGES) -> permissions

      Build.VERSION.SDK_INT < Build.VERSION_CODES.O
          || ctx.context().packageManager.canRequestPackageInstalls() -> {
        val temp = permissions.toMutableList()
        temp.removeAll(Permission.INSTALL_PACKAGE)
        return temp.toTypedArray()
      }

      request.showInstallRationaleWhenRequest -> {
        var allowed = request.rationaleInstallPackageSetting.request(
            request.scope,
            ctx.context(),
            permissions
        )
        if (allowed && ctx.requestInstallPackage()) {
          val temp = permissions.toMutableList()
          temp.removeAll(Permission.INSTALL_PACKAGE)
          return temp.toTypedArray()
        }
        return permissions
      }

      else -> {
        return if (ctx.requestInstallPackage()) {
          val temp = permissions.toMutableList()
          temp.removeAll(Permission.INSTALL_PACKAGE)
          temp.toTypedArray()
        } else {
          permissions
        }
      }
    }
  }

  /**
   * Has always been denied permissions.
   */
  private fun hasAlwaysDeniedPermission(deniedPermissions: Array<String>): Boolean {
    for (permission in deniedPermissions) {
      if (!PermissionUtil.isNeedShowRationalePermission(ctx.context(), permission)) {
        return true
      }
    }
    return false
  }
}