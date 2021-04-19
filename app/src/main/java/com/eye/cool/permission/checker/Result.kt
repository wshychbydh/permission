package com.eye.cool.permission.checker

import android.content.Context
import com.eye.cool.permission.support.PermissionTranslator

/**
 * Created by ycb on 2020/8/31
 */
data class Result(
    val request: List<String>,
    val denied: List<String>? = null
) {
  fun isSucceed(): Boolean = denied.isNullOrEmpty()

  fun isFailed(): Boolean = !denied.isNullOrEmpty()

  fun toDeniedText(context: Context): List<String> {
    if (denied.isNullOrEmpty()) return emptyList()
    return PermissionTranslator.toText(context, denied)
  }
}