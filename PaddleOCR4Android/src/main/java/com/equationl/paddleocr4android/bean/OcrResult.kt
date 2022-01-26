package com.equationl.paddleocr4android.bean

import android.graphics.Bitmap
import com.equationl.paddleocr4android.Util.paddle.OcrResultModel
import java.util.ArrayList

data class OcrResult(
    /**
     * 简单识别结果
     * */
    val simpleText: String,
    /**
    * 识别耗时
    * */
    val inferenceTime: Float,
    /**
     * 框选出文字位置的图像
     * */
    val imgWithBox: Bitmap,
    /**
     * 原始识别结果
     * */
    val outputRawResult: ArrayList<OcrResultModel>,
    )
