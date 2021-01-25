package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.eye.cool.permission.support.PermissionSetting
import com.eye.cool.permission.support.PermissionUtil
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.CancellableContinuation

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
internal class PermissionSettingActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    invasionStatusBar(this)
    val requestInstallPkg = intent.getBooleanExtra(REQUEST_INSTALL_PACKAGES, false)
    if (requestInstallPkg) {
      if (intent.resolveActivity(packageManager) != null) {
        startActivityForResult(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:$packageName")
            ),
            REQUEST_INSTALL_PACKAGES_CODE
        )
      } else {
        sRequestInstallPackageCallback?.complete(false)
        finish()
      }
    } else if (sAllFileAccessCallback != null) {
      startActivityForResult(
          Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION),
          REQUEST_ALL_FILE_ACCESS_CODE
      )
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
      sRequestInstallPackageCallback?.complete(result)
    } else if (requestCode == REQUEST_ALL_FILE_ACCESS_CODE) {
      sAllFileAccessCallback?.complete(Environment.isExternalStorageManager())
    } else if (requestCode == REQUEST_SETTING_CODE) {
      var permissions = intent.getStringArrayExtra(PERMISSIONS)
      if (permissions.isNullOrEmpty()) {
        sDeniedPermissionCallback?.complete(null)
      } else {
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          PermissionUtil.getDeniedPermissions(this, permissions)
        } else permissions
        sDeniedPermissionCallback?.complete(permissions)
      }
    }
    release()
    finish()
  }

  override fun onDestroy() {
    super.onDestroy()
    release()
  }

  private fun release() {
    sDeniedPermissionCallback = null
    sRequestInstallPackageCallback = null
    sAllFileAccessCallback = null
  }

  companion object {

    private const val PERMISSIONS = "permissions"
    private const val REQUEST_SETTING_CODE = 8011

    private const val REQUEST_INSTALL_PACKAGES = "request_install_package"
    private const val REQUEST_INSTALL_PACKAGES_CODE = 7011

    private const val REQUEST_ALL_FILE_ACCESS_CODE = 6011

    @Volatile
    private var sDeniedPermissionCallback: CancellableContinuation<Array<String>?>? = null

    @Volatile
    private var sRequestInstallPackageCallback: CancellableContinuation<Boolean>? = null

    @Volatile
    private var sAllFileAccessCallback: CancellableContinuation<Boolean>? = null

    @TargetApi(Build.VERSION_CODES.O)
    fun delegateInstallPackage(
        context: Context,
        requestInstallPackageCallback: CancellableContinuation<Boolean>
    ) {
      sRequestInstallPackageCallback = requestInstallPackageCallback
      val intent = Intent(context, PermissionActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.putExtra(REQUEST_INSTALL_PACKAGES, true)
      context.startActivity(intent)
    }

    /**
     * Request for permissions.
     */
    fun delegateSetting(
        context: Context,
        permissions: Array<String>,
        deniedPermissionCallback: CancellableContinuation<Array<String>?>
    ) {
      sDeniedPermissionCallback = deniedPermissionCallback
      val intent = Intent(context, PermissionSettingActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.putExtra(PERMISSIONS, permissions)
      context.startActivity(intent)
    }

    /**
     * Request for all file manage.
     */
    fun delegateAllFileAccessSetting(
        context: Context,
        allFileAccessCallback: CancellableContinuation<Boolean>
    ) {
      sAllFileAccessCallback = allFileAccessCallback
      val intent = Intent(context, PermissionSettingActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
