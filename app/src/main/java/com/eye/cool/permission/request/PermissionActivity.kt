package com.eye.cool.permission.request

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
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.CancellableContinuation

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class PermissionActivity : Activity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    invasionStatusBar(this)

    val permissions = intent.getStringArrayExtra(REQUEST_PERMISSIONS)

    if (permissions.isNullOrEmpty()) {
      finish()
    } else {
      requestPermissions(permissions, 1)
    }
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    sGrantResultsCallback?.complete(grantResults)
    finish()
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event)
  }

  override fun onDestroy() {
    super.onDestroy()
    sGrantResultsCallback = null
  }

  companion object {

    private const val REQUEST_PERMISSIONS = "permissions"

    private var sGrantResultsCallback: CancellableContinuation<IntArray>? = null

    /**
     * Request for permissions.
     */
    fun requestPermission(
        context: Context,
        permissions: Array<String>,
        grantResultsCallback: CancellableContinuation<IntArray>
    ) {
      sGrantResultsCallback = grantResultsCallback
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
        decorView.systemUiVisibility = (decorView.systemUiVisibility
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
      }
    }
  }
}
