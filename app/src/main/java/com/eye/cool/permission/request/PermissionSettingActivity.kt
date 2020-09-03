package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.eye.cool.permission.support.PermissionSetting
import com.eye.cool.permission.support.PermissionUtil

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
 internal class PermissionSettingActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    invasionStatusBar(this)
    val requestInstallPackages = intent.getBooleanExtra(REQUEST_INSTALL_PACKAGES, false)
    if (requestInstallPackages) {
      val intent = Intent(
          Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
          Uri.parse("package:$packageName")
      )
      if (intent.resolveActivity(packageManager) != null) {
        startActivityForResult(intent, REQUEST_INSTALL_PACKAGES_CODE)
      } else {
        sRequestInstallPackageListener?.invoke(false)
        finish()
      }
    } else {
      PermissionSetting().start(this, REQUEST_SETTING_CODE)
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_INSTALL_PACKAGES_CODE) {
      var result = resultCode == RESULT_OK
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        result = result or packageManager.canRequestPackageInstalls()
      }
      sRequestInstallPackageListener?.invoke(result)
    } else if (requestCode == REQUEST_SETTING_CODE) {
      val permissions = intent.getStringArrayExtra(PERMISSIONS)
      if (permissions.isNullOrEmpty()) {
        sPermissionCallback?.invoke(null)
      } else {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          sPermissionCallback?.invoke(PermissionUtil.getDeniedPermissions(this, permissions))
        } else {
          sPermissionCallback?.invoke(permissions)
        }
      }
    }
    finish()
  }

  override fun onDestroy() {
    super.onDestroy()
    sPermissionCallback = null
    sRequestInstallPackageListener = null
  }

  companion object {

    private const val PERMISSIONS = "permissions"
    private const val REQUEST_SETTING_CODE = 8011

    private const val REQUEST_INSTALL_PACKAGES = "request_install_package"
    private const val REQUEST_INSTALL_PACKAGES_CODE = 7011

    private var sPermissionCallback: ((Array<String>?) -> Unit)? = null
    private var sRequestInstallPackageListener: ((Boolean) -> Unit)? = null

    @TargetApi(Build.VERSION_CODES.O)
    fun requestInstallPackages(context: Context, callback: ((Boolean) -> Unit)? = null) {
      sRequestInstallPackageListener = callback
      val intent = Intent(context, PermissionActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.putExtra(REQUEST_INSTALL_PACKAGES, true)
      context.startActivity(intent)
    }

    /**
     * Request for permissions.
     */
    fun startSetting(
        context: Context,
        permissions: Array<String>,
        permissionCallback: ((Array<String>?) -> Unit)? = null
    ) {
      sPermissionCallback = permissionCallback
      val intent = Intent(context, PermissionSettingActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.putExtra(PERMISSIONS, permissions)
      context.startActivity(intent)
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    private fun invasionStatusBar(activity: Activity) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = activity.window
        val decorView = window.decorView
        decorView.systemUiVisibility = (decorView.systemUiVisibility
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
      }
    }
  }
}
