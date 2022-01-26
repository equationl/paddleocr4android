package com.equationl.paddleocr4android.callback

import com.equationl.paddleocr4android.bean.OcrResult

interface OcrRunCallback {
    fun onSuccess(result: OcrResult)
    fun onFail(e: Throwable)
}