package com.eye.cool.permission.support

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.eye.cool.permission.request.PermissionActivity
import com.eye.cool.permission.request.PermissionDialogFragment
import com.eye.cool.permission.request.PermissionSettingActivity
import com.eye.cool.permission.request.PermissionSettingDialogFragment

/**
 *Created by ycb on 2019/12/17 0017
 */
internal class CompatContext {

  private var context: Context? = null
  private var fragment: Fragment? = null
  private var activity: AppCompatActivity? = null

  constructor(context: Context) {
    if (context is AppCompatActivity) {
      this.activity = context
    } else {
      this.context = context
    }
  }

  constructor(fragmentX: Fragment) {
    this.fragment = fragmentX
  }

  fun startSettingForResult(permissions: Array<String>, deniedPermissions: ((Array<String>?) -> Unit)? = null) {

    when {
      context != null -> {
        PermissionSettingActivity.startSetting(context(), permissions, deniedPermissions)
      }
      fragment != null -> {
        val dialog = PermissionSettingDialogFragment.newInstance(permissions, deniedPermissions)
        dialog.show(fragment!!.childFragmentManager)
      }
      activity != null -> {
        val dialog = PermissionSettingDialogFragment.newInstance(permissions, deniedPermissions)
        dialog.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun requestInstallPackage(callback: ((Boolean) -> Unit)? = null) {
    when {
      context != null -> {
        PermissionSettingActivity.requestInstallPackages(context(), callback)
      }
      fragment != null -> {
        val dialog = PermissionSettingDialogFragment.newInstallPackageInstance(callback)
        dialog.show(fragment!!.childFragmentManager)
      }
      activity != null -> {
        val dialog = PermissionSettingDialogFragment.newInstallPackageInstance(callback)
        dialog.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun requestPermission(
      permissions: Array<String>,
      grantResults: ((permissions: Array<String>, grantResults: IntArray) -> Unit)? = null
  ) {
    when {
      context != null -> {
        PermissionActivity.requestPermission(context(), permissions, grantResults)
      }
      fragment != null -> {
        val dialog = PermissionDialogFragment.newInstance(permissions, grantResults)
        dialog.show(fragment!!.childFragmentManager)
      }
      activity != null -> {
        val dialog = PermissionDialogFragment.newInstance(permissions, grantResults)
        dialog.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun context(): Context {
    return context ?: fragment?.context ?: activity
    ?: throw IllegalStateException("CompatContext init error")
  }
}