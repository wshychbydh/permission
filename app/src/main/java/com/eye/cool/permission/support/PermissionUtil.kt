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
      camera!!.parameters = camera!!.parameters
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
   * Get registered permissions in AndroidManifest
   */
  @JvmStatic
  fun getRequestedPermissions(context: Context): Array<String>? {
    try {
      val packageInfo: PackageInfo = context.packageManager
          .getPackageInfo(context.packageName, PackageManager.GET_PERMISSIONS)
      return packageInfo.requestedPermissions
    } catch (e: PackageManager.NameNotFoundException) {
      e.printStackTrace()
    }
    return null
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

  internal fun hasInstallPermissionOnly(permissions: Array<String>): Boolean {
    val installPermissions = Permission.INSTALL_PACKAGE
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
}