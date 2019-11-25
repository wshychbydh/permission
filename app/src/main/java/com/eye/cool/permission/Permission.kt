package com.eye.cool.permission

import android.Manifest.permission.*
import android.annotation.TargetApi
import android.content.Context
import android.os.StrictMode
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
      ANSWER_PHONE_CALLS,
      ADD_VOICEMAIL,
      USE_SIP
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
      when (permission) {
        READ_CALENDAR, WRITE_CALENDAR -> {
          val message = context.getString(R.string.permission_name_calendar)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }

        android.Manifest.permission.CAMERA -> {
          val message = context.getString(R.string.permission_name_camera)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        READ_CONTACTS, WRITE_CONTACTS, GET_ACCOUNTS -> {
          val message = context.getString(R.string.permission_name_contacts)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION -> {
          val message = context.getString(R.string.permission_name_location)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        RECORD_AUDIO -> {
          val message = context.getString(R.string.permission_name_microphone)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        READ_PHONE_STATE, CALL_PHONE, READ_CALL_LOG, WRITE_CALL_LOG, USE_SIP, PROCESS_OUTGOING_CALLS -> {
          val message = context.getString(R.string.permission_name_phone)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        BODY_SENSORS -> {
          val message = context.getString(R.string.permission_name_sensors)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
        SEND_SMS, RECEIVE_SMS, READ_SMS, RECEIVE_WAP_PUSH, RECEIVE_MMS -> {
          val message = context.getString(R.string.permission_name_sms)
          if (!textList.contains(message)) {
            textList.add(message)
          }
        }
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
        else -> {
          // fixme un known permission name
          if (textList.contains(permission))
            textList.add(permission)
        }
      }
    }
    return textList
  }

  /**
   * Resolve file uri access issues for 7.0 and above
   * Call in application's onCreate. not recommend!
   */
  @TargetApi(18)
  fun detectFileUriExposure() {
    val builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())
    builder.detectFileUriExposure()
  }
}