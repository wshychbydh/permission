package com.eye.cool.permission.support

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.os.StrictMode


/**
 *Created by ycb on 2019/9/3 0003
 */
object PermissionUtil {

  /**
   * Check camera is available below 6.0
   */
  @JvmStatic
  fun isCameraAvailable(): Boolean {
    var camera: Camera? = null
    return try {
      camera = Camera.open()
      // setParameters is Used for MeiZu MX5.
      camera!!.parameters = camera.parameters
      true
    } catch (e: Exception) {
      false
    } finally {
      try {
        camera?.release()
      } catch (ignore: Exception) {
      }
    }
  }

  /**
   * Check external storage dir is write available
   */
  @JvmStatic
  fun isExternalDirAvailable(): Boolean {
    return isExternalStorageWritable()
  }

  /**
   * Checks if a volume containing external storage is available for read and write.
   */
  @JvmStatic
  fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
  }

  /**
   * Get registered permissions in AndroidManifest or empty array
   */
  @JvmStatic
  fun getRequestedPermissions(context: Context): Array<String> {
    try {
      val packageInfo: PackageInfo = context.packageManager
          .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
      return packageInfo.requestedPermissions
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return emptyArray()
  }

  /**
   * Checks if a volume containing external storage is available to at least read.
   */
  @JvmStatic
  fun isExternalStorageReadable(): Boolean {
    return Environment.getExternalStorageState() in
        setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
  }

  /**
   * Check external cache dir is write available
   */
  @JvmStatic
  fun isCacheDirAvailable(context: Context): Boolean {
    val appFile = context.externalCacheDir ?: return false
    return appFile.canWrite() && appFile.canRead()
  }

  private fun getMinBufferSize(): Int {
    for (rate in intArrayOf(44100, 22050, 11025, 16000, 8000)) {  // add the rates you wish to check against
      val bufferSize = AudioRecord.getMinBufferSize(
          rate, AudioFormat.CHANNEL_OUT_DEFAULT, AudioFormat.ENCODING_PCM_16BIT
      )
      if (bufferSize > 0) {
        return bufferSize
      }
    }
    return 0
  }

  @JvmStatic
  fun isRecordAvailable(): Boolean {
    var audioRecord: AudioRecord? = null
    try {
      audioRecord = AudioRecord(
          MediaRecorder.AudioSource.DEFAULT,
          44100,
          AudioFormat.CHANNEL_IN_MONO,
          AudioFormat.ENCODING_PCM_16BIT,
          getMinBufferSize()
      )
      audioRecord.startRecording()
      return audioRecord.recordingState == AudioRecord.RECORDSTATE_RECORDING
    } catch (ignore: Exception) {
      return false
    } finally {
      try {
        audioRecord?.stop()
        audioRecord?.release()
      } catch (ignore: Exception) {
      }
    }
  }

  /**
   * Resolve file uri access issues for 7.0 and above
   * Call in application's onCreate. not recommend!
   */
  @JvmStatic
  @TargetApi(18)
  fun detectFileUriExposure() {
    val builder = StrictMode.VmPolicy.Builder()
    StrictMode.setVmPolicy(builder.build())
    builder.detectFileUriExposure()
  }

  @JvmStatic
  @TargetApi(Build.VERSION_CODES.M)
  fun getDeniedPermissions(context: Context, permissions: Array<String>?): Array<String> {
    val requestList = mutableListOf<String>()
    permissions?.forEach {
      if (context.checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
        requestList.add(it)
      }
    }
    return requestList.toTypedArray()
  }

  internal fun filterPermissionGroup(permissions: List<String>): ArrayList<String> {
    val filtered = ArrayList<String>()
    permissions.forEach {
      if (it.startsWith("android.permission-group")) {
        filtered.addAll(parsePermissionGroup(it))
      } else {
        filtered.add(it)
      }
    }
    return filtered
  }

  private fun parsePermissionGroup(group: String): Array<String> {
    return when (group) {
      Manifest.permission_group.CALENDAR -> PermissionGroup.CALENDAR
      Manifest.permission_group.CALL_LOG -> PermissionGroup.CALL_LOG
      Manifest.permission_group.CAMERA -> PermissionGroup.CAMERA
      Manifest.permission_group.CONTACTS -> PermissionGroup.CONTACTS
      Manifest.permission_group.LOCATION -> PermissionGroup.LOCATION
      Manifest.permission_group.MICROPHONE -> PermissionGroup.MICROPHONE
      Manifest.permission_group.PHONE -> PermissionGroup.PHONE
      Manifest.permission_group.SENSORS -> PermissionGroup.SENSORS
      Manifest.permission_group.SMS -> PermissionGroup.SMS
      Manifest.permission_group.STORAGE -> PermissionGroup.STORAGE
      else -> arrayOf(group)
    }
  }

  internal fun isNeedShowRationalePermission(context: Context, permission: String): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return false
    val packageManager = context.packageManager
    val pkManagerClass = packageManager.javaClass
    return try {
      val method = pkManagerClass.getMethod("shouldShowRequestPermissionRationale", String::class.java)
      method.isAccessible = true
      method.invoke(packageManager, permission) as Boolean? ?: false
    } catch (ignored: Exception) {
      false
    }
  }

  internal fun hasInstallPermissionOnly(permissions: List<String>): Boolean {
    val installPermissions = PermissionGroup.INSTALL_PACKAGE
    if (permissions.size > installPermissions.size) return false
    if (permissions.size == 1) {
      return permissions[0] == Manifest.permission.INSTALL_PACKAGES
          || permissions[0] == Manifest.permission.REQUEST_INSTALL_PACKAGES
    }
    if (permissions.size == 2) {
      return installPermissions.contains(permissions[0])
          && installPermissions.contains(permissions[1])
    }
    return false
  }

  internal fun hasManageFilePermissionOnly(permissions: List<String>): Boolean {

    if (BuildVersion.isBuildBelowR()) return false

    if (permissions.size == 1) {
      return permissions[0] == Manifest.permission.MANAGE_EXTERNAL_STORAGE
          || permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
    if (permissions.size == 2) {
      val write = permissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
          || permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      val read = permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
      return write && read
    }
    if (permissions.size == 3) {
      return permissions.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
          && permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)
          && permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    return false
  }

  internal fun isNeedRequestAllFileAccessPermission(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
    val rp = getRequestedPermissions(context)
    val allFileAccessPermission = Manifest.permission.MANAGE_EXTERNAL_STORAGE
    return rp.contains(allFileAccessPermission)
  }
}