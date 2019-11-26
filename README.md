# Permission

## Android权限适配

### 功能介绍：

1、适配6.0以上运行时权限

2、适配6.0以下文件权限，照相机权限，录音权限

3、适配targetApi小于23时文件、照相机、录音权限

4、权限组整合，可按组请求权限，分组见**Permission**

5、适配8.0以上安装包权限

6、可自定义引导权限弹框及设置弹框

7、可自定义引导权限提示内容

### 使用方法：

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
        implementation 'com.github.wshychbydh:permission:1.1.1'
    }
```

**注**：

1)、如果编译的时候报重复的'META-INF/app_release.kotlin_module'时，在app的build.gradle文件的android下添加
```
    packagingOptions {
        exclude 'META-INF/app_release.kotlin_module'
    }
```
报其他类似的重复错误时，添加方式同上。

2)、该工具类只提供运行时权限申请，未提供7.0以上文件访问权限，需自行添加

3、通过PermissionHelper.Builder创建实例。

4、设置相应参数，如需要申请的permissions，自定义弹框rationale等。

5、设置权限回调permissionCallback (必填，否则无回调)。

6、若sdk或targetApi小于23时，只会判断拍照/录音/存储权限，其他权限根据是否在Manifest中配置自动返回

7、添加/移除自定义权限提示内容：

```
    Permission.addTranslateText(permission, translate)     //动态添加权限(permission)对应的提示语(translate)
    Permission.removeTranslateText(permission, translate)  //移除动态添加的权限(permission)对应的提示语(translate)
    Permission.clearText()                                 //移除所有动态添加的权限
```

### 示例：

```
    PermissionHelper.Builder(context)
         .permission(permission)                       //请求的单个权限
         .permissions(permissions)                     //请求的多个权限
         .rationale(rationale)                         //权限提示弹框（可选）
         .rationaleSetting(rationale)                  //引导设置弹框（可选）
         .rationaleInstallPackagesSetting(rationale)   //引导设置安装未知来源应用弹框（可选）
         .showRationaleSettingWhenDenied(boolean)      //是否弹设置框去引导授权（默认true）
         .showRationaleWhenRequest(boolean)            //是否弹框提示需要申请的权限（默认false）
         .permissionAuthoriseCallback{                 //用于需要判断用户具体操作 （可选）
            if (it) {
                AuthoriseType.TYPE_GRANTED ->           //请求权限成功
                AuthoriseType.TYPE_DENIED ->            //请求权限失败
                AuthoriseType.TYPE_SETTING_CANCELED ->  //请求权限失败，引导设置点击取消
                AuthoriseType.TYPE_SETTING_ALLOW ->     //请求权限失败，引导设置点击设置（不能判断是否设置了对应权限，安装未知来源应用除外）
            }
         }
         .permissionCallback {                          //授权结果回调（可选）
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

**注**：permissionCallback和permissionAuthoriseCallback会在同时回调，按需设置其一即可；deniedPermissionCallback仅在授权失败时回调。

#### 联系方式 wshychbydh@gmail.com

[![](https://jitpack.io/v/wshychbydh/Permission.svg)](https://jitpack.io/#wshychbydh/Permission)
