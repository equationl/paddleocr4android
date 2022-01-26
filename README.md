[![](https://jitpack.io/v/equationl/paddleocr4android.svg)](https://jitpack.io/#equationl/paddleocr4android)

# 简介

该库是对 [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) 中的 [android_demo](https://github.com/PaddlePaddle/PaddleOCR/tree/release/2.4/deploy/android_demo) 进行二次封装的库。
对于只想体验或者快速上手使用的安卓开发者，该库对官方 demo 进行了简单的封装，使其可以直接上手使用，而无需关心 PaddleOCR 的实现，亦无需进行繁琐的配置。

基于 *Paddle-Lite* 部署

截图：

![截图](/doc/screenshot1.jpg)

# 使用方法

## 1.下载依赖

首先，根据你使用的 Gradle 版本在项目级 *build.gradle* 或 *settings.gradle* 文件添加 jitpack 远程仓库：

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

然后在 Module 级 *build.gradle* 文件添加依赖：

```gradle
dependencies {
    implementation 'com.github.equationl:paddleocr4android:v1.0.3'
}
```

**请将版本号替换为最新版本**

## 2.下载模型

模型下载地址: [Paddle-Lite模型](https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.4/doc/doc_ch/models_list.md#Paddle-Lite模型)

更多模型请自行前往 PaddleOCR 官网下载。

需要注意的是，由于是基于 *Paddle-Lite* 部署，所以只能使用 `*.nb` 格式的slim模型。

请将下载好的三个模型：

```
xx_cls.nb
xx_det.nb
xx_rec.nb
```

放置到手机任意目录或项目的 **assets** 的目录下。

*建议测试时直接放到 assets 中，避免放到手机目录中时由于权限问题而无法读取模型*

*正式使用时请自行实现模型的下载，建议不要直接将模型放在 assets 中打包进安装包*

## 3.加载模型

```kotlin
// 配置
val config = OcrConfig()
//config.labelPath = null
//config.modelPath = "models/ocr_v2_for_cpu" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android/files/models" // 使用 "/" 表示手机储存路径，测试时请将下载的三个模型放置于该目录下
//config.clsModelFilename = "xx_cls.nb" // cls 模型文件名
//config.detModelFilename = "xx_det.nb" // det 模型文件名
//config.recModelFilename = "xx_rec.nb" // rec 模型文件名

// 1.同步初始化
/*ocr.initModelSync(config).fold(
              {
                  if (it) {
                      Log.i(TAG, "onCreate: init success")
                  }
              },
              {
                  it.printStackTrace()
              }
          )*/

// 2.异步初始化
resultText.text = "开始加载模型"
ocr.initModel(config, object : OcrInitCallback {
    override fun onSuccess() {
        resultText.text = "加载模型成功"
        Log.i(TAG, "onSuccess: 初始化成功")
    }

    override fun onFail(e: Throwable) {
        resultText.text = "加载模型失败: $e"
        Log.e(TAG, "onFail: 初始化失败", e)
    }

})
```

更多配置请自行查看 [OcrConfig.kt](/PaddleOCR4Android/src/main/java/com/equationl/paddleocr4android)

## 4.开始使用

```kotlin
// 1.同步识别
/*val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test2)
          ocr.runSync(bitmap)

          val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.test3)
          ocr.runSync(bitmap2)*/

// 2.异步识别
resultText.text = "开始识别"
val bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.test4)
ocr.run(bitmap3, object : OcrRunCallback {
    override fun onSuccess(result: OcrResult) {
        val simpleText = result.simpleText
        val imgWithBox = result.imgWithBox
        val inferenceTime = result.inferenceTime
        val outputRawResult = result.outputRawResult

        var text = "识别文字=$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

        outputRawResult.forEachIndexed { index, ocrResultModel ->
            text += "$index: 置信度 ${ocrResultModel.confidence}；文字索引位置 ${ocrResultModel.wordIndex}；文字位置：${ocrResultModel.points} \n"
        }

        resultText.text = text
        resultImg.setImageBitmap(imgWithBox)
    }

    override fun onFail(e: Throwable) {
        resultText.text = "识别失败：$e"
        Log.e(TAG, "onFail: 识别失败！", e)
    }

})
```

## 5.其他

有任何问题请先尝试 demo 或阅读源码，如果无法解决请提 issue
