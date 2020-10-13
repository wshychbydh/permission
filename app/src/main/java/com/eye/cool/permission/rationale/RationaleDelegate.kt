package com.eye.cool.permission.rationale

import android.content.Context
import com.eye.cool.permission.support.complete
import kotlinx.coroutines.*
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
  ) = suspendCancellableCoroutine<Boolean> {
    scope.launch(Dispatchers.Main) {
      rationale.showRationale(context, permissions) { result ->
        scope.launch(Dispatchers.Default) {
          it.complete(result)
        }
      }
    }
  }
}