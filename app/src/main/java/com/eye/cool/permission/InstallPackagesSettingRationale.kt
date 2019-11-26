package com.eye.cool.permission

import android.app.AlertDialog
import android.content.Context

/**
 * Created by cool on 2018/4/20.
 */
internal class InstallPackagesSettingRationale : Rationale {

  override fun showRationale(
      context: Context,
      permissions: Array<String>,
      callback: ((result: Boolean) -> Unit)?
  ) {

    val message = context.getString(R.string.permission_install_packages_setting_rationale, getAppName(context))

    AlertDialog.Builder(context)
        .setCancelable(false)
        .setTitle(R.string.permission_title_rationale)
        .setMessage(message)
        .setPositiveButton(R.string.permission_setting) { _, _ ->
          PermissionActivity.requestInstallPackages(context, callback)
        }
        .setNegativeButton(R.string.permission_no) { _, _ -> callback?.invoke(false) }
        .show()
  }

  private fun getAppName(context: Context): String {
    val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0) ?: return ""
    return context.packageManager.getApplicationLabel(appInfo) as? String ?: ""
  }
}