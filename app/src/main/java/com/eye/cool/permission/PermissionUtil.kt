package com.eye.cool.permission

import android.content.Context
import android.hardware.Camera
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Environment

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
      // setParameters is Used for meizu MX5.
      camera!!.parameters = camera!!.parameters
      true
    } catch (e: Exception) {
      false
    } finally {
      camera?.release()
    }
  }

  /**
   * Check external storage dir is write available
   */
  @JvmStatic
  fun isExternalDirAvailable(): Boolean {
    val file = Environment.getExternalStorageDirectory()
    return file != null && file.canWrite() && file.canRead()
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
      val bufferSize = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_DEFAULT, AudioFormat.ENCODING_PCM_16BIT)
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

  @JvmStatic
  fun openPermissionSetting(context: Context) {
    PermissionSetting(context).start()
  }
}