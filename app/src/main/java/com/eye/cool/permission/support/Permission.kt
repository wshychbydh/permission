package com.eye.cool.permission.support

import android.Manifest.permission.*
import android.content.Context
import com.eye.cool.permission.R
import java.util.*

/**
 * Created by cool on 2017/4/16.
 */
object Permission {

  @JvmStatic
  val MICROPHONE = arrayOf(
      RECORD_AUDIO
  )

  @JvmStatic
  val CALENDAR = arrayOf(
      READ_CALENDAR,
      WRITE_CALENDAR
  )

  @JvmStatic
  val CONTACTS = arrayOf(
      READ_CONTACTS,
      WRITE_CONTACTS,
      GET_ACCOUNTS
  )

  @JvmStatic
  val LOCATION = arrayOf(
      ACCESS_FINE_LOCATION,
      ACCESS_COARSE_LOCATION
  )

  @JvmStatic
  val CALL_LOG = arrayOf(
      READ_CALL_LOG,
      WRITE_CALL_LOG,
      PROCESS_OUTGOING_CALLS
  )

  @JvmStatic
  val PHONE = arrayOf(
      READ_PHONE_STATE,
      READ_PHONE_NUMBERS,
      CALL_PHONE,
      ADD_VOICEMAIL,
      USE_SIP,
      READ_CALL_LOG,
      WRITE_CALL_LOG,
      PROCESS_OUTGOING_CALLS
  )

  @JvmStatic
  val SMS = arrayOf(
      SEND_SMS,
      RECEIVE_SMS,
      READ_SMS,
      RECEIVE_WAP_PUSH,
      RECEIVE_MMS
  )

  @JvmStatic
  val STORAGE = arrayOf(
      WRITE_EXTERNAL_STORAGE,
      READ_EXTERNAL_STORAGE
  )

  @JvmStatic
  val SENSORS = arrayOf(
      BODY_SENSORS
  )

  @JvmStatic
  val CAMERA = arrayOf(
      android.Manifest.permission.CAMERA
  )

  @JvmStatic
  val INSTALL_PACKAGE = arrayOf(
      INSTALL_PACKAGES,
      REQUEST_INSTALL_PACKAGES
  )

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun transformText(context: Context, vararg permissions: String): List<String> {
    return transformText(context, listOf(*permissions))
  }

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun transformText(context: Context, vararg groups: Array<String>): List<String> {
    val permissionList = ArrayList<String>()
    for (group in groups) {
      permissionList.addAll(listOf(*group))
    }
    return transformText(context, permissionList)
  }

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun transformText(context: Context, permissions: List<String>): List<String> {
    val textList = ArrayList<String>()
    for (permission in permissions) {
      val result = translates[permission]
      if (!result.isNullOrEmpty()) {
        textList.add(result)
        continue
      }
      when (permission) {
        android.Manifest.permission_group.CALENDAR,
        READ_CALENDAR, WRITE_CALENDAR -> {
          val message = context.getString(R.string.permission_name_calendar)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.CAMERA,
        android.Manifest.permission.CAMERA -> {
          val message = context.getString(R.string.permission_name_camera)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.CONTACTS,
        READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS -> {
          val message = context.getString(R.string.permission_name_contacts)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.LOCATION,
        ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> {
          val message = context.getString(R.string.permission_name_location)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.MICROPHONE,
        RECORD_AUDIO -> {
          val message = context.getString(R.string.permission_name_microphone)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.PHONE,
        READ_PHONE_STATE, CALL_PHONE, USE_SIP, ADD_VOICEMAIL -> {
          val message = context.getString(R.string.permission_name_phone)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.CALL_LOG,
        READ_CALL_LOG, WRITE_CALL_LOG, PROCESS_OUTGOING_CALLS -> {
          val message = context.getString(R.string.permission_name_call_log)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.SENSORS,
        BODY_SENSORS -> {
          val message = context.getString(R.string.permission_name_sensors)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.SMS,
        SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS -> {
          val message = context.getString(R.string.permission_name_sms)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.STORAGE,
        READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE -> {
          val message = context.getString(R.string.permission_name_storage)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        INSTALL_PACKAGES, REQUEST_INSTALL_PACKAGES -> {
          val message = context.getString(R.string.permission_name_install)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        android.Manifest.permission_group.ACTIVITY_RECOGNITION -> {
          val message = context.getString(R.string.permission_activity_recognition)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        else -> {
          // fixme un known permission name
          if (!textList.contains(permission))
            textList.add(permission.substring(permission.lastIndexOf(".") + 1, permission.length))
        }
      }
    }
    return textList
  }

  private val translates = hashMapOf<String, String>()

  /**
   * Add permission translation dynamically, highest priority-first
   *
   * @param permission the permission will be translated
   * @param translate the display text to user
   */
  fun addTranslateText(permission: String, translate: String): Permission {
    translates[permission] = translate
    return this
  }

  /**
   * Add permissions translation dynamically, highest priority-first
   *
   * @param permissions the permissions will be translated
   * @param translate the display text to user
   */
  fun addTranslateText(permissions: Array<String>, translate: String): Permission {
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
  fun addTranslateText(permissions: List<String>, translate: String): Permission {
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
  fun removeTranslateText(permission: String): Permission {
    translates.remove(permission)
    return this
  }

  /**
   * Remove all dynamically permissions
   */
  fun clearText(): Permission {
    translates.clear()
    return this
  }
}