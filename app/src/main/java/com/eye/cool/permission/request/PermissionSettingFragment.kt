package com.eye.cool.permission.request

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.eye.cool.permission.support.PermissionSetting
import com.eye.cool.permission.support.PermissionUtil
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 *Created by ycb on 2019/12/17 0017
 */
internal class PermissionSettingFragment : Fragment() {

  private var deniedResultsCallback: CancellableContinuation<Array<String>?>? = null
  private var requestInstallPackageCallback: CancellableContinuation<Boolean>? = null
  private var permissions: Array<String>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (requestInstallPackageCallback == null && permissions.isNullOrEmpty()) {
      removeFragment()
      return
    }
    if (requestInstallPackageCallback == null) {
      PermissionSetting().start(this, REQUEST_SETTING_CODE)
    } else {
      val intent = Intent(
          Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          Uri.parse("package:${requireContext().packageName}")
      )
      startActivityForResult(intent, REQUEST_INSTALL_PACKAGES_CODE)
    }
  }

  private fun removeFragment() {
    fragmentManager?.beginTransaction()
        ?.remove(this)
        ?.commitAllowingStateLoss()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_INSTALL_PACKAGES_CODE) {
      requestInstallPackageCallback?.complete(resultCode == Activity.RESULT_OK)
    } else if (requestCode == REQUEST_SETTING_CODE) {
      val permissions = this.permissions
      deniedResultsCallback?.complete(
          PermissionUtil.getDeniedPermissions(requireContext(), permissions)
      )
    }
    removeFragment()
  }

  companion object {
    private const val REQUEST_INSTALL_PACKAGES = "request_install_package"
    private const val REQUEST_INSTALL_PACKAGES_CODE = 6011

    private const val REQUEST_SETTING_CODE = 6012
    private const val PERMISSIONS = "permissions"

    fun delegateInstallPackage(
        activity: FragmentActivity,
        requestInstallPackageCallback: CancellableContinuation<Boolean>
    ) {
      activity.runOnUiThread {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        var fragment = fm.findFragmentByTag(REQUEST_INSTALL_PACKAGES)
        if (fragment != null) {
          transaction.remove(fragment)
        }
        fragment = PermissionSettingFragment()
        fragment.requestInstallPackageCallback = requestInstallPackageCallback
        transaction.add(fragment, REQUEST_INSTALL_PACKAGES)
        transaction.commitNowAllowingStateLoss()
      }
    }

    fun delegate(
        activity: FragmentActivity,
        permissions: Array<String>,
        deniedResultsCallback: CancellableContinuation<Array<String>?>
    ) {
      if (permissions.isNullOrEmpty()) {
        deniedResultsCallback.resume(null)
        return
      }
      activity.runOnUiThread {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        var fragment = fm.findFragmentByTag(PERMISSIONS)
        if (fragment != null) {
          transaction.remove(fragment)
        }
        fragment = PermissionSettingFragment()
        fragment.deniedResultsCallback = deniedResultsCallback
        fragment.permissions = permissions
        transaction.add(fragment, PERMISSIONS)
        transaction.commitNowAllowingStateLoss()
      }
    }
  }
}