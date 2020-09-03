package com.eye.cool.permission

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import com.eye.cool.permission.checker.Request
import com.eye.cool.permission.checker.Result
import com.eye.cool.permission.support.Permission
import com.eye.cool.permission.support.PermissionUtil
import kotlinx.coroutines.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by ycb on 2020/9/2
 */
class PermissionChecker(
    private val request: Request
) {

  private val compactContext = request.context

  fun check(callback: Continuation<Result>) {
    if (request.permissions.isNullOrEmpty()) {
      callback.resume(Result(request = request.permissions))
      return
    }
    check(CoroutineScope(callback.context)) {
      callback.resume(it)
    }
  }

  fun check(scope: CoroutineScope, callback: (Result) -> Unit) {
    scope.launch {
      compactContext.proxyContext()
      checker(this) {
        compactContext.release()
        callback.invoke(Result(request.permissions, it?.toList()))
      }
    }
  }

  /**
   * If targetApi or SDK is less than 23,
   * support checks for [camera | recorder | storage]'s permissions,
   * and returns true for all other permissions
   */
  private suspend fun checker(scope: CoroutineScope, callback: (Array<String>?) -> Unit) {
    val target = compactContext.context().applicationInfo.targetSdkVersion
    if (target >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      withContext(Dispatchers.Main) {
        requestPermission(callback)
      }
    } else {
      val deniedPermissions = requestPermissionBelow23(scope).toTypedArray()
      if (deniedPermissions.isNotEmpty() && request.showRationaleSettingWhenDenied) {
        withContext(Dispatchers.Main) {
          request.rationaleSetting.showRationale(compactContext.context(), deniedPermissions) {
            if (it) {
              compactContext.startSettingForResult(deniedPermissions) { result ->
                callback.invoke(result)
              }
            } else {
              callback.invoke(deniedPermissions)
            }
          }
        }
      } else {
        callback.invoke(deniedPermissions)
      }
    }
  }

  private suspend fun requestPermissionBelow23(
      scope: CoroutineScope
  ) = suspendCoroutine<List<String>> {
    scope.launch(Dispatchers.IO) {
      val deniedPermissions = arrayListOf<String>()
      request.permissions.forEach { permission ->
        val available = when (permission) {
          in Permission.CAMERA -> {
            PermissionUtil.isCameraAvailable()
          }

          in Permission.STORAGE -> {
            PermissionUtil.isCacheDirAvailable(compactContext.context())
                && PermissionUtil.isExternalDirAvailable()
          }

          in Permission.MICROPHONE -> {
            PermissionUtil.isRecordAvailable()
          }

          else -> true //fixme Other permission's checking
        }
        if (!available) {
          deniedPermissions.add(permission)
        }
      }
      it.resume(deniedPermissions)
    }
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun requestPermission(callback: (Array<String>?) -> Unit) {
    val deniedPermissions = PermissionUtil.getDeniedPermissions(
        compactContext.context(),
        request.permissions.toTypedArray()
    )
    when {
      deniedPermissions.isEmpty() -> {
        callback.invoke(null)
      }
      else -> filterPermissions(callback, deniedPermissions)
    }
  }

  private fun filterPermissions(callback: (Array<String>?) -> Unit, permissions: Array<String>) {
    requestInstallPackage(permissions) { filtered ->
      when {
        filtered.isNullOrEmpty() -> {
          callback.invoke(null)
        }

        PermissionUtil.hasInstallPermissionOnly(permissions) -> {
          callback.invoke(filtered)
        }

        request.showRationaleWhenRequest -> {
          request.rationale.showRationale(compactContext.context(), filtered) {
            if (it) {
              compactContext.requestPermission(filtered) { requestPermissions, grantResults ->
                verifyPermissions(callback, requestPermissions, grantResults)
              }
            } else {
              callback.invoke(filtered)
            }
          }
        }

        else -> {
          compactContext.requestPermission(filtered) { requestPermissions, grantResults ->
            verifyPermissions(callback, requestPermissions, grantResults)
          }
        }
      }
    }
  }

  private fun verifyPermissions(
      callback: (Array<String>?) -> Unit,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    // Verify that each required permissions has been granted, otherwise all granted
    val deniedPermissions = arrayListOf<String>()
    grantResults.forEachIndexed { index, result ->
      if (result != PackageManager.PERMISSION_GRANTED) {
        deniedPermissions.add(permissions[index])
      }
    }

    val deniedArray = deniedPermissions.toTypedArray()

    when {
      deniedArray.isEmpty() -> {
        callback.invoke(null)
      }

      request.showRationaleSettingWhenDenied && hasAlwaysDeniedPermission(deniedArray) -> {
        request.rationaleSetting.showRationale(compactContext.context(), deniedArray) {
          if (it) {
            compactContext.startSettingForResult(deniedArray) { result ->
              if (result.isNullOrEmpty()) {
                callback.invoke(null)
              } else {
                callback.invoke(result)
              }
            }
          } else {
            callback.invoke(deniedArray)
          }
        }
      }

      else -> {
        callback.invoke(deniedArray)
      }
    }
  }

  private fun requestInstallPackage(
      permissions: Array<String>,
      callback: (Array<String>) -> Unit
  ) {

    when {
      !permissions.contains(Manifest.permission.REQUEST_INSTALL_PACKAGES)
          && !permissions.contains(Manifest.permission.INSTALL_PACKAGES) -> {
        callback.invoke(permissions)
      }

      Build.VERSION.SDK_INT < Build.VERSION_CODES.O
          || compactContext.context().packageManager.canRequestPackageInstalls() -> {
        val temp = permissions.toMutableList()
        temp.removeAll(Permission.INSTALL_PACKAGE)
        callback.invoke(temp.toTypedArray())
      }

      request.showInstallRationaleWhenRequest -> {
        request.rationaleInstallPackagesSetting.showRationale(compactContext.context(), permissions) {
          if (it) {
            compactContext.requestInstallPackage { result ->
              if (result) {
                val temp = permissions.toMutableList()
                temp.removeAll(Permission.INSTALL_PACKAGE)
                callback.invoke(temp.toTypedArray())
              } else {
                callback.invoke(permissions)
              }
            }
          } else {
            callback.invoke(permissions)
          }
        }
      }

      else -> {
        compactContext.requestInstallPackage { result ->
          if (result) {
            val temp = permissions.toMutableList()
            temp.removeAll(Permission.INSTALL_PACKAGE)
            callback.invoke(temp.toTypedArray())
          } else {
            callback.invoke(permissions)
          }
        }
      }
    }
  }

  /**
   * Has always been denied permissions.
   */
  private fun hasAlwaysDeniedPermission(deniedPermissions: Array<String>): Boolean {
    for (permission in deniedPermissions) {
      if (!PermissionUtil.isNeedShowRationalePermission(compactContext.context(), permission)) {
        return true
      }
    }
    return false
  }
}