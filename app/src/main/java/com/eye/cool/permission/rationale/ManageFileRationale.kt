package com.eye.cool.permission.rationale

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.eye.cool.permission.R

/**
 * Created by cool on 2018/4/20.
 */
internal class ManageFileRationale : Rationale {

  override fun showRationale(
      context: Context,
      permissions: Array<String>,
      callback: (result: Boolean) -> Unit
  ) {

    val message = context.getString(R.string.permission_manage_file_rationale, getAppName(context))

    AlertDialog.Builder(context)
        .setCancelable(false)
        .setTitle(R.string.permission_title_rationale)
        .setMessage(message)
        .setPositiveButton(R.string.permission_setting) { _, _ ->
          callback.invoke(true)
        }
        .setNegativeButton(R.string.permission_no) { _, _ ->
          callback.invoke(false)
        }
        .show()
  }

  private fun getAppName(context: Context): String {
    val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
    return context.packageManager.getApplicationLabel(appInfo) as? String ?: ""
  }
}