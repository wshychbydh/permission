package com.eye.cool.permission

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Created by cool on 2018/4/20.
 */
internal class PermissionSetting(private val context: Context) {

  fun start() {
    val intent = obtainSettingIntent()
    try {
      context.startActivity(intent)
    } catch (e: Exception) {
      context.startActivity(defaultApi(context))
    }
  }

  private fun obtainSettingIntent(): Intent {
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