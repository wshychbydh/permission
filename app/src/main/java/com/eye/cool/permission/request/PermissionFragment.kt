package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.CancellableContinuation

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class PermissionFragment : Fragment() {

  private var permissions: Array<String>? = null
  private var grantResultsCallback: CancellableContinuation<IntArray>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val permissions = this.permissions
    if (permissions.isNullOrEmpty()) {
      removeFragment()
    } else {
      requestPermissions(permissions, 1)
    }
  }

  private fun removeFragment() {
    fragmentManager?.beginTransaction()
        ?.remove(this)
        ?.commitAllowingStateLoss()
  }

  override fun onRequestPermissionsResult(
      requestCode: Int,
      permissions: Array<String>,
      grantResults: IntArray
  ) {
    grantResultsCallback?.complete(grantResults)
    removeFragment()
  }

  companion object {

    private const val TAG = "PermissionFragment"

    fun delegate(
        activity: FragmentActivity,
        permissions: Array<String>,
        grantResultsCallback: CancellableContinuation<IntArray>
    ) {
      activity.runOnUiThread {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        var fragment = fm.findFragmentByTag(TAG)
        if (fragment != null) {
          transaction.remove(fragment)
        }
        fragment = PermissionFragment()
        fragment.grantResultsCallback = grantResultsCallback
        fragment.permissions = permissions
        transaction.add(fragment, TAG)
        transaction.commitNowAllowingStateLoss()
      }
    }
  }
}