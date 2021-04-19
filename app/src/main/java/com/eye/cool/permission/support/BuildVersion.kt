package com.eye.cool.permission.support

import android.content.Context
import android.os.Build

/**
 * Created by cool on 2021/4/10.
 */
internal object BuildVersion {

  fun isBuildOverR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
  fun isBuildBelowR() = Build.VERSION.SDK_INT < Build.VERSION_CODES.R

  fun isBuildOverM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
  fun isTargetOverM(context: Context) =
      context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.M

  fun isBuildOverO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
  fun isBuildBelowO() = Build.VERSION.SDK_INT < Build.VERSION_CODES.O
}