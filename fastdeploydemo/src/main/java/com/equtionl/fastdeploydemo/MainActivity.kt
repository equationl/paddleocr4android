package com.equtionl.fastdeploydemo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.baidu.paddle.fastdeploy.LitePowerMode
import com.equationl.fastdeployocr.OCR
import com.equationl.fastdeployocr.OcrConfig
import com.equationl.fastdeployocr.RunPrecision
import com.equationl.fastdeployocr.RunType
import com.equationl.fastdeployocr.bean.OcrResult
import com.equationl.fastdeployocr.callback.OcrInitCallback
import com.equationl.fastdeployocr.callback.OcrRunCallback


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

            config.modelPath = "models" // 不使用 "/" 开头的路径表示安装包中 assets 目录下的文件，例如当前表示 assets/models/ocr_v2_for_cpu
            //config.modelPath = "/sdcard/Android/data/com.equationl.paddleocr4android.app/files/models" // 使用 "/" 表示手机储存路径，测试时请将下载的三个模型放置于该目录下
            config.clsModelFileName = "cls" // cls 模型文件名
            config.detModelFileName = "det" // det 模型文件名
            config.recModelFileName = "rec" // rec 模型文件名

            // 运行全部模型
            config.runType = RunType.All

            // 使用所有核心运行
            config.cpuPowerMode = LitePowerMode.LITE_POWER_FULL

            // 绘制文本位置
            config.isDrwwTextPositionBox = true

            // 如果是原始模型，则使用 FP16 精度
            config.recRunPrecision = RunPrecision.LiteFp16
            config.detRunPrecision = RunPrecision.LiteFp16
            config.clsRunPrecision = RunPrecision.LiteFp16

            // 如果是量化模型则使用 int8 精度
            //config.recRunPrecision = RunPrecision.LiteInt8
            //config.detRunPrecision = RunPrecision.LiteInt8
            //config.clsRunPrecision = RunPrecision.LiteInt8

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
            val bitmap3 = getBitmap(R.drawable.test4)

            ocr.run(bitmap3, object : OcrRunCallback {
                override fun onSuccess(result: OcrResult) {
                    val simpleText = result.simpleText
                    val imgWithBox = result.imgWithBox
                    val inferenceTime = result.inferenceTime
                    val outputRawResult = result.outputRawResult

                    var text = "识别文字=\n$simpleText\n识别时间=$inferenceTime ms\n更多信息=\n"

                    outputRawResult.forEachIndexed { index, ocrResultModel ->
                        // 文字方向 ocrResultModel.clsLabel 可能为 "0" 或 "180"
                        text += "$index: 文字方向：${ocrResultModel.cls_label}；文字方向置信度：${ocrResultModel.cls_confidenceL}；识别置信度 ${ocrResultModel.confidence}；；文字位置：${ocrResultModel.points}\n"
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

    /**
     * 自定义 option 获取 Drawable 中的图片，避免获取到的 Bitmap 被缩放
     *
     * 来自：https://blog.csdn.net/qiantanlong/article/details/87712906
     * */
    private fun getBitmap(resId: Int): Bitmap {
        val options = BitmapFactory.Options()
        val value = TypedValue()
        resources.openRawResource(resId, value)
        options.inTargetDensity = value.density
        options.inScaled = false //不缩放
        return BitmapFactory.decodeResource(resources, resId, options)
    }
}