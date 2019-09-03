package com.eye.cool.permission

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class PermissionActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    invasionStatusBar(this)

    val intent = intent
    val permissions = intent.getStringArrayExtra(REQUEST_PERMISSIONS)

    if (permissions != null && permissions.isNotEmpty()) {
      requestPermissions(permissions, 1)
    } else {
      sPermissionListener = null
      finish()
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    sPermissionListener?.invoke(permissions, grantResults)
    sPermissionListener = null
    finish()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event)
  }

  companion object {

    private const val REQUEST_PERMISSIONS = "PERMISSIONS"

    private var sPermissionListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null

    /**
     * Request for permissions.
     */
    fun requestPermission(context: Context, permissions: Array<String>,
                          permissionListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null) {
      sPermissionListener = permissionListener
      val intent = Intent(context, PermissionActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      intent.putExtra(REQUEST_PERMISSIONS, permissions)
      context.startActivity(intent)
    }

    /**
     * Set the content layout full the StatusBar, but do not hide StatusBar.
     */
    private fun invasionStatusBar(activity: Activity) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        val window = activity.window
        val decorView = window.decorView
        decorView.systemUiVisibility = (decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
      }
    }
  }
}
