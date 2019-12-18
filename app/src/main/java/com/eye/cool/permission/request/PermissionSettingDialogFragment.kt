package com.eye.cool.permission.request

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager
import com.eye.cool.permission.support.PermissionSetting
import com.eye.cool.permission.support.PermissionUtil

/**
 *Created by ycb on 2019/12/17 0017
 */
internal class PermissionSettingDialogFragment : AppCompatDialogFragment() {

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val requestInstallPackages = arguments?.getBoolean(REQUEST_INSTALL_PACKAGES, false) ?: false
    if (requestInstallPackages) {
      val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${requireContext().packageName}"))
      startActivityForResult(intent, REQUEST_INSTALL_PACKAGES_CODE)
    } else {
      PermissionSetting().start(this, REQUEST_SETTING_CODE)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_INSTALL_PACKAGES_CODE) {
      sRequestInstallPackageListener?.invoke(resultCode == Activity.RESULT_OK)
    } else {
      val permissions = arguments?.getStringArray(PERMISSIONS)
      if (permissions.isNullOrEmpty()) {
        sDeniedPermission?.invoke(null)
      } else {
        sDeniedPermission?.invoke(PermissionUtil.getDeniedPermissions(requireContext(), permissions))
      }
    }
    dismissAllowingStateLoss()
  }

  fun show(manager: FragmentManager) {
    super.show(manager, PermissionSettingDialogFragment::class.java.simpleName)
  }

  override fun onDestroy() {
    super.onDestroy()
    sDeniedPermission = null
    sRequestInstallPackageListener = null
  }

  companion object {
    private const val REQUEST_INSTALL_PACKAGES = "request_install_package"
    private const val REQUEST_INSTALL_PACKAGES_CODE = 6011

    private const val REQUEST_SETTING_CODE = 6012
    private const val PERMISSIONS = "permissions"

    private var sDeniedPermission: ((Array<String>?) -> Unit)? = null
    private var sRequestInstallPackageListener: ((Boolean) -> Unit)? = null

    fun newInstallPackageInstance(callback: ((Boolean) -> Unit)? = null): PermissionSettingDialogFragment {
      sRequestInstallPackageListener = callback
      val fragment = PermissionSettingDialogFragment()
      val bundle = Bundle()
      bundle.putBoolean(REQUEST_INSTALL_PACKAGES, true)
      fragment.arguments = bundle
      return fragment
    }

    fun newInstance(
        permissions: Array<String>,
        deniedPermission: ((Array<String>?) -> Unit)? = null
    ): PermissionSettingDialogFragment {
      sDeniedPermission = deniedPermission
      val fragment = PermissionSettingDialogFragment()
      val bundle = Bundle()
      bundle.putStringArray(PERMISSIONS, permissions)
      fragment.arguments = bundle
      return fragment
    }
  }
}