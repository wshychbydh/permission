package com.eye.cool.permission.checker

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.eye.cool.permission.rationale.*
import com.eye.cool.permission.rationale.DefaultRationale
import com.eye.cool.permission.rationale.InstallPackageRationale
import com.eye.cool.permission.rationale.RationaleDelegate
import com.eye.cool.permission.rationale.SettingRationale
import com.eye.cool.permission.support.CompatContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * Created by ycb on 2020/8/31
 */
class Request private constructor(
    internal val context: CompatContext
) {

  internal var permissions = arrayListOf<String>()
  internal var rationale = RationaleDelegate(DefaultRationale())
  internal var rationaleSetting = RationaleDelegate(SettingRationale())
  internal var rationaleInstallPackage = RationaleDelegate(InstallPackageRationale())
  internal var rationaleManageFile = RationaleDelegate(ManageFileRationale())
  internal var showRationaleSettingWhenDenied = true
  internal var showRationaleWhenRequest = false
  internal var showInstallRationaleWhenRequest = false
  internal var showManageFileRationaleWhenRequest = false

  internal var scope = MainScope()

  fun onDestroy() {
    scope.cancel()
    context.release()
  }

  class Builder {

    private val request: Request

    constructor(context: Context) {
      request = Request(CompatContext(context))
    }

    constructor(fragment: Fragment) {
      request = Request(CompatContext(fragment))
    }

    internal constructor(context: CompatContext) {
      request = Request(context)
    }

    /**
     * [permission] Requested permission is required
     */
    fun permission(permission: String): Builder {
      request.permissions.add(permission)
      return this
    }

    /**
     * [permissions] Requested permissions are required
     */
    fun permissions(permissions: Array<String>): Builder {
      request.permissions.addAll(permissions)
      return this
    }

    /**
     * [permissions] Requested permissions are required
     */
    fun permissions(permissions: Collection<String>): Builder {
      request.permissions.addAll(permissions)
      return this
    }


    /**
     * @see [rationale]
     *
     * [show] Show Permission dialog when requesting, default false
     */
    fun showRationaleWhenRequest(show: Boolean): Builder {
      request.showRationaleWhenRequest = show
      return this
    }

    /**
     * @see [rationaleInstallPackage]
     *
     * [show] Show install permission dialog when requesting, default false
     */
    fun showInstallRationaleWhenRequest(show: Boolean): Builder {
      request.showInstallRationaleWhenRequest = show
      return this
    }

    /**
     * @see [rationaleManageFile]
     *
     * [show] Show manage file permission dialog when requesting, default false
     */
    fun showManageFileRationaleWhenRequest(show: Boolean): Builder {
      request.showManageFileRationaleWhenRequest = show
      return this
    }

    /**
     * If true, it will be guided to the permission setting page. @see [rationaleSetting]
     *
     * [show] Show Settings dialog when permission denied, default true
     */
    fun showRationaleSettingWhenDenied(show: Boolean): Builder {
      request.showRationaleSettingWhenDenied = show
      return this
    }

    /**
     * [rationale] The dialog that leads to user authorization
     */
    fun rationale(rationale: Rationale): Builder {
      request.rationale = RationaleDelegate(rationale)
      return this
    }

    /**
     * [rationaleSetting] The Settings dialog that leads to user authorize
     */
    fun rationaleSetting(rationaleSetting: Rationale): Builder {
      request.rationaleSetting = RationaleDelegate(rationaleSetting)
      return this
    }

    /**
     * It will pop up when you request the permission of
     * 'android.Manifest.permission.REQUEST_INSTALL_PACKAGES'
     * @see [requestInstallPackages] or
     * declare storage permissions[Manifest.permission.REQUEST_INSTALL_PACKAGES] in [permission]
     *
     * [rationaleInstallPackage] The Settings dialog that leads to user authorize
     */
    fun rationaleInstallPackage(rationaleInstallPackage: Rationale): Builder {
      request.rationaleInstallPackage = RationaleDelegate(rationaleInstallPackage)
      return this
    }

    /**
     * It will pop up when you request the permission of
     * 'android.Manifest.permission.MANAGE_EXTERNAL_STORAGE'
     * @see [requestManageExternalStorage] or
     * declare storage permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] or
     * [Manifest.permission.READ_EXTERNAL_STORAGE] or
     * [Manifest.permission.MANAGE_EXTERNAL_STORAGE] in [permission]
     *
     * [accessFile] The Settings dialog that leads to user authorize
     */
    fun rationaleManageFile(accessFile: Rationale): Builder {
      request.rationaleManageFile = RationaleDelegate(accessFile)
      return this
    }

    /**
     * You must register in manifest [Manifest.permission.MANAGE_EXTERNAL_STORAGE]
     *
     * If [Build.VERSION.SDK_INT] is exceeds [Build.VERSION_CODES.R],
     * will request manage external storage.
     */
    fun requestManageExternalStorage(): Builder {
      permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
      return this
    }

    /**
     * You must register in manifest [Manifest.permission.REQUEST_INSTALL_PACKAGES]
     *
     * If [Build.VERSION.SDK_INT] is exceeds [Build.VERSION_CODES.O],
     * will request install package.
     */
    fun requestInstallPackages(): Builder {
      permission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
      return this
    }

    fun build() = request
  }
}