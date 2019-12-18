package com.eye.cool.permission.support

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.fragment.app.Fragment

/**
 * Created by cool on 2018/4/20.
 */
class PermissionSetting {

  fun start(context: Context) {
    start(context, null)
  }

  fun start(context: Context, requestCode: Int?) {
    if (context is Activity) {
      start(context, requestCode)
    } else {
      val intent = obtainSettingIntent(context)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      try {
        context.startActivity(intent)
      } catch (e: Exception) {
        context.startActivity(defaultApi(context))
      }
    }
  }

  fun start(activity: Activity) {
    start(activity, null)
  }

  fun start(activity: Activity, requestCode: Int?) {
    val intent = obtainSettingIntent(activity)
    try {
      if (requestCode == null) {
        activity.startActivity(intent)
      } else {
        activity.startActivityForResult(intent, requestCode)
      }
    } catch (e: Exception) {
      if (requestCode == null) {
        activity.startActivity(defaultApi(activity))
      } else {
        activity.startActivityForResult(defaultApi(activity), requestCode)
      }
    }
  }

  fun start(fragment: Fragment) {
    start(fragment, null)
  }

  fun start(fragment: Fragment, requestCode: Int?) {
    val intent = obtainSettingIntent(fragment.requireContext())
    try {
      if (requestCode == null) {
        fragment.startActivity(intent)
      } else {
        fragment.startActivityForResult(intent, requestCode)
      }
    } catch (e: Exception) {
      if (requestCode == null) {
        fragment.startActivity(defaultApi(fragment.requireContext()))
      } else {
        fragment.startActivityForResult(defaultApi(fragment.requireContext()), requestCode)
      }
    }
  }

  private fun obtainSettingIntent(context: Context): Intent {
    return when {
      MARK.contains("huawei") -> huaweiApi(context)
      MARK.contains("xiaomi") -> xiaomiApi(context)
      MARK.contains("oppo") -> oppoApi(context)
      MARK.contains("vivo") -> vivoApi(context)
      MARK.contains("samsung") -> samsungApi(context)
      MARK.contains("meizu") -> meizuApi(context)
      MARK.contains("smartisan") -> smartisanApi(context)
      else -> defaultApi(context)
    }
  }

  companion object {

    private val MARK = Build.MANUFACTURER.toLowerCase()

    /**
     * App details page.
     */
    private fun defaultApi(context: Context): Intent {
      val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
      intent.data = Uri.fromParts("package", context.packageName, null)
      return intent
    }

    /**
     * Huawei cell phone Api23 the following method.
     */
    private fun huaweiApi(context: Context): Intent {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return defaultApi(context)
      }
      val intent = Intent()
      intent.component = ComponentName("com.huawei.systemmanager", "com.huawei" + ".permissionmanager.ui.NotifyActivity")
      return intent
    }

    /**
     * Xiaomi phone to achieve the method.
     */
    private fun xiaomiApi(context: Context): Intent {
      val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
      intent.putExtra("extra_pkgname", context.packageName)
      return intent
    }

    /**
     * Vivo phone to achieve the method.
     */
    private fun vivoApi(context: Context): Intent {
      val intent = Intent()
      intent.putExtra("packagename", context.packageName)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        intent.component = ComponentName("com.vivo.permissionmanager", "com.vivo" + ".permissionmanager.context.SoftPermissionDetailActivity")
      } else {
        intent.component = ComponentName("com.iqoo.secure", "com.iqoo.secure.safeguard" + ".SoftPermissionDetailActivity")
      }
      return intent
    }

    /**
     * Oppo phone to achieve the method.
     */
    private fun oppoApi(context: Context): Intent {
      return defaultApi(context)
    }

    /**
     * Meizu phone to achieve the method.
     */
    private fun meizuApi(context: Context): Intent {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
        return defaultApi(context)
      }
      val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
      intent.putExtra("packageName", context.packageName)
      intent.component = ComponentName("com.meizu.safe", "com.meizu.safe.security" + ".AppSecActivity")
      return intent
    }

    /**
     * Smartisan phone to achieve the method.
     */
    private fun smartisanApi(context: Context): Intent {
      return defaultApi(context)
    }

    /**
     * Samsung phone to achieve the method.
     */
    private fun samsungApi(context: Context): Intent {
      return defaultApi(context)
    }
  }
}