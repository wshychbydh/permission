package com.eye.cool.permission.rationale

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Created by cool on 2020/9/16.
 */
internal class RationaleDelegate(
    private val rationale: Rationale
) {

  suspend fun request(
      scope: CoroutineScope,
      context: Context,
      permissions: Array<String>
  ) = suspendCoroutine<Boolean> {
    scope.launch(Dispatchers.Main) {
      rationale.showRationale(context, permissions) { result ->
        scope.launch(Dispatchers.Default) {
          if (result) {
            it.resume(true)
          } else {
            it.resume(false)
          }
        }
      }
    }
  }
}