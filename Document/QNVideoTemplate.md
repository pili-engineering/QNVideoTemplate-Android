<a id="1"></a>
# 1 概述

</br>

<a id="1.1"></a>


## 1.1 下载地址
[Android Demo下载地址](https://github.com/pili-engineering/QNVideoTemplate-Android)

</br>

<a id="2"></a>
# 2 阅读对象

本文档为技术文档，需要阅读者

- 具有基本的 Android 开发能力
- 准备接入七牛模板视频

</br>

<a id="3"></a>
# 3 总体设计
<a id="3.1"></a>
## 3.1 基本规则

为了方便理解和使用，对于 SDK 的接口设计，我们遵循了如下的原则：

- 每个接口类，均已 `QNVT` 开头
- 所有的参数配置类，均以 `QNVTXXXSetting` 命名
- 所有的回调接口类，均以 `QNVTXXXCallback` 命名

<a id="3.2"></a>

## 3.2 核心接口

核心接口类说明如下：

|类名 |功能 |备注 |
|-|-|-|
|QNVTAssetProperty |代表视频模板资源可替换的属性。 |当前可替换的属性为文字，图片和视频路径 |
|QNVTTemplateEditor |负责视频模板编辑，实时渲染展示模板内容，并可替换模板中的图片、视频、文字、背景音乐等属性，并支持导出视频 |内部会使用OpenGL进行渲染，会占用大量资源，进入后台请暂停，否则有可能造成crash。建议预览和导出不要同时执行，否则可能导致性能下降并且概览出现oom |

</br>

<a id="4"></a>
# 4 开发准备


<a id="4.1"></a>
## 4.1 设备及系统要求
- 设备要求： 至少2G内存
- 系统要求： Android 5.0 及以上系统, 支持opengles 2.0 及以上
  

<a id="4.2"></a>
## 4.2 开发环境配置

* Android Studio 开发工具，官方 [下载地址](http://developer.android.com/intl/zh-cn/sdk/index.html)
* Android 官方开发 SDK，官方 [下载地址](https://developer.android.com/intl/zh-cn/sdk/index.html#Other)

<a id="4.3"></a>
## 4.3 导入 SDK

视频的替换需要裁减、转码等前处理，这里推荐使用七牛推出的短视频SDK [Android Demo 以及 SDK 下载地址](https://github.com/pili-engineering/PLDroidShortVideo)

将 `qnvt.aar` 放入项目 `libs` 目录中，并修改 `build.gradle` 文件

双击打开您的工程根目录下的 `build.gradle`，确保加入了如下配置
```java
allprojects {
    repositories {
        jcenter()
        flatDir {
            dirs 'libs'
        }
    }
}
```

双击打开您的工程模块目录下的 `build.gradle`，确保已经添加了如下依赖，如下所示：

```java
dependencies {
    implementation(name: 'qnvt', ext: 'aar')
}
```

<a id="4.4"></a>

在 `app/src/main` 目录中的 `AndroidManifest.xml` 中增加如下 `uses-permission` 声明：

```xml
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />  
```

</br>

<a id="5"></a>
# 5 快速开始

### 设置鉴权文件路径
创建编辑器并鉴权，鉴权文件需要放在 `assets` 根目录下
``` java
QNVTTemplateEditor mEditor = new QNVTTemplateEditor(context, "license_name");
```

### 设置模板路径
模板解析比较耗时，推荐异步设置
``` java
mEditor.setTemplate("template path");
```

### 创建播放器并设置回调 
比较耗时，推荐异步创建
``` java
mEditor.createPlayer(previewWidth, previewHeight);
mEditor.setPreview(surface, surfaceWidth, surfaceHeight);
mEditor.setPlayCallback(new QNVTPlayerCallback());
mEditor.startPlay();
```

### 替换资源
``` java
QNVTAssetProperty assetProperty = mEditor.getReplaceableAssetList()[0];
// 替换文字资源
// assetProperty.text = "replace text";
// 替换 图片或视频资源
assetProperty.path = "image path";
assetProperty.type = QNVTAssetProperty.TYPE_IMAGE;
mEditor.updateAsset(assetProperty);
```

### 导出视频
``` java
QNVTVideoSetting setting = new QNVTVideoSetting(outputPath);
setting.dimension = new Size(width, height);
setting.bitrate = bitrate;
mEditor.startExport(setting, new QNVTExportCallback());
```

</br>

<a id="6"></a>
# 6 最佳实践

- 播放器以及导出的FPS若无特殊要求建议设为 0，使用模板中预置的FPS
- 替换的图片分辨率应裁剪到尽量小，可以加快渲染速度
- 替换的视频应提前转码，分辨率尽量小，GOP size尽量小（推荐 10 以内），可以加快渲染速度
- 替换的背景音乐应使用 AAC 编码的音频，非 AAC 编码（例如 MP3）有可能造成导出的视频无法在微信中分享

<a id=""></a>


<a id=""></a>
