package com.equationl.paddleocr4android.callback

interface OcrInitCallback {
    fun onSuccess()
    fun onFail(e: Throwable)
}