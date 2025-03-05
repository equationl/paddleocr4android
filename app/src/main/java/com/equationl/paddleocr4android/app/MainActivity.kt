package com.equationl.paddleocr4android.app

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.equationl.paddleocr4android.CpuPowerMode
import com.equationl.paddleocr4android.OCR
import com.equationl.paddleocr4android.OcrConfig
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
            // 配置
            val config = OcrConfig()
            //config.labelPath = null
            //ch_PP-OCRv2
            //config.modelPath = "models/ch_PP-OCRv2" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
            //ch_PP-OCRv3
            config.modelPath = "models/ch_PP-OCRv3" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
            //config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android.app/files/models" // 使用 "/" 表示手机储存路径，测试时请将下载的三个模型放置于该目录下
            config.clsModelFilename = "cls.nb" // cls 模型文件名
            //ch_PP-OCRv2
            config.detModelFilename = "det_db.nb" // det 模型文件名
            //ch_PP-OCRv3
            config.detModelFilename = "ch_PP-OCRv3_det_opt.nb" // det 模型文件名
            //ch_PP-OCRv2
            //config.recModelFilename = "rec_crnn.nb" // rec 模型文件名
            //ch_PP-OCRv3
            config.recModelFilename = "ch_PP-OCRv3_rec_opt.nb" // rec 模型文件名

            // 运行全部模型
            // 请根据需要配置，三项全开识别率最高；如果只开识别几乎无法正确识别，至少需要搭配检测或分类其中之一
            // 也可单独运行 检测模型 获取文本位置
            config.isRunDet = true
            config.isRunCls = true
            config.isRunRec = true

            // 使用所有核心运行
            config.cpuPowerMode = CpuPowerMode.LITE_POWER_FULL

            // 绘制文本位置
            config.isDrwwTextPositionBox = true

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
                    resultText.text = "加载模型失败: $e"
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

                    var text = "识别文字=\n$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

                    val wordLabels = ocr.getWordLabels()
                    outputRawResult.forEachIndexed { index, ocrResultModel ->
                        // 文字索引（crResultModel.wordIndex）对应的文字可以从字典（wordLabels） 中获取
                        ocrResultModel.wordIndex.forEach {
                            Log.i(TAG, "onSuccess: text = ${wordLabels[it]}")
                        }
                        // 文字方向 ocrResultModel.clsLabel 可能为 "0" 或 "180"
                        text += "$index: 文字方向：${ocrResultModel.clsLabel}；文字方向置信度：${ocrResultModel.clsConfidence}；识别置信度 ${ocrResultModel.confidence}；文字索引位置 ${ocrResultModel.wordIndex}；文字位置：${ocrResultModel.points}\n"
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
