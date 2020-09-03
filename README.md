# Permission

## Android权限适配

### 功能介绍：

* 适配6.0以上运行时权限

* 适配6.0以下存储权限，照相机权限，录音权限，其他返回已授权

* 适配8.0以上的安装包权限

* 可自定义引导权限弹框及设置弹框

* 可自定义引导权限提示语

* 引导设置权限并监听回调结果

### 使用方法：

1. 在root目录的build.gradle目录中添加
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

2. 在项目的build.gradle中添加依赖
```
    dependencies {
        implementation 'com.github.wshychbydh:permission:tag'
    }
```

3. 注意事项：

  - 如果编译的时候报重复的'META-INF/app_release.kotlin_module'时，在app的build.gradle文件的android下添加
```
    packagingOptions {
        exclude 'META-INF/app_release.kotlin_module'
    }

报其他类似的重复错误时，添加方式同上。
```


  - 该工具类只提供运行时权限申请，未提供7.0以上文件访问权限，需自行添加

  - 若sdk或targetApi小于23时，只会判断拍照/录音/存储权限，其他权限根据是否在Manifest中配置自动返回

  - 添加/移除自定义权限提示语：（自定义提示语优先级高于默认提示语）

```
    Permission.addTranslateText(permission, translate)     //动态添加权限(permission)对应的提示语(translate)
    Permission.removeTranslateText(permission, translate)  //移除动态添加的权限(permission)对应的提示语(translate)
    Permission.clearText()                                 //移除所有动态添加的权限
```

  - 有且仅有android.permission.REQUEST_INSTALL_PACKAGES 或 android.permission.INSTALL_PACKAGES(**系统**)时，才会弹出引导授权安装包提示框

  - 所有请求的权限都必须在Manifest中声明，否则一律返回false

  - 请求权限的过程中可能会导致Activity出现一次或多次paused -> resumed

### 示例：

```
    PermissionHelper.Builder(context)
         .permission(permission)                       //请求的单个权限
         .permissions(permissions)                     //请求的多个权限
         .rationale(rationale)                         //权限提示弹框（可选）
         .rationaleSetting(rationale)                  //引导设置弹框（可选）
         .rationaleInstallPackagesSetting(rationale)   //引导设置安装未知来源应用弹框（可选）
         .showRationaleSettingWhenDenied(boolean)      //是否弹设置框去引导授权（默认true）
         .showRationaleWhenRequest(boolean)            //是否弹框提示需要动态申请的权限（默认false）
         .showInstallRationaleWhenRequest(boolean)     //是否弹框提示安装APK需要申请的权限（默认false）
         .permissionCallback {                         //授权结果回调（必填，否则无回调）
           if (it) {
             // 请求权限成功
           } else {
             // 请求权限失败
           }
         }
         .deniedPermissionCallback { deniedPermissions-> //仅在授权失败时返回未授权的权限 （可选）
            //  返回授权失败的权限，可自行处理
         }
         .build()
         .request()
```
**推荐一、**
```
    PermissionChecker(
        Request.Builder(this)
            .permission(permission)                       //请求的单个权限
            .permissions(permissions)                     //请求的多个权限
            .rationale(rationale)                         //权限提示弹框（可选）
            .rationaleSetting(rationale)                  //引导设置弹框（可选）
            .rationaleInstallPackagesSetting(rationale)   //引导设置安装未知来源应用弹框（可选）
            .showRationaleSettingWhenDenied(boolean)      //是否弹设置框去引导授权（默认true）
            .showRationaleWhenRequest(boolean)            //是否弹框提示需要动态申请的权限（默认false）
            .showInstallRationaleWhenRequest(boolean)     //是否弹框提示安装APK需要申请的权限（默认false）
            .build()
    ).check(CoroutineScope) {
      //todo
      //根据返回的result.isSucceed()判断请求的权限是否成功
    }
```

**推荐二、**
```
    val result = permissionForResult(
          Request.Builder(this@PermissionTestDialogFragment)
              .permission(permission)                       //请求的单个权限
              .permissions(permissions)                     //请求的多个权限
              .rationale(rationale)                         //权限提示弹框（可选）
              .rationaleSetting(rationale)                  //引导设置弹框（可选）
              .rationaleInstallPackagesSetting(rationale)   //引导设置安装未知来源应用弹框（可选）
              .showRationaleSettingWhenDenied(boolean)      //是否弹设置框去引导授权（默认true）
              .showRationaleWhenRequest(boolean)            //是否弹框提示需要动态申请的权限（默认false）
              .showInstallRationaleWhenRequest(boolean)     //是否弹框提示安装APK需要申请的权限（默认false）
              .build()
    )
    //todo
    //根据返回的result.isSucceed()判断请求的权限是否成功
```
#####   
 
**Demo地址：(https://github.com/wshychbydh/SampleDemo)**    
    
##

###### **欢迎fork，更希望你能贡献commit.** (*￣︶￣)    

###### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/Permission.svg)](https://jitpack.io/#wshychbydh/Permission)