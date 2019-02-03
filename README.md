# Permission
## Android6.0权限适配

#### 使用方法：

1、PermissionHelper.Buidler创建实例。

2、设置相应参数，如需要申请的permissions，自定义弹框rationale。

3、设置权限回调permissionCallback。

#### 示例：

```
 PermissionHelper.Builder(params.wrapper!!.context())

         .permissions(Permission.STORAGE) // 设置需要请求的权限
         .rationale()           //需要定制的提示弹框（可以不设置）
         .rationaleSetting()    //需要定制的引导设置弹框（可以不设置）
         .permissionCallback {
           if (it) {
             // 请求权限成功
           } else {
             // 请求权限失败
           }
         }.build()
         .request()
```

[![](https://jitpack.io/v/wshychbydh/Permission.svg)](https://jitpack.io/#wshychbydh/Permission)
