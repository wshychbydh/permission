# [Permission](https://github.com/wshychbydh/permission)

## Android权限适配

### 功能介绍：

* 支持6.0以上运行时权限

* 支持6.0以下存储/相机/录音权限，其他根据AndroidManifest是否已注册返回

* 支持8.0以上的安装包权限

* 支持11.0所有文件访问权限请求

* 支持自定义引导权限弹框及设置弹框

* 支持自定义引导权限提示语

* 支持引导权限设置并监听回调结果

### 使用方法：

1、 在root目录的build.gradle目录中添加
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

2、 在项目的build.gradle中添加依赖
```
    dependencies {
        implementation 'com.github.wshychbydh:permission:Tag'
    }
```

3、 注意事项：

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

  - 所有请求的权限都必须在Manifest中声明，否则一律返回false

  - 请求权限的过程中可能会导致Activity出现一次或多次paused -> resumed

### 示例：

#### 构建
  [Request](app/src/main/java/com/eye/cool/permission/checker/Request.kt)请求参数
```kotlin
    val request = Request.Builder(context)
                    .permission(permission)                    //请求的单个权限
                    .permissions(permissions)                  //请求的多个权限
                    .rationale(rationale)                      //权限提示弹框（可选）
                    .rationaleSetting(rationale)               //引导设置弹框（可选）
                    .rationaleInstallPackageSetting(rationale) //引导设置安装未知来源应用弹框（可选）
                    .showRationaleSettingWhenDenied(boolean)   //是否弹设置框去引导授权（默认true）
                    .showRationaleWhenRequest(boolean)         //是否弹框提示需要动态申请的权限（默认false）
                    .showInstallRationaleWhenRequest(boolean)  //是否弹框提示安装APK需要申请的权限（默认false）
                    .requestInstallPackages()                  //8.0请求安装包权限（默认不请求）
                    .requestManageExternalStorage()            //11.0请求文件所有访问权限（默认不请求）
                    .build()
```

#### 使用

* [PermissionChecker](app/src/main/java/com/eye/cool/permission/PermissionChecker.kt)
```kotlin
    PermissionChecker(request).check {
      //todo
      //根据返回的result.isSucceed()判断请求的权限是否成功
    }
```

* [扩展](app/src/main/java/com/eye/cool/permission/checker/permission.kt) **（推荐）**
```kotlin
    val result = permissionForResult(request)
    //todo
    //根据返回的result.isSucceed()判断请求的权限是否成功
```


### **[Demo](https://github.com/wshychbydh/SampleDemo)**  
    
###### **欢迎fork，更希望你能贡献commit.** (*￣︶￣)    

###### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/Permission.svg)](https://jitpack.io/#wshychbydh/Permission)