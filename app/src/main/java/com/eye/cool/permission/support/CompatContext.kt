package com.eye.cool.permission.support

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eye.cool.permission.request.*
import kotlinx.coroutines.suspendCancellableCoroutine
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
    proxyActivity = null
    context = null
    activity = null
  }

  suspend fun proxyContext() = suspendCancellableCoroutine<CompatContext> {
    if (activity == null) {
      PermissionProxyActivity.launch(context()) { activity ->
        proxyActivity = activity
        if (it.isActive) it.resume(this)
      }
    } else {
      it.resume(this)
    }
  }

  suspend fun startSettingForResult(
      permissions: Array<String>
  ) = suspendCancellableCoroutine<Array<String>?> {

    if (permissions.isEmpty()) {
      it.resume(null)
      return@suspendCancellableCoroutine
    }

    when {
      proxyActivity != null -> {
        PermissionSettingDialogFragment.newInstance(permissions) { result ->
          if (it.isActive) it.resume(result)
        }.show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionSettingActivity.startSetting(context(), permissions) { result ->
          if (it.isActive) it.resume(result)
        }
      }
      activity != null -> {
        PermissionSettingDialogFragment.newInstance(permissions) { result ->
          if (it.isActive) it.resume(result)
        }.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  suspend fun requestInstallPackage() = suspendCancellableCoroutine<Boolean> {
    when {
      proxyActivity != null -> {
        PermissionSettingDialogFragment.newInstallPackageInstance { result ->
          if (it.isActive) it.resume(result)
        }.show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionSettingActivity.requestInstallPackages(context()) { result ->
          if (it.isActive) it.resume(result)
        }
      }
      activity != null -> {
        PermissionSettingDialogFragment.newInstallPackageInstance() { result ->
          if (it.isActive) it.resume(result)
        }.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  suspend fun requestPermission(
      permissions: Array<String>
  ) = suspendCancellableCoroutine<IntArray> {
    when {
      proxyActivity != null -> {
        PermissionDialogFragment.newInstance(permissions) { _, grantResults ->
          if (it.isActive) it.resume(grantResults)
        }.show(proxyActivity!!.supportFragmentManager)
      }
      context != null -> {
        PermissionActivity.requestPermission(context(), permissions) { _, grantResults ->
          if (it.isActive) it.resume(grantResults)
        }
      }
      activity != null -> {
        PermissionDialogFragment.newInstance(permissions) { _, grantResults ->
          if (it.isActive) it.resume(grantResults)
        }.show(activity!!.supportFragmentManager)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun context(): Context {
    return proxyActivity ?: activity ?: context
    ?: throw IllegalStateException("CompatContext init error")
  }
}