package com.eye.cool.permission.rationale

import android.content.Context
import androidx.annotation.UiThread

/**
 * Created by cool on 2018/4/16.
 */
fun interface Rationale {

  /**
   * Show rationale of permissions to user.
   *
   * @param context     context.
   * @param permissions show rationale permissions.
   * @param callback true means accept, false otherwise.
   */
  @UiThread
  fun showRationale(
      context: Context,
      permissions: Array<String>,
      callback: (result: Boolean) -> Unit
  )
}