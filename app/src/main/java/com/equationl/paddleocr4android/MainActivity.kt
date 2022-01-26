package com.equationl.paddleocr4android

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.equationl.paddleocr4android.bean.OcrResult
import com.equationl.paddleocr4android.callback.OcrInitCallback
import com.equationl.paddleocr4android.callback.OcrRunCallback

class MainActivity : AppCompatActivity() {
    private val TAG = "el, Main"

    private lateinit var ocr: OCR

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ocr = OCR(this)

        val initBtn = findViewById<Button>(R.id.init_model)
        val startBtn = findViewById<Button>(R.id.start_model)
        val resultImg = findViewById<ImageView>(R.id.result_img)
        val resultText = findViewById<TextView>(R.id.result_text)

        initBtn.setOnClickListener {
            // 配置配置文件
            val config = OcrConfig()
            //config.labelPath = null
            config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android/files/models"
            //config.clsModelFilename = "cls.nb"
            //config.detModelFilename = "det.nb"
            //config.recModelFilename = "rec.nb"

            // 1.同步初始化
            /*ocr.initModelSync(config).fold(
                {
                    if (it) {
                        Log.i(TAG, "onCreate: init success")
                    }
                },
                {
                    it.printStackTrace()
                }
            )*/

            // 2.异步初始化
            resultText.text = "开始加载模型"
            ocr.initModel(config, object : OcrInitCallback {
                override fun onSuccess() {
                    resultText.text = "加载模型成功"
                    Log.i(TAG, "onSuccess: 初始化成功")
                }

                override fun onFail(e: Throwable) {
                    resultText.text = "加载模型失败"
                    Log.e(TAG, "onFail: 初始化失败", e)
                }

            })
        }

        startBtn.setOnClickListener {
            // 1.同步识别
            /*val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test2)
            ocr.runSync(bitmap)

            val bitmap2 = BitmapFactory.decodeResource(resources, R.drawable.test3)
            ocr.runSync(bitmap2)*/

            // 2.异步识别
            resultText.text = "开始识别"
            val bitmap3 = BitmapFactory.decodeResource(resources, R.drawable.test4)
            ocr.run(bitmap3, object : OcrRunCallback {
                override fun onSuccess(result: OcrResult) {
                    val simpleText = result.simpleText
                    val imgWithBox = result.imgWithBox
                    val inferenceTime = result.inferenceTime
                    val outputRawResult = result.outputRawResult

                    var text = "识别文字=$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

                    outputRawResult.forEachIndexed { index, ocrResultModel ->
                        text += "$index: 置信度 ${ocrResultModel.confidence}；文字索引位置 ${ocrResultModel.wordIndex}；文字位置：${ocrResultModel.points} \n"
                    }

                    resultText.text = text
                    resultImg.setImageBitmap(imgWithBox)
                }

                override fun onFail(e: Throwable) {
                    resultText.text = "识别失败：$e"
                    Log.e(TAG, "onFail: 识别失败！", e)
                }

            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 释放
        ocr.releaseModel()
    }
}