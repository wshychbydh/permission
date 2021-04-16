package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.CancellableContinuation

/**
 *Created by ycb on 2021/1/22
 */
@TargetApi(Build.VERSION_CODES.R)
internal class PermissionFileAccessFragment : Fragment() {

  private var callback: CancellableContinuation<Boolean>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    if (callback == null) {
      removeFragment()
      return
    }
    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
    startActivityForResult(intent, REQUEST_FILE_SETTING_CODE)
  }

  private fun removeFragment() {
    fragmentManager?.beginTransaction()
        ?.remove(this)
        ?.commitAllowingStateLoss()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == REQUEST_FILE_SETTING_CODE) {
      callback?.complete(Environment.isExternalStorageManager())
    }
    removeFragment()
  }

  companion object {
    private const val REQUEST_FILE_SETTING_CODE = 7012

    fun delegate(
        activity: FragmentActivity,
        callback: CancellableContinuation<Boolean>
    ) {
      activity.runOnUiThread {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        var fragment = fm.findFragmentByTag(PermissionFileAccessFragment::class.java.name)
        if (fragment != null) {
          transaction.remove(fragment)
        }
        fragment = PermissionFileAccessFragment()
        fragment.callback = callback
        transaction.add(fragment, PermissionFileAccessFragment::class.java.name)
        transaction.commitNowAllowingStateLoss()
      }
    }
  }
}