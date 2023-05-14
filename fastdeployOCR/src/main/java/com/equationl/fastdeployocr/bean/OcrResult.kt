package com.equationl.fastdeployocr.bean

import android.graphics.Bitmap
import android.graphics.Point

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
)

data class OcrResultModel (
    /**识别到的文本位置框，四个 [Point] 分别表示文本框的 左下，右下，右上，左上 点*/
    val points: List<Point>,
    /**识别到的文字*/
    val label: String,
    /**识别到的文字的置信度*/
    val confidence: Float,
    /**检测到的文字方向分类*/
    val cls_label: String,
    /**检测到的文字方向分类置信度*/
    val cls_confidenceL: Float
)