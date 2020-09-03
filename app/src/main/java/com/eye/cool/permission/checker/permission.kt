package com.eye.cool.permission.checker

import android.content.Context
import androidx.fragment.app.Fragment
import com.eye.cool.permission.PermissionChecker
import kotlin.coroutines.suspendCoroutine

/**
 * Created by ycb on 2020/8/31
 */

suspend fun Context.permissionForResult(
    permission: String
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(this).permission(permission).build()
  ).check(it)
}

suspend fun Context.permissionForResult(
    permissions: Array<String>
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(this).permissions(permissions).build()
  ).check(it)
}

suspend fun Context.permissionForResult(
    permissions: Collection<String>
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(this).permissions(permissions).build()
  ).check(it)
}

suspend fun permissionForResult(
    fragment: Fragment,
    permission: String
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(fragment).permission(permission).build()
  ).check(it)
}

suspend fun permissionForResult(
    fragment: Fragment,
    permissions: Array<String>
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(fragment).permissions(permissions).build()
  ).check(it)
}

suspend fun permissionForResult(
    fragment: Fragment,
    permissions: Collection<String>
) = suspendCoroutine<Result> {
  PermissionChecker(
      Request.Builder(fragment).permissions(permissions).build()
  ).check(it)
}

suspend fun permissionForResult(
    builder: Request
) = suspendCoroutine<Result> {
  PermissionChecker(builder).check(it)
}