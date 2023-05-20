package com.equationl.fastdeployocr.bean

import android.graphics.Bitmap
import android.graphics.Point
import com.baidu.paddle.fastdeploy.vision.OCRResult

data class OcrResult(
    /**
     * 简单识别结果
     * */
    val simpleText: String,
    /**
     * 识别耗时
     * */
    val inferenceTime: Long,
    /**
     * 框选出文字位置的图像
     * */
    val imgWithBox: Bitmap,
    /**
     * 格式化后的原始识别结果
     * */
    val outputRawResult: ArrayList<OcrResultModel>,
    /**
     * fastDeploy 返回的最原始的结果
     * */
    val rawOCRResult: OCRResult
)

data class OcrResultModel (
    /**识别到的文本位置框，四个 [Point] 分别表示文本框的 左下，右下，右上，左上 点*/
    val points: List<Point>,
    /**识别到的文字*/
    val label: String,
    /**识别到的文字的置信度*/
    val confidence: Float,
    /**检测到的文字方向 0° 或 180° */
    val cls_label: String,
    /**检测到的文字方向分类置信度*/
    val cls_confidenceL: Float
)