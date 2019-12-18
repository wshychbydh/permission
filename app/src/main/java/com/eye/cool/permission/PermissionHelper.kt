package com.eye.cool.permission

import android.Manifest.permission.INSTALL_PACKAGES
import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.fragment.app.Fragment
import com.eye.cool.permission.rationale.DefaultRationale
import com.eye.cool.permission.rationale.InstallPackagesSettingRationale
import com.eye.cool.permission.rationale.Rationale
import com.eye.cool.permission.rationale.SettingRationale
import com.eye.cool.permission.support.CompatContext
import com.eye.cool.permission.support.Permission
import com.eye.cool.permission.support.PermissionUtil
import java.util.*

/**
 * The permissions for all requests must be declared in the manifest.
 * Created by cool on 2018/4/13.
 */
class PermissionHelper private constructor(private var context: CompatContext) {

  private lateinit var rationale: Rationale
  private lateinit var rationaleSetting: Rationale
  private lateinit var rationaleInstallPackagesSetting: Rationale
  private var callback: ((authorise: Boolean) -> Unit)? = null
  private lateinit var permissions: Array<String>
  private var showRationaleSettingWhenDenied = true
  private var showRationaleWhenRequest = false
  private var deniedPermissionCallback: ((Array<String>) -> Unit)? = null

  /**
   * If targetApi or SDK is less than 23,
   * support checks for [camera | recorder | storage]'s permissions,
   * and returns true for all other permissions
   */
  fun request() {
    if (permissions.isNullOrEmpty()) {
      callback?.invoke(true)
      return
    }
    val target = context.context().applicationInfo.targetSdkVersion
    if (target >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermission(context.context())
    } else {
      val deniedPermissions = requestPermissionBelow23()
      if (deniedPermissions.isNotEmpty() && showRationaleSettingWhenDenied) {
        rationaleSetting.showRationale(context.context(), deniedPermissions.toTypedArray(), null)
      } else {
        val hasDeniedPermissions = deniedPermissions.isNotEmpty()
        if (hasDeniedPermissions) {
          deniedPermissionCallback?.invoke(deniedPermissions.toTypedArray())
        }
        callback?.invoke(!hasDeniedPermissions)
      }
    }
  }

