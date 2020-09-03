package com.eye.cool.permission.support

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eye.cool.permission.request.PermissionActivity
import com.eye.cool.permission.request.PermissionDialogFragment
import com.eye.cool.permission.request.PermissionSettingActivity
import com.eye.cool.permission.request.PermissionSettingDialogFragment
import com.eye.cool.permission.request.PermissionProxyActivity
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *Created by ycb on 2019/12/17 0017
 */
internal class CompatContext {

  private var context: Context? = null
  private var proxyActivity: AppCompatActivity? = null
  private var activity: FragmentActivity? = null

  constructor(context: Context) {
    if (context is FragmentActivity) {
      this.activity = context
    } else {
      this.context = context
    }
  }

  constructor(fragmentX: Fragment) {
    this.activity = fragmentX.requireActivity()
  }

  fun release() {
    proxyActivity?.finish()
  }

  suspend fun proxyContext() = suspendCoroutine<CompatContext> {
    if (activity == null) {
      PermissionProxyActivity.launch(context()) { activity ->
        proxyActivity = activity
        it.resume(this)
      }
    } else {
      it.resume(this)
    }
  }

  fun startSettingForResult(
      permissions: Array<String>,
      callback: ((Array<String>?) -> Unit)? = null
  ) {

    if (permissions.isEmpty()) {
      callback?.invoke(null)
      return
    }

    when {
      proxyActivity != null -> {
        PermissionSettingDialogFragment.newInstance(permissions, callback)
            .show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionSettingActivity.startSetting(context(), permissions, callback)
      }
      activity != null -> {
        PermissionSettingDialogFragment.newInstance(permissions, callback)
            .show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun requestInstallPackage(callback: ((Boolean) -> Unit)? = null) {
    when {
      proxyActivity != null -> {
        PermissionSettingDialogFragment.newInstallPackageInstance(callback)
            .show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionSettingActivity.requestInstallPackages(context(), callback)
      }
      activity != null -> {
        PermissionSettingDialogFragment.newInstallPackageInstance(callback)
            .show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun requestPermission(
      permissions: Array<String>,
      callback: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null
  ) {
    when {
      proxyActivity != null -> {
        PermissionDialogFragment.newInstance(permissions, callback)
            .show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionActivity.requestPermission(context(), permissions, callback)
      }
      activity != null -> {
        PermissionDialogFragment.newInstance(permissions, callback)
            .show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun context(): Context {
    return proxyActivity ?: activity ?: context
    ?: throw IllegalStateException("CompatContext init error")
  }
}