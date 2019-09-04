package com.eye.cool.permission

import android.content.Context

/**
 * Created by cool on 2018/4/16.
 */
interface Rationale {

  /**
   * Show rationale of permissions to user.
   *
   * @param context     context.
   * @param permissions show rationale permissions.
   */
  fun showRationale(
      context: Context,
      permissions: Array<String>,
      callback: ((result: Boolean) -> Unit)? = null
  )
}