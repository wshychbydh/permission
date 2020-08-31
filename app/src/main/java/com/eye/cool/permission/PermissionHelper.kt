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
import com.eye.cool.permission.support.PermissionUtil.isNeedShowRationalePermission
import java.util.*

/**
 * This class may start an activity allowing the user to choose which permissions
 * to grant and which to reject. Hence, you should be prepared that your activity
 * may be paused and resumed. Further, granting some permissions may require
 * a restart of you application. {@link Activity or Fragment requestPermissions()}.
 *
 * The permissions for all requests must be declared in the manifest.
 *
 * Created by cool on 2018/4/13.
 */
class PermissionHelper private constructor(private var context: CompatContext) {

  private lateinit var permissions: Array<String>

  private var rationale: Rationale = DefaultRationale()
  private var rationaleSetting: Rationale = SettingRationale()
  private var rationaleInstallPackagesSetting: Rationale = InstallPackagesSettingRationale()
  private var callback: ((authorise: Boolean) -> Unit)? = null
  private var showRationaleSettingWhenDenied = true
  private var showRationaleWhenRequest = false
  private var showInstallRationaleWhenRequest = false
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
      requestPermission()
    } else {
      val deniedPermissions = requestPermissionBelow23().toTypedArray()
      if (deniedPermissions.isNotEmpty() && showRationaleSettingWhenDenied) {
        rationaleSetting.showRationale(context.context(), deniedPermissions) {
          if (it) {
            context.startSettingForResult(deniedPermissions) { result ->
              if (!result.isNullOrEmpty()) {
                deniedPermissionCallback?.invoke(result)
              }
              callback?.invoke(result.isNullOrEmpty())
            }
          } else {
            deniedPermissionCallback?.invoke(deniedPermissions)
            callback?.invoke(false)
          }
        }
      } else {
        val hasDeniedPermissions = deniedPermissions.isNotEmpty()
        if (hasDeniedPermissions) {
          deniedPermissionCallback?.invoke(deniedPermissions)
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
          PermissionUtil.isCacheDirAvailable(context.context())
                  && PermissionUtil.isExternalDirAvailable()
        }

        in Permission.MICROPHONE -> {
          PermissionUtil.isRecordAvailable()
        }

        else -> true //fixme Other permission's checking
      }
      if (!available) {
        deniedPermissions.add(it)
      }
    }
    return deniedPermissions
  }

  @TargetApi(Build.VERSION_CODES.M)
  private fun requestPermission() {
    val deniedPermissions = PermissionUtil.getDeniedPermissions(context.context(), permissions)
    when {
      deniedPermissions.isEmpty() -> {
        callback?.invoke(true)
      }
      else -> filterPermissions(deniedPermissions)
    }
  }

  private fun filterPermissions(permissions: Array<String>) {
    requestInstallPackage(permissions) { filtered ->
      if (filtered.isNullOrEmpty()) {
        callback?.invoke(true)
      } else {
        if (showRationaleWhenRequest) {
          rationale.showRationale(context.context(), filtered) {
            if (it) {
              context.requestPermission(filtered) { requestPermissions, grantResults ->
                verifyPermissions(requestPermissions, grantResults)
              }
            } else {
              deniedPermissionCallback?.invoke(filtered)
              callback?.invoke(false)
            }
          }
        } else {
          context.requestPermission(filtered) { requestPermissions, grantResults ->
            verifyPermissions(requestPermissions, grantResults)
          }
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
      if (showRationaleSettingWhenDenied && hasAlwaysDeniedPermission(deniedArray)) {
        rationaleSetting.showRationale(context.context(), deniedArray) {
          if (it) {
            requestPermissionBySetting(deniedPermissions)
          } else {
            deniedPermissionCallback?.invoke(deniedArray)
            callback?.invoke(false)
          }
        }
      } else {
        deniedPermissionCallback?.invoke(deniedArray)
        callback?.invoke(false)
      }
    }
  }

  private fun requestPermissionBySetting(denied: ArrayList<String>) {
    if (denied.contains(REQUEST_INSTALL_PACKAGES) || denied.contains(INSTALL_PACKAGES)) {
      context.requestInstallPackage {
        if (it) {
          denied.remove(INSTALL_PACKAGES)
          denied.remove(REQUEST_INSTALL_PACKAGES)
        }
        context.startSettingForResult(denied.toTypedArray()) { result ->
          if (result.isNullOrEmpty()) {
            callback?.invoke(true)
          } else {
            deniedPermissionCallback?.invoke(result)
            callback?.invoke(false)
          }
        }
      }
    } else {
      context.startSettingForResult(denied.toTypedArray()) { result ->
        if (result.isNullOrEmpty()) {
          callback?.invoke(true)
        } else {
          deniedPermissionCallback?.invoke(result)
          callback?.invoke(false)
        }
      }
    }
  }

  private fun requestInstallPackage(
          permissions: Array<String>,
          callback: (Array<String>) -> Unit
  ) {

    if (!permissions.contains(REQUEST_INSTALL_PACKAGES)
            && !permissions.contains(INSTALL_PACKAGES)) {
      callback.invoke(permissions)
      return
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O
        || context.context().packageManager.canRequestPackageInstalls()
    ) {
      val temp = permissions.toMutableList()
      temp.remove(INSTALL_PACKAGES)
      temp.remove(REQUEST_INSTALL_PACKAGES)
      callback.invoke(temp.toTypedArray())
      return
    }

    if (showInstallRationaleWhenRequest) {
      rationaleInstallPackagesSetting.showRationale(context.context(), permissions) {
        if (it) {
          context.requestInstallPackage { result ->
            if (result) {
              val temp = permissions.toMutableList()
              temp.remove(INSTALL_PACKAGES)
              temp.remove(REQUEST_INSTALL_PACKAGES)
              callback.invoke(temp.toTypedArray())
            } else {
              callback.invoke(permissions)
            }
          }
        } else {
          callback.invoke(permissions)
        }
      }
    } else {
      context.requestInstallPackage { result ->
        if (result) {
          val temp = permissions.toMutableList()
          temp.remove(INSTALL_PACKAGES)
          temp.remove(REQUEST_INSTALL_PACKAGES)
          callback.invoke(temp.toTypedArray())
        } else {
          callback.invoke(permissions)
        }
      }
    }
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
      helper = PermissionHelper(CompatContext(context))
    }

    constructor(fragment: Fragment) {
      helper = PermissionHelper(CompatContext(fragment))
    }

    constructor(activity: Activity) {
      helper = PermissionHelper(CompatContext(activity))
    }

    private val helper: PermissionHelper

    private var permissions = LinkedHashSet<String>()

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
      helper.callback = callback
      return this
    }

    /**
     * The denied permission is returned through this callback
     * @param callback Returns permission to reject
     */
    fun deniedPermissionCallback(callback: ((Array<String>) -> Unit)? = null): Builder {
      helper.deniedPermissionCallback = callback
      return this
    }

    /**
     * @param showRationaleWhenRequest Show Permission dialog when requesting, default false
     */
    fun showRationaleWhenRequest(showRationaleWhenRequest: Boolean): Builder {
      helper.showRationaleWhenRequest = showRationaleWhenRequest
      return this
    }

    /**
     * @param showInstallRationaleWhenRequest Show Install Permission dialog when requesting, default false
     */
    fun showInstallRationaleWhenRequest(showInstallRationaleWhenRequest: Boolean): Builder {
      helper.showInstallRationaleWhenRequest = showInstallRationaleWhenRequest
      return this
    }

    /**
     * @param showRationaleSettingWhenDenied Show Settings dialog when permission denied, default true
     */
    fun showRationaleSettingWhenDenied(showRationaleSettingWhenDenied: Boolean = true): Builder {
      helper.showRationaleSettingWhenDenied = showRationaleSettingWhenDenied
      return this
    }

    /**
     * @param rationale The dialog that leads to user authorization
     */
    fun rationale(rationale: Rationale): Builder {
      helper.rationale = rationale
      return this
    }

    /**
     * @param rationaleSetting The Settings dialog that leads to user authorize
     */
    fun rationaleSetting(rationaleSetting: Rationale): Builder {
      helper.rationaleSetting = rationaleSetting
      return this
    }

    /**
     * It will only pop up when you request the permission of 'android.Manifest.permission.REQUEST_INSTALL_PACKAGES'
     *
     * @param rationaleInstallPackagesSetting The Settings dialog that leads to user authorize
     */
    fun rationaleInstallPackagesSetting(rationaleInstallPackagesSetting: Rationale): Builder {
      helper.rationaleInstallPackagesSetting = rationaleInstallPackagesSetting
      return this
    }

    fun build(): PermissionHelper {
      helper.permissions = permissions.toTypedArray()
      return helper
    }
  }
}