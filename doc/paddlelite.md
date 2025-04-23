[![](https://jitpack.io/v/equationl/paddleocr4android.svg)](https://jitpack.io/#equationl/paddleocr4android)

# 简介

该库是对 [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) 中的 [android_demo](https://github.com/PaddlePaddle/PaddleOCR/tree/release/2.5/deploy/android_demo) 进行二次封装的库。
对于只想体验或者快速上手使用的安卓开发者，该库对官方 demo 进行了简单的封装，使其可以直接上手使用，而无需关心 PaddleOCR 的实现，亦无需进行繁琐的配置。

基于 *Paddle-Lite* 部署

截图：

![截图](/doc/screenshot1.jpg)

# 注意

本库基于 *Paddle-Lite* 部署，因此只支持 Paddle-Lite 模型（格式 `.nb`） 目前 Paddle-Lite 版本为 2.14-rc，已支持 PPOCRv4 模型。

您也可以尝试使用 [fastDeployOCR](/README.md) 部署。

# 使用方法

无需进行任何配置，直接运行 demo （[app](../app)）即可体验。

如需集成至您自己的项目中，请按下述步骤进行：

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
    implementation 'com.github.equationl.paddleocr4android:paddleocr4android:v1.2.9'
    
    // 如果需要包含 OpenCL 预测库，请使用下面这个依赖
    //implementation 'com.github.equationl:paddleocr4android:v1.1.1-OpenCL'
}
```

## 2.下载模型

### 下载模型的渠道
1. 去官网下载

模型下载地址: [Paddle-Lite模型](https://paddlepaddle.github.io/PaddleOCR/latest/ppocr/model_list.html)

**注意：当前本库最新版本使用的 Paddle-Lite 版本为 2.14-rc，已支持 PPOCRv4 模型，为了保证良好的识别效果，请使用相同版本的 paddle-lite [opt 工具](https://www.paddlepaddle.org.cn/lite/v2.12/user_guides/model_optimize_tool.html)对模型进行量化**

更多模型请自行前往 PaddleOCR 官网下载。

2. 直接使用demo中的模型

demo 中已经集成了 ch_PP-OCRv2 模型（官方提供）和 ch_PP-OCRv4 模型（@[dwh](https://github.com/dengwhao)提供），可以直接复制使用

文件路径 /app/src/main/assets/models/ch_PP-OCRv2/ 以及 /app/src/main/assets/models/ch_PP-OCRv4/

需要注意的是，由于是基于 *Paddle-Lite* 部署，所以只能使用 `*.nb` 格式的slim模型。

请将下载好的三个模型：

```
xx_cls.nb
xx_det.nb
xx_rec.nb
```

放置到手机任意目录或项目的 **assets** 的目录下。

三个模型分别为：

| 文件名       | 模型名称     | 说明       |
|-----------|----------|----------|
| xx_cls.nb | 文本方向分类模型 | 用于文本方向分类 |
| xx_det.nb | 检测模型     | 用于检测文本位置 |
| xx_rec.nb | 识别模型     | 用于识别文本内容 |

*建议测试时直接放到 assets 中，避免放到手机目录中时由于权限问题而无法读取模型*

*正式使用时请自行实现模型的下载，建议不要直接将模型放在 assets 中打包进安装包*

## 3.加载模型

```kotlin
// 配置
val config = OcrConfig()
//config.labelPath = null


config.modelPath = "models/ch_PP-OCRv4" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
//config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android.app/files/models" // 使用 "/" 表示手机储存路径，测试时请将下载的三个模型放置于该目录下
config.clsModelFilename = "cls.nb" // cls 模型文件名
config.detModelFilename = "det.nb" // det 模型文件名
config.recModelFilename = "rec.nb" // rec 模型文件名

// 运行全部模型
// 请根据需要配置，三项全开识别率最高；如果只开识别几乎无法正确识别，至少需要搭配检测或分类其中之一
// 也可单独运行 检测模型 获取文本位置
config.isRunDet = true
config.isRunCls = true
config.isRunRec = true

// 使用所有核心运行
config.cpuPowerMode = CpuPowerMode.LITE_POWER_FULL

// 绘制文本位置
config.isDrwwTextPositionBox = true

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
val bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.test4)
ocr.run(bitmap3, object : OcrRunCallback {
    override fun onSuccess(result: OcrResult) {
        val simpleText = result.simpleText
        val imgWithBox = result.imgWithBox
        val inferenceTime = result.inferenceTime
        val outputRawResult = result.outputRawResult

        var text = "识别文字=\n$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

        val wordLabels = ocr.getWordLabels()
        outputRawResult.forEachIndexed { index, ocrResultModel ->
            // 文字索引（crResultModel.wordIndex）对应的文字可以从字典（wordLabels） 中获取
            ocrResultModel.wordIndex.forEach {
                Log.i(TAG, "onSuccess: text = ${wordLabels[it]}")
            }
            // 文字方向 ocrResultModel.clsLabel 可能为 "0" 或 "180"
            text += "$index: 文字方向：${ocrResultModel.clsLabel}；文字方向置信度：${ocrResultModel.clsConfidence}；识别置信度 ${ocrResultModel.confidence}；文字索引位置 ${ocrResultModel.wordIndex}；文字位置：${ocrResultModel.points}\n"
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

国内镜像地址： [paddleocr4android](https://gitee.com/equation/paddleocr4android)

## 6.问题解决
- 提示 ` Error: This model is not supported, because kernel for 'io_copy' is not supported by Paddle-Lite.`

该提示表示您使用的模型需要 OpenCL 预测库支持。解决办法：

使用包含 OpenCL 预测库的依赖。


# 更新记录
**v1.2.0**
- PaddleLite 更新至 v2.14-rc（支持 PPOCRv4, 感谢 @[dwh](https://github.com/dengwhao) PR ）

**v1.1.0**

- PaddleLite 更新至 v2.10
- 支持单独运行 分类、检测、识别 模型
- API 变动：

```kotlin
// 移除配置项：

    var inputColorFormat: InputColorFormat
    var inputShape: LongArray
    var inputMean: FloatArray
    var inputStd: FloatArray
    
// 增加配置项：
    
    /**
     * 是否运行检测模型
     * */
    var isRunDet: Boolean = true

    /**
     * 是否运行分类模型
     * */
    var isRunCls: Boolean = true

    /**
     * 是否运行识别模型
     * */
    var isRunRec: Boolean = true

    var isUseOpencl: Boolean = false

    /**
     * 是否绘制文字位置
     *
     * 如果为 true， [OcrResult.imgWithBox] 返回的是在输入 Bitmap 上绘制出文本位置框的 Bitmap
     *
     * 否则，[OcrResult.imgWithBox] 将会直接返回输入 Bitmap
     * */
    var isDrwwTextPositionBox: Boolean = false
```