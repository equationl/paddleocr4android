package com.equationl.paddleocr4android

import com.equationl.paddleocr4android.bean.OcrResult

data class OcrConfig(
    /**
     * 模型路径（默认为 assets 目录下的预装模型）
     *
     * 如果该值以 "/" 开头则认为是自定义路径，程序会直接从该路径加载模型；
     * 否则认为该路径传入的是 assets 下的文件，则将其复制到 cache 目录下后加载
     *
     * */
    var modelPath:String = "models/ocr_v2_for_cpu",
    /**
     * label 词组列表路径(程序返回的识别结果是该词组列表的索引)
     * */
    var labelPath: String? = "labels/ppocr_keys_v1.txt",
    /**
     * 使用的CPU线程数
     * */
    var cpuThreadNum: Int = 4,
    /**
     * cpu power model
     * */
    var cpuPowerMode: CpuPowerMode = CpuPowerMode.LITE_POWER_HIGH,
    /**
     * Score Threshold
     * */
    var scoreThreshold: Float = 0.1f,

    var detLongSize: Int = 960,

    /**
     * 检测模型文件名
     * */
    var detModelFilename: String = "ch_ppocr_mobile_v2.0_det_opt.nb",

    /**
     * 识别模型文件名
     * */
    var recModelFilename: String = "ch_ppocr_mobile_v2.0_rec_opt.nb",

    /**
     * 分类模型文件名
     * */
    var clsModelFilename: String = "ch_ppocr_mobile_v2.0_cls_opt.nb",

    /**
     * 是否运行检测模型
     * */
    var isRunDet: Boolean = true,

    /**
     * 是否运行分类模型
     * */
    var isRunCls: Boolean = true,

    /**
     * 是否运行识别模型
     * */
    var isRunRec: Boolean = true,

    var isUseOpencl: Boolean = false,

    /**
     * 是否绘制文字位置
     *
     * 如果为 true， [OcrResult.imgWithBox] 返回的是在输入 Bitmap 上绘制出文本位置框的 Bitmap
     *
     * 否则，[OcrResult.imgWithBox] 将会直接返回输入 Bitmap
     * */
    var isDrwwTextPositionBox: Boolean = false
)

enum class CpuPowerMode {
    /**
     * HIGH(only big cores)
     * */
    LITE_POWER_HIGH,
    /**
     * LOW(only LITTLE cores)
     * */
    LITE_POWER_LOW,
    /**
     * FULL(all cores)
     * */
    LITE_POWER_FULL,
    /**
     * NO_BIND(depends on system)
     * */
    LITE_POWER_NO_BIND,
    /**
     * RAND_HIGH
     * */
    LITE_POWER_RAND_HIGH,
    /**
     * RAND_LOW
     * */
    LITE_POWER_RAND_LOW
}