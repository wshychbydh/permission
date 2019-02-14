# Permission
## Android6.0权限适配

#### 使用方法：

1、在root目录的build.gradle目录中添加
```
    allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
    }
```

2、在项目的build.gradle中添加依赖
```
    dependencies {
            implementation 'com.github.wshychbydh:Permission:Tag'
    }
```

3、PermissionHelper.Builder创建实例。

4、设置相应参数，如需要申请的permissions，自定义弹框rationale。

5、设置权限回调permissionCallback。

#### 示例：

```
    PermissionHelper.Builder(context)
         .permission(permission)           // 设置需要请求的单个权限
         .permissions(permissions)         // 设置需要请求的权限组
         .rationale(rationale)             //需要定制的提示弹框（可以不设置）
         .rationaleSetting(rationale)      //需要定制的引导设置弹框（可以不设置）
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
