package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class PermissionDialogFragment : AppCompatDialogFragment() {

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    val permissions = arguments?.getStringArray(REQUEST_PERMISSIONS)

    if (permissions.isNullOrEmpty()) {
      dismissAllowingStateLoss()
    } else {
      requestPermissions(permissions, 1)
    }
  }

  fun show(manager: FragmentManager) {
    super.show(manager, PermissionDialogFragment::class.java.simpleName)
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    sPermissionListener?.invoke(permissions, grantResults)
    dismissAllowingStateLoss()
  }

  override fun onDestroy() {
    super.onDestroy()
    sPermissionListener = null
  }

  companion object {

    private const val REQUEST_PERMISSIONS = "permissions"

    private var sPermissionListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null

    /**
     * Request for permissions.
     */
    fun newInstance(
        permissions: Array<String>,
        permissionListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null
    ): PermissionDialogFragment {
      sPermissionListener = permissionListener
      val fragment = PermissionDialogFragment()
      val bundle = Bundle()
      bundle.putStringArray(REQUEST_PERMISSIONS, permissions)
      fragment.arguments = bundle
      return fragment
    }
  }
}
