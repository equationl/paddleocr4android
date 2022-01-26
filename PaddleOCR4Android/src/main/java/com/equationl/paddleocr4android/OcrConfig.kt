package com.equationl.paddleocr4android

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
     * 颜色模式
     * */
    var inputColorFormat: InputColorFormat = InputColorFormat.BGR,
    /**
     * Input Shape: (1,1,max_width_height) or (1,3,max_width_height)
     * */
    var inputShape: LongArray = longArrayOf(1,3,960),
    /**
     * Input Mean: (channel/255-mean)/std
     * */
    var inputMean: FloatArray = floatArrayOf(0.485F, 0.456F, 0.406F),
    /**
     * Input Std: (channel/255-mean)/std
     * */
    var inputStd: FloatArray = floatArrayOf(0.229F, 0.224F, 0.225F),
    /**
     * Score Threshold
     * */
    var scoreThreshold: Float = 0.1f,

    var detModelFilename: String = "ch_ppocr_mobile_v2.0_det_opt.nb",

    var recModelFilename: String = "ch_ppocr_mobile_v2.0_rec_opt.nb",

    var clsModelFilename: String = "ch_ppocr_mobile_v2.0_cls_opt.nb"
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

enum class InputColorFormat {
    BGR,
    RGB
}