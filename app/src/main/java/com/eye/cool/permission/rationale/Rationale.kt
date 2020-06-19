package com.eye.cool.permission.rationale

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
   * @param callback result, Only means which button was clicked
   */
  fun showRationale(
      context: Context,
      permissions: Array<String>,
      callback: (result: Boolean) -> Unit
  )
}