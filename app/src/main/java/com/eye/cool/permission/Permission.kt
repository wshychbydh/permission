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

  @TargetApi(20)
  const val BODY_SENSORS = android.Manifest.permission.BODY_SENSORS

  const val CAMERA = android.Manifest.permission.CAMERA

  const val WRITE_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

  @TargetApi(16)
  const val READ_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE

  @JvmStatic
  val CALENDAR = arrayOf(
      android.Manifest.permission.READ_CALENDAR,
      android.Manifest.permission.WRITE_CALENDAR
  )

  @JvmStatic
  val CONTACTS = arrayOf(
      android.Manifest.permission.READ_CONTACTS,
      android.Manifest.permission.WRITE_CONTACTS,
      android.Manifest.permission.GET_ACCOUNTS
  )

  @JvmStatic
  val LOCATION = arrayOf(
      android.Manifest.permission.ACCESS_FINE_LOCATION,
      android.Manifest.permission.ACCESS_COARSE_LOCATION
  )

  @JvmStatic
  val PHONE = arrayOf(
      android.Manifest.permission.READ_PHONE_STATE,
      android.Manifest.permission.CALL_PHONE,
      android.Manifest.permission.READ_CALL_LOG,
      android.Manifest.permission.WRITE_CALL_LOG,
      android.Manifest.permission.ADD_VOICEMAIL,
      android.Manifest.permission.USE_SIP,
      android.Manifest.permission.PROCESS_OUTGOING_CALLS
  )

  @JvmStatic
  val SMS = arrayOf(
      android.Manifest.permission.SEND_SMS,
      android.Manifest.permission.RECEIVE_SMS,
      android.Manifest.permission.READ_SMS,
      android.Manifest.permission.RECEIVE_WAP_PUSH,
      android.Manifest.permission.RECEIVE_MMS
  )

  @JvmStatic
  val STORAGE = arrayOf(
      WRITE_STORAGE,
      READ_STORAGE
  )

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun transformText(context: Context, vararg permissions: String): List<String> {
    return transformText(context, Arrays.asList(*permissions))
  }

  /**
   * Turn permissions into text.
   */
  @JvmStatic
  fun transformText(context: Context, vararg groups: Array<String>): List<String> {
    val permissionList = ArrayList<String>()
    for (group in groups) {
      permissionList.addAll(Arrays.asList(*group))
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

        CAMERA -> {
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
      }
    }
    return textList
  }

  /**
   * 解决7.0及以上文件uri访问问题
   * Call in application's onCreate. not recommend
   */
  @TargetApi(18)
  fun detectFileUriExposure() {
    // android 7.0系统解决拍照的问题
    val builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())
    builder.detectFileUriExposure()
  }
}