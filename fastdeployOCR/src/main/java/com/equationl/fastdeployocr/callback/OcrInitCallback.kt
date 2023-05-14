package com.equationl.fastdeployocr.callback

interface OcrInitCallback {
    fun onSuccess()
    fun onFail(e: Throwable)
}