  private fun requestPermissionBelow23(): List<String> {
    val deniedPermissions = arrayListOf<String>()
    permissions.forEach {
      val available = when (it) {
        in Permission.CAMERA -> {
          PermissionUtil.isCameraAvailable()
        }
        in Permission.STORAGE -> {
          PermissionUtil.isCacheDirAvailable(context.context()) && PermissionUtil.isExternalDirAvailable()
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
    val deniedPermissions = PermissionUtil.getDeniedPermissions(context, permissions)
    when {
      deniedPermissions.isEmpty() -> {
        callback?.invoke(true)
      }
      showRationaleWhenRequest -> rationale.showRationale(context, deniedPermissions) {
        if (it) {
          requestPermission(deniedPermissions)
        } else {
          deniedPermissionCallback?.invoke(deniedPermissions)
          callback?.invoke(false)
        }
      }
      else -> requestPermission(deniedPermissions)
    }
  }

  private fun requestPermission(permissions: Array<String>) {
    val filtered = filterInstallPackagePermission(permissions)
    if (filtered.isNullOrEmpty()) {
      callback?.invoke(true)
      return
    }
    context.requestPermission(filtered) { requestPermissions, grantResults ->
      verifyPermissions(requestPermissions, grantResults)
    }
  }

  private fun filterInstallPackagePermission(permissions: Array<String>): Array<String> {
    val filtered = permissions.toMutableList()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val result = context.context().packageManager.canRequestPackageInstalls()
      if (result) {
        filtered.remove(INSTALL_PACKAGES)
        filtered.remove(REQUEST_INSTALL_PACKAGES)
      }
    }
    return filtered.toTypedArray()
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
      if (showRationaleSettingWhenDenied && hasAlwaysDeniedPermission(deniedArray)) {
        if (!requestInstallPackage(deniedArray)) {
          rationaleSetting.showRationale(context.context(), deniedArray) {
            if (it) {
              context.startSettingForResult(deniedArray) { permissions ->
                if (permissions.isNullOrEmpty()) {
                  callback?.invoke(true)
                } else {
                  callback?.invoke(false)
                  deniedPermissionCallback?.invoke(permissions)
                }
              }
            } else {
              callback?.invoke(false)
            }
          }
        }
      } else {
        deniedPermissionCallback?.invoke(deniedArray)
        callback?.invoke(false)
      }
    }
  }

  private fun requestInstallPackage(permissions: Array<String>): Boolean {

    if (permissions.size > 2) return false

    if (permissions.size == 2 && (permissions.contains(REQUEST_INSTALL_PACKAGES) && permissions.contains(INSTALL_PACKAGES))
        || (permissions.contains(REQUEST_INSTALL_PACKAGES) || permissions.contains(INSTALL_PACKAGES))
    ) {
      rationaleInstallPackagesSetting.showRationale(context.context(), permissions) {
        if (it) {
          context.requestInstallPackage(callback)
        } else {
          deniedPermissionCallback?.invoke(permissions)
          callback?.invoke(false)
        }
      }
      return true
    }
    return false
  }

  /**
   * Has always been denied permissions.
   */
  private fun hasAlwaysDeniedPermission(deniedPermissions: Array<String>): Boolean {
    for (permission in deniedPermissions) {
      if (!isNeedShowRationalePermission(context.context(), permission)) {
        return true
      }
    }
    return false
  }

  class Builder {

    constructor(context: Context) {
      this.context = CompatContext(context)
    }

    constructor(fragment: Fragment) {
      this.context = CompatContext(fragment)
    }

    constructor(activity: Activity) {
      this.context = CompatContext(activity)
    }

    private var context: CompatContext

    private var rationale: Rationale? = null
    private var rationaleSetting: Rationale? = null
    private var rationaleInstallPackagesSetting: Rationale? = null
    private var callback: ((authorise: Boolean) -> Unit)? = null
    private var permissions = LinkedHashSet<String>()
    private var showRationaleSettingWhenDenied = true
    private var showRationaleWhenRequest = false
    private var deniedPermissionCallback: ((Array<String>) -> Unit)? = null

    /**
     * @param permission Requested permission is required
     */
    fun permission(permission: String): Builder {
      permissions.add(permission)
      return this
    }

    /**
     * @param permissions Requested permissions are required
     */
    fun permissions(permissions: Array<String>): Builder {
      this.permissions.addAll(permissions)
      return this
    }

    /**
     * @param permissions Requested permissions are required
     */
    fun permissions(permissions: Collection<String>): Builder {
      this.permissions.addAll(permissions)
      return this
    }

    /**
     * @param callback Authorization result callback, true was granted all, false otherwise
     */
    fun permissionCallback(callback: ((authorise: Boolean) -> Unit)? = null): Builder {
      this.callback = callback
      return this
    }

    /**
     * The denied permission is returned through this callback
     * @param callback Returns permission to reject
     */
    fun deniedPermissionCallback(callback: ((Array<String>) -> Unit)? = null): Builder {
      this.deniedPermissionCallback = callback
      return this
    }

    /**
     * @param showRationaleWhenRequest Show Permission dialog when requesting, default false
     */
    fun showRationaleWhenRequest(showRationaleWhenRequest: Boolean): Builder {
      this.showRationaleWhenRequest = showRationaleWhenRequest
      return this
    }

    /**
     * @param rationale Dialog box that prompts the user for authorization
     */
    fun rationale(rationale: Rationale?): Builder {
      this.rationale = rationale
      return this
    }

    /**
     * @param showRationaleSettingWhenDenied Show Settings dialog when permission denied, default true
     */
    fun showRationaleSettingWhenDenied(showRationaleSettingWhenDenied: Boolean = true): Builder {
      this.showRationaleSettingWhenDenied = showRationaleSettingWhenDenied
      return this
    }

    /**
     * @param rationaleSetting The Settings dialog box that guides the user to authorize
     */
    fun rationaleSetting(rationaleSetting: Rationale?): Builder {
      this.rationaleSetting = rationaleSetting
      return this
    }

    /**
     * It will only pop up when you request the permission of 'android.Manifest.permission.REQUEST_INSTALL_PACKAGES'
     *
     * @param rationaleInstallPackagesSetting The Settings dialog box that guides the user to authorize
     */
    fun rationaleInstallPackagesSetting(rationaleInstallPackagesSetting: Rationale?): Builder {
      this.rationaleInstallPackagesSetting = rationaleInstallPackagesSetting
      return this
    }

    fun build(): PermissionHelper {
      val permissionHelper = PermissionHelper(context)
      permissionHelper.permissions = permissions.toTypedArray()
      permissionHelper.callback = callback
      permissionHelper.rationale = rationale ?: DefaultRationale()
      permissionHelper.rationaleSetting = rationaleSetting ?: SettingRationale()
      permissionHelper.rationaleInstallPackagesSetting = rationaleInstallPackagesSetting
          ?: InstallPackagesSettingRationale()
      permissionHelper.showRationaleSettingWhenDenied = showRationaleSettingWhenDenied
      permissionHelper.showRationaleWhenRequest = showRationaleWhenRequest
      permissionHelper.deniedPermissionCallback = deniedPermissionCallback
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
  }
}