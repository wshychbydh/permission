package com.eye.cool.permission.request

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.eye.cool.permission.R

/**
 * Request permissions.
 * Created cool on 2018/4/16.
 */
@TargetApi(Build.VERSION_CODES.M)
internal class PermissionDialogFragment : AppCompatDialogFragment() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setStyle(DialogFragment.STYLE_NO_INPUT, R.style.permission_translate_dialog)
  }

  override fun onStart() {
    super.onStart()
    val window: Window = dialog!!.window ?: return
    val lp = window.attributes as WindowManager.LayoutParams
    lp.dimAmount = 0f
    lp.flags = lp.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
    window.attributes = lp
  }

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

  override fun onRequestPermissionsResult(
          requestCode: Int,
          permissions: Array<String>,
          grantResults: IntArray
  ) {
    sListener?.invoke(permissions, grantResults)
    dismissAllowingStateLoss()
  }

  override fun onDestroy() {
    super.onDestroy()
    sListener = null
  }

  companion object {

    private const val REQUEST_PERMISSIONS = "permissions"

    private var sListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null

    /**
     * Request for permissions.
     */
    fun newInstance(
        permissions: Array<String>,
        permissionListener: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null
    ): PermissionDialogFragment {
      sListener = permissionListener
      val fragment = PermissionDialogFragment()
      val bundle = Bundle()
      bundle.putStringArray(REQUEST_PERMISSIONS, permissions)
      fragment.arguments = bundle
      return fragment
    }
  }
}