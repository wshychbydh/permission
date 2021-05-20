package com.eye.cool.permission

import android.content.Context
import androidx.fragment.app.Fragment
import com.eye.cool.permission.checker.Request
import com.eye.cool.permission.checker.Result
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Created by ycb on 2020/8/31
 */
object Permission {
  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      context: Context,
      permission: String
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.build(context) { permission(permission) }
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      context: Context,
      permissions: Array<String>
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.build(context) { permissions(permissions) }
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      context: Context,
      permissions: Collection<String>
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.Builder(context).permissions(permissions).build()
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      fragment: Fragment,
      permission: String
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.build(fragment) { permission(permission) }
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      fragment: Fragment,
      permissions: Array<String>
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.build(fragment) { permissions(permissions) }
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      fragment: Fragment,
      permissions: Collection<String>
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(
        Request.build(fragment) { permissions(permissions) }
    ).check(it)
  }

  /**
   * @return run on ui-thread
   */
  suspend fun requestForResult(
      builder: Request
  ) = suspendCancellableCoroutine<Result> {
    PermissionChecker(builder).check(it)
  }
}