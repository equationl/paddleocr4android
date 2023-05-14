package com.equationl.fastdeployocr

import com.baidu.paddle.fastdeploy.LitePowerMode
import com.equationl.fastdeployocr.bean.OcrResult


data class OcrConfig(
    /**
     * 模型根路径（默认为 assets 目录下的预装模型）
     *
     * 如果该值以 "/" 开头则认为是自定义路径，程序会直接从该路径加载模型；
     * 否则认为该路径传入的是 assets 下的文件，则将其复制到 cache 目录下后加载
     *
     * */
    var modelPath: String = "models",

    /**
     * label 字典文件
     * */
    var labelPath: String? = "ppocr_keys_v1.txt",

    /**
     * 使用的CPU线程数
     * */
    var cpuThreadNum: Int = 4,
    /**
     * cpu power model
     * */
    var cpuPowerMode: LitePowerMode = LitePowerMode.LITE_POWER_HIGH,

    /**
     * Score Threshold
     * */
    var scoreThreshold: Float = 0.1f,

    /**
     * 检测模型文件名(不包括后缀)
     * */
    var detModelFileName: String = "det",

    /**
     * 识别模型文件名(不包括后缀)
     * */
    var recModelFileName: String = "rec",

    /**
     * 分类模型文件名(不包括后缀)
     * */
    var clsModelFileName: String = "cls",

    /**
     * 运行类型，建议根据自身情况合理选择，大多数情况下选择 [RunType.All] 即可，
     * 但是某些情况下使用文本分类反而会导致识别率降低，所以可以选用 [RunType.WithDet]
     * */
    var runType: RunType = RunType.All,

    /**
     * 识别模型运行精度，一般模型选择 [RunPrecision.LiteFp16] 即可，如果是量化后的模型则选择 [RunPrecision.LiteInt8]
     * */
    var recRunPrecision: RunPrecision = RunPrecision.LiteFp16,

    /**
     * 检测模型运行精度，一般模型选择 [RunPrecision.LiteFp16] 即可，如果是量化后的模型则选择 [RunPrecision.LiteInt8]
     * */
    var detRunPrecision: RunPrecision = RunPrecision.LiteFp16,

    /**
     * 分类模型运行精度，一般模型选择 [RunPrecision.LiteFp16] 即可，如果是量化后的模型则选择 [RunPrecision.LiteInt8]
     * */
    var clsRunPrecision: RunPrecision = RunPrecision.LiteFp16,

    /**
     * OCR 模型版本
     * */
    var modelVersion: ModelVersion = ModelVersion.V3,

    /**
     * 是否绘制文字位置
     *
     * 如果为 true， [OcrResult.imgWithBox] 返回的是在输入 Bitmap 上绘制出文本位置框的 Bitmap
     *
     * 否则，[OcrResult.imgWithBox] 将会直接返回输入 Bitmap
     * */
    var isDrwwTextPositionBox: Boolean = false
)

enum class RunType {
    /** 同时运行文本检测、文本分类、文本识别 */
    All,
    /** 仅运行文本检测和文本识别 */
    WithDet,
}

/** 模型运行精度 */
enum class RunPrecision {
    LiteFp16,
    LiteInt8
}

enum class ModelVersion {
    V2,
    V3
}