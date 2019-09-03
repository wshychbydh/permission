package com.eye.cool.permission

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.util.*

/**
 * The permissions for all requests must be declared in the manifest.
 * Created by cool on 2018/4/13.
 */
class PermissionHelper private constructor(private var context: Context) {

  private var rationale: Rationale? = null
  private var rationaleSetting: Rationale? = null
  private var callback: ((authorise: Boolean) -> Unit)? = null
  private var permissions: Array<String>? = null
  private var showSettingWhenDenied = true

  /**
   * If targetApi or SDK is less than 23,
   * support checks for [camera | recorder | storage]'s permissions,
   * and returns true for all other permissions
   */
  fun request() {
    if (permissions == null || permissions!!.isEmpty()) {
      callback?.invoke(true)
      return
    }
    val target = context.applicationInfo.targetSdkVersion
    if (target >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermission(context)
    } else {
      val deniedPermissions = requestPermissionBelow23()
      if (deniedPermissions.isNotEmpty() && showSettingWhenDenied) {
        rationaleSetting?.showRationale(context, deniedPermissions.toTypedArray(), null)
      } else callback?.invoke(deniedPermissions.isEmpty())
    }
  }

  private fun requestPermissionBelow23(): List<String> {
    val deniedPermissions = arrayListOf<String>()
    permissions?.forEach {
      val available = when (it) {
        in Permission.CAMERA -> {
          PermissionUtil.isCameraAvailable()
        }
        in Permission.STORAGE -> {
          PermissionUtil.isCacheDirAvailable(context) && PermissionUtil.isExternalDirAvailable()
        }

        in Permission.MICROPHONE -> {
          PermissionUtil.isRecordAvailable()
        }
        else -> {
          //fixme Other permission's checking
          true
        }
      }
      if (!available) {
        deniedPermissions.add(it)
      }
    }
    return deniedPermissions
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun requestPermission(context: Context) {
    val checkPermission = getRequestPermission(context, permissions)
    if (checkPermission.isEmpty()) {
      callback?.invoke(true)
    } else {
      var showRationale = false
      checkPermission.forEach {
        showRationale = showRationale || isNeedShowRationalePermission(context, it)
      }
      if (showRationale) {
        rationale?.showRationale(context, checkPermission) {
          if (it) {
            PermissionActivity.requestPermission(
                context,
                permissions!!
            ) { requestPermissions, grantResults ->
              verifyPermissions(requestPermissions, grantResults)
            }
          } else {
            callback?.invoke(false)
          }
        }

      } else {
        PermissionActivity.requestPermission(context, permissions!!) { requestPermissions, grantResults ->
          verifyPermissions(requestPermissions, grantResults)
        }
      }
    }
  }


  private fun verifyPermissions(permissions: Array<String>, grantResults: IntArray) {
    // Verify that each required permissions has been granted, otherwise all granted
    val deniedPermissions = arrayListOf<String>()
    grantResults.forEachIndexed { index, result ->
      if (result != PackageManager.PERMISSION_GRANTED) {
        deniedPermissions.add(permissions[index])
      }
    }

    if (deniedPermissions.isNullOrEmpty()) {
      callback?.invoke(true)
    } else {
      val deniedArray = deniedPermissions.toTypedArray()
      if (showSettingWhenDenied && hasAlwaysDeniedPermission(deniedArray)) {
        rationaleSetting?.showRationale(context, deniedArray, null)
      } else {
        callback?.invoke(false)
      }
    }
  }

  /**
   * Has always been denied permissions.
   */
  private fun hasAlwaysDeniedPermission(deniedPermissions: Array<String>): Boolean {
    for (permission in deniedPermissions) {
      if (!isNeedShowRationalePermission(context, permission)) {
        return true
      }
    }
    return false
  }

  class Builder(private var context: Context) {
    private var rationale: Rationale? = null
    private var rationaleSetting: Rationale? = null
    private var callback: ((authorise: Boolean) -> Unit)? = null
    private var permissions = LinkedHashSet<String>()
    private var showSettingWhenDenied = true

    fun permission(permission: String): Builder {
      permissions.add(permission)
      return this
    }

    fun permissions(array: Array<String>): Builder {
      permissions.addAll(array)
      return this
    }

    fun showSettingWhenDenied(showSettingWhenDenied: Boolean): Builder {
      this.showSettingWhenDenied = showSettingWhenDenied
      return this
    }

    fun permissionCallback(callback: ((authorise: Boolean) -> Unit)? = null): Builder {
      this.callback = callback
      return this
    }

    fun rationale(rationale: Rationale?): Builder {
      this.rationale = rationale
      return this
    }

    fun rationaleSetting(rationaleSetting: Rationale?): Builder {
      this.rationaleSetting = rationaleSetting
      return this
    }

    fun build(): PermissionHelper {
      val permissionHelper = PermissionHelper(context)
      permissionHelper.permissions = permissions.toTypedArray()
      permissionHelper.callback = callback
      permissionHelper.rationale = rationale ?: DefaultRationale()
      permissionHelper.rationaleSetting = rationaleSetting ?: SettingRationale()
      permissionHelper.showSettingWhenDenied = showSettingWhenDenied
      return permissionHelper
    }
  }

  companion object {

    private fun isNeedShowRationalePermission(context: Context, permission: String): Boolean {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
      val packageManager = context.packageManager
      val pkManagerClass = packageManager.javaClass
      return try {
        val method = pkManagerClass.getMethod("shouldShowRequestPermissionRationale", String::class.java)
        if (!method.isAccessible) method.isAccessible = true
        method.invoke(packageManager, permission) as Boolean? ?: false
      } catch (ignored: Exception) {
        false
      }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun getRequestPermission(context: Context, permissions: Array<String>?): Array<String> {
      val requestList = mutableListOf<String>()
      permissions?.forEach {
        if (context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
          requestList.add(it)
        }
      }
      return requestList.toTypedArray()
    }
  }
}