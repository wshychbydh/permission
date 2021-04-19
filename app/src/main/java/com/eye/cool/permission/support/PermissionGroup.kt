package com.eye.cool.permission.support

import android.Manifest

/**
 * https://developer.android.com/guide/topics/security/permissions?hl=zh-cn
 * Created by cool on 2020/4/16.
 */
object PermissionGroup {

  /**
   * android.permission-group.MICROPHONE
   */
  @JvmStatic
  val MICROPHONE = arrayOf(
      Manifest.permission.RECORD_AUDIO
  )

  /**
   * android.permission-group.CALENDAR
   */
  @JvmStatic
  val CALENDAR = arrayOf(
      Manifest.permission.READ_CALENDAR,
      Manifest.permission.WRITE_CALENDAR
  )

  /**
   * android.permission-group.CONTACTS
   */
  @JvmStatic
  val CONTACTS = arrayOf(
      Manifest.permission.READ_CONTACTS,
      Manifest.permission.WRITE_CONTACTS,
      Manifest.permission.GET_ACCOUNTS
  )

  /**
   * android.permission-group.LOCATION
   */
  @JvmStatic
  val LOCATION = arrayOf(
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
  )

  /**
   * android.permission-group.CALL_LOG
   */
  @JvmStatic
  val CALL_LOG = arrayOf(
      Manifest.permission.READ_CALL_LOG,
      Manifest.permission.WRITE_CALL_LOG,
      Manifest.permission.PROCESS_OUTGOING_CALLS
  )

  /**
   * android.permission-group.PHONE
   */
  @JvmStatic
  val PHONE = arrayOf(
      Manifest.permission.READ_PHONE_STATE,
      Manifest.permission.CALL_PHONE,
      Manifest.permission.ADD_VOICEMAIL,
      Manifest.permission.USE_SIP,
      Manifest.permission.READ_CALL_LOG,
      Manifest.permission.WRITE_CALL_LOG,
      Manifest.permission.PROCESS_OUTGOING_CALLS
  )

  /**
   * android.permission-group.SMS
   */
  @JvmStatic
  val SMS = arrayOf(
      Manifest.permission.SEND_SMS,
      Manifest.permission.RECEIVE_SMS,
      Manifest.permission.READ_SMS,
      Manifest.permission.RECEIVE_WAP_PUSH,
      Manifest.permission.RECEIVE_MMS
  )

  /**
   * android.permission-group.STORAGE
   */
  @JvmStatic
  val STORAGE = arrayOf(
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.READ_EXTERNAL_STORAGE
  )

  /**
   * android.permission-group.SENSORS
   */
  @JvmStatic
  val SENSORS = arrayOf(
      Manifest.permission.BODY_SENSORS
  )

  /**
   * android.permission-group.CAMERA
   */
  @JvmStatic
  val CAMERA = arrayOf(
      android.Manifest.permission.CAMERA
  )

  @JvmStatic
  val INSTALL_PACKAGE = arrayOf(
      Manifest.permission.INSTALL_PACKAGES,
      Manifest.permission.REQUEST_INSTALL_PACKAGES
  )
}