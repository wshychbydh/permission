package com.eye.cool.permission.support

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.eye.cool.permission.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

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
        it.complete(this)
      }
    } else {
      it.resume(this)
    }
  }

  suspend fun startSettingForResult(
      permissions: Array<String>
  ) = suspendCancellableCoroutine<Array<String>?> {

    if (permissions.isEmpty()) {
      it.complete(null)
      return@suspendCancellableCoroutine
    }

    when {
      proxyActivity != null -> {
        PermissionSettingFragment.delegate(proxyActivity!!, permissions, it)
      }
      context != null -> {
        PermissionSettingActivity.delegateSetting(context(), permissions, it)
      }
      activity != null -> {
        PermissionSettingFragment.delegate(activity!!, permissions, it)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  suspend fun requestInstallPackage() = suspendCancellableCoroutine<Boolean> {
    when {
      proxyActivity != null -> {
        PermissionSettingFragment.delegateInstallPackage(proxyActivity!!, it)
      }
      context != null -> {
        PermissionSettingActivity.delegateInstallPackage(context(), it)
      }
      activity != null -> {
        PermissionSettingFragment.delegateInstallPackage(activity!!, it)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  suspend fun requestPermission(
      permissions: Array<String>
  ) = suspendCancellableCoroutine<IntArray> {
    when {
      proxyActivity != null -> {
        PermissionFragment.delegate(proxyActivity!!, permissions, it)
      }
      context != null -> {
        PermissionActivity.requestPermission(context(), permissions, it)
      }
      activity != null -> {
        PermissionFragment.delegate(activity!!, permissions, it)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  suspend fun requestAllFileAccess() = suspendCancellableCoroutine<Boolean> {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
      it.complete(true)
      return@suspendCancellableCoroutine
    }
    if (Environment.isExternalStorageManager()) {
      it.complete(true)
      return@suspendCancellableCoroutine
    }
    when {
      proxyActivity != null -> {
        PermissionFileAccessFragment.delegate(proxyActivity!!, it)
      }
      context != null -> {
        PermissionSettingActivity.delegateAllFileAccessSetting(context(), it)
      }
      activity != null -> {
        PermissionFileAccessFragment.delegate(activity!!, it)
      }
      else -> throw IllegalStateException("CompatContext init error")
    }
  }

  fun context(): Context {
    return proxyActivity ?: activity ?: context
    ?: throw IllegalStateException("CompatContext init error")
  }
}