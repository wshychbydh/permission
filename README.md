# Permission
## Android权限适配
###### 适配6.0以上和6.0以下，以及targetApi小于23

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
            implementation 'com.github.wshychbydh:permission:1.1.0'
    }
```

3、PermissionHelper.Builder创建实例。

4、设置相应参数，如需要申请的permissions，自定义弹框rationale。

5、设置权限回调permissionCallback。

6、sdk或targetApi小于23的时候，只能判断拍照，录音，存储权限，其他权限根据是否在Manifest中是否配置自动返回

#### 示例：

```
    PermissionHelper.Builder(context)
         .permission(permission)           //设置需要请求的单个权限
         .permissions(permissions)         //设置需要请求的权限组
         .rationale(rationale)             //需要定制的提示弹框（可以不设置）
         .rationaleSetting(rationale)      //需要定制的引导设置弹框（可以不设置）
         .showSettingWhenDenied(boolean)   //是否弹设置框去引导授权(默认true)
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
