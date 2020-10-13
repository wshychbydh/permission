package com.eye.cool.permission.support

import kotlinx.coroutines.CancellableContinuation
import kotlin.coroutines.resume

/**
 * Created by cool on 2020/10/10.
 */
internal fun <T> CancellableContinuation<T>.complete(data: T) {
  if (this.isCompleted) return
  if (this.isActive) {
    this.resume(data)
  } else {
    this.cancel()
  }
}