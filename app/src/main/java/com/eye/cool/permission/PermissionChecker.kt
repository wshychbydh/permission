package com.eye.cool.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import com.eye.cool.permission.checker.Request
import com.eye.cool.permission.checker.Result
import com.eye.cool.permission.support.*
import com.eye.cool.permission.support.BuildVersion
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.*

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
    return if (BuildVersion.isTargetOverM(ctx.context()) && BuildVersion.isBuildOverM()) {
      tryFilterPermission(request.permissions)
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
        in PermissionGroup.CAMERA -> {
          registered && PermissionUtil.isCameraAvailable()
        }

        in PermissionGroup.STORAGE -> {
          registered && PermissionUtil.isCacheDirAvailable(ctx.context())
              && PermissionUtil.isExternalDirAvailable()
        }

        in PermissionGroup.MICROPHONE -> {
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

  private suspend fun tryFilterPermission(permissions: List<String>): Array<String>? {

    val filterGroup = PermissionUtil.filterPermissionGroup(permissions)

    val filterInstall = tryRequestInstallPackage(filterGroup)
    if (filterInstall.isNullOrEmpty()
        || PermissionUtil.hasInstallPermissionOnly(filterInstall)) {
      return filterInstall.toTypedArray()
    }

    val filterManageFile = tryRequestManageFile(filterInstall)
    if (filterManageFile.isNullOrEmpty()
        || PermissionUtil.hasManageFilePermissionOnly(filterManageFile)) {
      return filterManageFile.toTypedArray()
    }

    val filtered = PermissionUtil.getDeniedPermissions(
        ctx.context(),
        filterManageFile.toTypedArray()
    )

    if (filtered.isNullOrEmpty()) return null

    return if (request.showRationaleWhenRequest) {
      val result = request.rationale.request(request.scope, ctx.context(), filtered)
      if (result) doRequestPermission(filtered) else filtered
    } else {
      doRequestPermission(filtered)
    }
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

  private suspend fun tryRequestManageFile(permissions: ArrayList<String>): ArrayList<String> {

    val manageExternalStorage = Manifest.permission.MANAGE_EXTERNAL_STORAGE

    if (BuildVersion.isBuildBelowR()) {
      permissions.removeAll(arrayOf(manageExternalStorage))
    } else {

      val needReadStorage = permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
      val needWriteStorage = permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      val needMangeStorage = permissions.contains(manageExternalStorage)

      if (needReadStorage || needWriteStorage || needMangeStorage) {
        if (Environment.isExternalStorageManager()) {
          permissions.remove(manageExternalStorage)
          permissions.removeAll(PermissionGroup.STORAGE)
        } else if (needWriteStorage || needMangeStorage) {
          if (requestManageFile()) {
            permissions.remove(manageExternalStorage)
            permissions.removeAll(PermissionGroup.STORAGE)
          } else if (!needMangeStorage) {
            permissions.add(manageExternalStorage)
          }
        }
      }
    }

    return permissions
  }

  private suspend fun requestManageFile() = if (request.showManageFileRationaleWhenRequest) {
    val result = request.rationale.request(
        request.scope,
        ctx.context(),
        arrayOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    )
    if (result) ctx.requestAllFileAccess() else false
  } else {
    ctx.requestAllFileAccess()
  }

  private suspend fun tryRequestInstallPackage(permissions: ArrayList<String>): ArrayList<String> {
    return when {
      !permissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES)
          && !permissions.contains(Manifest.permission.INSTALL_PACKAGES) -> permissions

      Build.VERSION.SDK_INT < Build.VERSION_CODES.O
          || ctx.context().packageManager.canRequestPackageInstalls() -> {
        permissions.removeAll(PermissionGroup.INSTALL_PACKAGE)
        return permissions
      }

      request.showInstallRationaleWhenRequest -> {
        var allowed = request.rationaleInstallPackage.request(
            request.scope,
            ctx.context(),
            permissions.toTypedArray()
        )
        if (allowed && ctx.requestInstallPackage()) {
          permissions.removeAll(PermissionGroup.INSTALL_PACKAGE)
        }
        return permissions
      }

      else -> {
        if (ctx.requestInstallPackage()) {
          permissions.removeAll(PermissionGroup.INSTALL_PACKAGE)
        }
        return permissions
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