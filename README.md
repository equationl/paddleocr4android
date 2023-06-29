[![](https://jitpack.io/v/equationl/paddleocr4android.svg)](https://jitpack.io/#equationl/paddleocr4android)

# 简介

该库是使用 [fastDeploy](https://github.com/PaddlePaddle/PaddleOCR/tree/dygraph/deploy/fastdeploy/android) 部署在安卓端使用 [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) 的二次封装库。

对于只想体验或者快速上手使用的安卓开发者，该库对其进行了简单的封装，使其可以直接上手使用，而无需关心 PaddleOCR 的实现，亦无需进行繁琐的配置。

基于 *fastDeploy* 部署

截图：

![截图](/doc/screenshot2.jpg)

# 注意

本库基于 *fastDeploy* 部署，同时支持 Paddle 原始模型和量化模型（.pdmodel、pdiparams），并且支持 PPOCRv2 和 PPOCRv3。

但是使用本库可能会大幅增加安装包体积，如果对安装包体积敏感，推荐使用 *[Paddle-Lite](./doc/paddlelite.md)* 部署，但是使用 *Paddle-Lite* 部署将只支持 OPT 后的模型（.nb），并且目前尚未支持 PPOCRv3。


# 使用方法

因为 Paddle 模型比较大，所以没有集成模型到 demo （[fastdeploydemo](./fastdeploydemo)）中，如果想要运行 demo，您需要自行下载模型后放入 [./fastdeploydemo/src/main/assets](../fastdeploydemo/src/main/assets) 中:

![截图](/doc/screenshot3.png)

将模型放入后运行即可。

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
    implementation 'com.github.equationl.paddleocr4android:fastdeplyocr:v1.2.6'
    
}
```

## 2.下载模型

模型下载地址: [PP-OCR系列模型列表](https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.6/doc/doc_ch/models_list.md)

当然，你也可以使用自己训练的模型。

需要注意的是，文本检测、文本识别、文本方向分类 模型各有两个文件：\*.pdmodel、\*.pdiparams

请将下载好的三个模型，六个文件：

```
xx_cls.pdmodel
xx_cls.pdiparams
xx_det.pdmodel
xx_det.pdiparams
xx_rec.pdmodel
xx_rec.pdiparams
```

放置到手机任意目录或项目的 **assets** 目录下。

三个模型分别为：

| 文件名                             | 模型名称     | 说明       |
|---------------------------------|----------|----------|
| xx_cls.pdmodel、xx_cls.pdiparams | 文本方向分类模型 | 用于文本方向分类 |
| xx_det.pdmodel、xx_det.pdiparams | 检测模型     | 用于检测文本位置 |
| xx_rec.pdmodel、xx_rec.pdiparams | 识别模型     | 用于识别文本内容 |

*建议测试时直接放到 assets 中，避免放到手机目录中时由于权限问题而无法读取模型*

*由于模型文件较大，正式使用时请自行实现模型的下载，建议不要直接将模型放在 assets 中打包进安装包*

## 3.加载模型

```kotlin
// 配置
val config = OcrConfig()
//config.labelPath = null


config.modelPath = "models" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
//config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android.app/files/models" // 使用 "/" 表示手机储存路径，测试时请将下载的三个模型放置于该目录下
config.clsModelFileName = "cls" // cls 模型文件名
config.detModelFileName = "det" // det 模型文件名
config.recModelFileName = "rec" // rec 模型文件名

// 运行全部模型
config.runType = RunType.All

// 使用所有核心运行
config.cpuPowerMode = LitePowerMode.LITE_POWER_FULL

// 绘制文本位置
config.isDrwwTextPositionBox = true

// 如果是原始模型，则使用 FP16 精度
config.recRunPrecision = RunPrecision.LiteFp16
config.detRunPrecision = RunPrecision.LiteFp16
config.clsRunPrecision = RunPrecision.LiteFp16

// 如果是量化模型则使用 int8 精度
//config.recRunPrecision = RunPrecision.LiteInt8
//config.detRunPrecision = RunPrecision.LiteInt8
//config.clsRunPrecision = RunPrecision.LiteInt8

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

更多配置请自行查看 [OcrConfig.kt](../fastdeployOCR/src/main/java/com/equationl/fastdeployocr/OcrConfig.kt)

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

        var text = "识别文字=\n$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

        outputRawResult.forEachIndexed { index, ocrResultModel ->
            // 文字方向 ocrResultModel.clsLabel 可能为 "0" 或 "180"
            text += "$index: 文字方向：${ocrResultModel.cls_label}；文字方向置信度：${ocrResultModel.cls_confidenceL}；识别置信度 ${ocrResultModel.confidence}；；文字位置：${ocrResultModel.points}\n"
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

### 混淆
如果需要开启混淆，请在你的项目 `proguard-rules.pro` 中添加：

```text
-keep class com.baidu.paddle.fastdeploy.** { *; }
```


## 6.问题解决


# 更新记录

**v1.2.8**

本次更新主要是优化了识别结果为空时的返回值：

- 当识别结果为空时返回 `Result.failure(NoResultException())`
- 当检测文本位置为空时 `OcrResultModel.points` 返回空列表
- 当识别置信度为空时 `OcrResultModel.confidence` 返回 `-1f`
- 当方向检测为空时 `OcrResultModel.cls_labelc` 返回 `-1`
- 当方向检测置信度为空时 `OcrResultModel.cls_confidenceL` 返回 `-1f`

**v1.2.7**

- 移除无用依赖

**v1.2.6**

- 更改返回结果中 `outputRawResult.cls_label` 为 "0" 或 "180"，分别表示检测到当前文本为 0° 或 180°
- 返回结果新增一个 `rawOCRResult` 表示 fastDeploy 返回的原始识别结果
- 修复运行模式为 `RunType.WithDet` 时也会检查 cls 模型的错误