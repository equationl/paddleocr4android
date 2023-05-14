package com.equationl.fastdeployocr.callback

import com.equationl.fastdeployocr.bean.OcrResult


interface OcrRunCallback {
    fun onSuccess(result: OcrResult)
    fun onFail(e: Throwable)
}