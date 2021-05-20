package com.eye.cool.permission.checker

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.fragment.app.Fragment
import com.eye.cool.permission.rationale.*
import com.eye.cool.permission.support.CompatContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 * Created by ycb on 2020/8/31
 */
class Request private constructor(
    internal val context: CompatContext,
    internal val permissions: List<String>,
    internal val rationale: RationaleDelegate,
    internal val rationaleSetting: RationaleDelegate,
    internal val rationaleInstallPackage: RationaleDelegate,
    internal val rationaleManageFile: RationaleDelegate,
    internal val showRationaleSettingWhenDenied: Boolean,
    internal val showRationaleWhenRequest: Boolean,
    internal val showInstallRationaleWhenRequest: Boolean,
    internal val showManageFileRationaleWhenRequest: Boolean
) {

  companion object {
    inline fun build(
        context: Context,
        block: Builder.() -> Unit
    ) = Builder(context).apply(block).build()

    inline fun build(
        fragment: Fragment,
        block: Builder.() -> Unit
    ) = Builder(fragment.requireContext()).apply(block).build()
  }

  internal var scope = MainScope()

  fun onDestroy() {
    scope.cancel()
    context.release()
  }

  data class Builder(
      val context: Context,
      var permissions: ArrayList<String> = arrayListOf(),
      var rationale: Rationale = DefaultRationale(),
      var rationaleSetting: Rationale = SettingRationale(),
      var rationaleInstallPackage: Rationale = InstallPackageRationale(),
      var rationaleManageFile: Rationale = ManageFileRationale(),
      var showRationaleSettingWhenDenied: Boolean = true,
      var showRationaleWhenRequest: Boolean = false,
      var showInstallRationaleWhenRequest: Boolean = false,
      var showManageFileRationaleWhenRequest: Boolean = false
  ) {

    /**
     * [permission] Requested permission is required
     */
    fun permission(permission: String) = apply { this.permissions.add(permission) }

    /**
     * [permissions] Requested permissions are required
     */
    fun permissions(permissions: Array<String>) = apply { this.permissions.addAll(permissions) }

    /**
     * [permissions] Requested permissions are required
     */
    fun permissions(permissions: Collection<String>) = apply { this.permissions.addAll(permissions) }


    /**
     * @see [rationale]
     *
     * [show] Show Permission dialog when requesting, default false
     */
    fun showRationaleWhenRequest(show: Boolean) = apply { this.showRationaleWhenRequest = show }

    /**
     * @see [rationaleInstallPackage]
     *
     * [show] Show install permission dialog when requesting, default false
     */
    fun showInstallRationaleWhenRequest(show: Boolean) = apply {
      this.showInstallRationaleWhenRequest = show
    }

    /**
     * @see [rationaleManageFile]
     *
     * [show] Show manage file permission dialog when requesting, default false
     */
    fun showManageFileRationaleWhenRequest(show: Boolean) = apply {
      this.showManageFileRationaleWhenRequest = show
    }

    /**
     * If true, it will be guided to the permission setting page. @see [rationaleSetting]
     *
     * [show] Show Settings dialog when permission denied, default true
     */
    fun showRationaleSettingWhenDenied(show: Boolean) = apply {
      this.showRationaleSettingWhenDenied = show
    }

    /**
     * [rationale] The dialog that leads to user authorization
     */
    fun rationale(rationale: Rationale) = apply {
      this.rationale = rationale
    }

    /**
     * [rationaleSetting] The Settings dialog that leads to user authorize
     */
    fun rationaleSetting(rationaleSetting: Rationale) = apply {
      this.rationaleSetting = rationaleSetting
    }

    /**
     * It will pop up when you request the permission of
     * 'android.Manifest.permission.REQUEST_INSTALL_PACKAGES'
     * @see [requestInstallPackages] or
     * declare storage permissions[Manifest.permission.REQUEST_INSTALL_PACKAGES] in [permission]
     *
     * [rationaleInstallPackage] The Settings dialog that leads to user authorize
     */
    fun rationaleInstallPackage(rationaleInstallPackage: Rationale) = apply {
      this.rationaleInstallPackage = rationaleInstallPackage
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
    fun rationaleManageFile(accessFile: Rationale) = apply {
      this.rationaleManageFile = accessFile
    }

    /**
     * You must register in manifest [Manifest.permission.MANAGE_EXTERNAL_STORAGE]
     *
     * If [Build.VERSION.SDK_INT] is exceeds [Build.VERSION_CODES.R],
     * will request manage external storage.
     */
    fun requestManageExternalStorage() = apply {
      permission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
    }

    /**
     * You must register in manifest [Manifest.permission.REQUEST_INSTALL_PACKAGES]
     *
     * If [Build.VERSION.SDK_INT] is exceeds [Build.VERSION_CODES.O],
     * will request install package.
     */
    fun requestInstallPackages() = apply {
      permission(Manifest.permission.REQUEST_INSTALL_PACKAGES)
    }

    fun build() = Request(
        context = CompatContext(context),
        permissions = permissions.distinct(),
        rationale = RationaleDelegate(rationale),
        rationaleSetting = RationaleDelegate(rationaleSetting),
        rationaleInstallPackage = RationaleDelegate(rationaleInstallPackage),
        rationaleManageFile = RationaleDelegate(rationaleManageFile),
        showRationaleSettingWhenDenied = showRationaleSettingWhenDenied,
        showRationaleWhenRequest = showRationaleWhenRequest,
        showInstallRationaleWhenRequest = showInstallRationaleWhenRequest,
        showManageFileRationaleWhenRequest = showManageFileRationaleWhenRequest
    )
  }
}