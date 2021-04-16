package com.eye.cool.permission

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eye.cool.permission.checker.Request
import com.eye.cool.permission.rationale.DefaultRationale
import com.eye.cool.permission.rationale.InstallPackageSettingRationale
import com.eye.cool.permission.rationale.Rationale
import com.eye.cool.permission.rationale.SettingRationale
import com.eye.cool.permission.support.CompatContext
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
@Deprecated("Use {@link PermissionChecker} instead.")
class PermissionHelper private constructor(private var context: CompatContext) {

  private lateinit var permissions: Array<String>

  private var rationale: Rationale = DefaultRationale()
  private var rationaleSetting: Rationale = SettingRationale()
  private var rationaleInstallPackagesSetting: Rationale = InstallPackageSettingRationale()
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
    val request = Request.Builder(context)
        .rationale(rationale)
        .permissions(permissions)
        .rationaleSetting(rationaleSetting)
        .rationaleInstallPackageSetting(rationaleInstallPackagesSetting)
        .showInstallRationaleWhenRequest(showInstallRationaleWhenRequest)
        .showRationaleWhenRequest(showRationaleWhenRequest)
        .showRationaleSettingWhenDenied(showRationaleSettingWhenDenied)
        .build()
    PermissionChecker(request).check {
      callback?.invoke(it.isSucceed())
      if (!it.denied.isNullOrEmpty()) {
        deniedPermissionCallback?.invoke(it.denied.toTypedArray())
      }
    }
  }

  class Builder {

    constructor(context: Context) {
      helper = PermissionHelper(CompatContext(context))
    }

    constructor(fragment: Fragment) {
      helper = PermissionHelper(CompatContext(fragment))
    }

    internal constructor(activity: FragmentActivity) {
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
     * callback run on ui-thread
     *
     * @param callback Authorization result callback, true was granted all, false otherwise
     */
    fun permissionCallback(callback: ((authorise: Boolean) -> Unit)? = null): Builder {
      helper.callback = callback
      return this
    }

    /**
     * call run on ui-thread
     *
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