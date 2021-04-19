package com.eye.cool.permission.support

import android.Manifest.permission.*
import android.content.Context
import com.eye.cool.permission.R
import java.util.*

/**
 * https://developer.android.com/guide/topics/security/permissions?hl=zh-cn
 * Created by cool on 2017/4/16.
 */
object PermissionTranslator {

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun toText(
      context: Context,
      vararg permissions: String
  ): List<String> {
    return toText(context, listOf(*permissions))
  }

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun toText(
      context: Context,
      vararg groups: Array<String>
  ): List<String> {
    val permissionList = ArrayList<String>()
    for (group in groups) {
      permissionList.addAll(listOf(*group))
    }
    return toText(context, permissionList)
  }

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun toText(
      context: Context,
      permissions: List<String>
  ): List<String> {
    val textList = ArrayList<String>()
    for (permission in permissions) {
      val result = translates[permission]
      if (!result.isNullOrEmpty()) {
        textList.add(result)
        continue
      }
      val text = toText(context, permission, false)
      if (!textList.contains(text)) {
        textList.add(text)
      }
    }
    return textList
  }

  /**
   * Turn permissions into text.
   *
   * @param context
   * @param permission
   * @param useCustom {@link addTranslateText}
   */
  @JvmStatic
  fun toText(
      context: Context,
      permission: String,
      useCustom: Boolean = true
  ): String {

    if (useCustom) {
      val result = translates[permission]
      if (!result.isNullOrEmpty()) {
        return result
      }
    }

    return when (permission) {
      android.Manifest.permission_group.CALENDAR,
      READ_CALENDAR, WRITE_CALENDAR -> {
        context.getString(R.string.permission_name_calendar)
      }

      android.Manifest.permission_group.CAMERA,
      android.Manifest.permission.CAMERA -> {
        context.getString(R.string.permission_name_camera)
      }

      android.Manifest.permission_group.CONTACTS,
      READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS -> {
        context.getString(R.string.permission_name_contacts)
      }

      android.Manifest.permission_group.LOCATION,
      ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> {
        context.getString(R.string.permission_name_location)
      }

      android.Manifest.permission_group.MICROPHONE,
      RECORD_AUDIO -> {
        context.getString(R.string.permission_name_microphone)
      }

      android.Manifest.permission_group.PHONE,
      READ_PHONE_STATE, CALL_PHONE, USE_SIP, ADD_VOICEMAIL -> {
        context.getString(R.string.permission_name_phone)
      }

      android.Manifest.permission_group.CALL_LOG,
      READ_CALL_LOG, WRITE_CALL_LOG, PROCESS_OUTGOING_CALLS -> {
        context.getString(R.string.permission_name_call_log)
      }

      android.Manifest.permission_group.SENSORS,
      BODY_SENSORS -> {
        context.getString(R.string.permission_name_sensors)
      }

      android.Manifest.permission_group.SMS,
      SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS -> {
        context.getString(R.string.permission_name_sms)
      }

      android.Manifest.permission_group.STORAGE,
      READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE -> {
        context.getString(R.string.permission_name_storage)
      }

      MANAGE_EXTERNAL_STORAGE -> {
        context.getString(R.string.permission_all_file_access)
      }

      INSTALL_PACKAGES, REQUEST_INSTALL_PACKAGES -> {
        context.getString(R.string.permission_name_install)
      }

      android.Manifest.permission_group.ACTIVITY_RECOGNITION -> {
        context.getString(R.string.permission_activity_recognition)
      }

      else -> {
        // fixme un known permission name
        permission.substring(permission.lastIndexOf(".") + 1, permission.length)
      }
    }
  }

  private val translates = hashMapOf<String, String>()

  /**
   * Add permission translation dynamically, highest priority-first
   *
   * @param permission the permission will be translated
   * @param translate the display text to user
   */
  fun addText(permission: String, translate: String): PermissionTranslator {
    translates[permission] = translate
    return this
  }

  /**
   * Add permissions translation dynamically, highest priority-first
   *
   * @param permissions the permissions will be translated
   * @param translate the display text to user
   */
  fun addText(permissions: Array<String>, translate: String): PermissionTranslator {
    permissions.forEach {
      translates[it] = translate
    }
    return this
  }

  /**
   * Add permissions translation dynamically, highest priority-first
   *
   * @param permissions the permissions will be translated
   * @param translate the display text to user
   */
  fun addText(permissions: List<String>, translate: String): PermissionTranslator {
    permissions.forEach {
      translates[it] = translate
    }
    return this
  }

  /**
   * Remove permission translation, Only remove dynamically added ones
   *
   * @param permission the permission's translate will be removed
   */
  fun removeText(permission: String): PermissionTranslator {
    translates.remove(permission)
    return this
  }

  /**
   * Remove all dynamically permissions
   */
  @Deprecated("Use reset() instead")
  fun clearText(): PermissionTranslator {
    translates.clear()
    return this
  }

  /**
   * Remove all dynamically permissions
   */
  fun reset(): PermissionTranslator {
    translates.clear()
    return this
  }
}