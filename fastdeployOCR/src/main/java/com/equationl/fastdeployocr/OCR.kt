package com.equationl.fastdeployocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Point
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import com.baidu.paddle.fastdeploy.LitePowerMode
import com.baidu.paddle.fastdeploy.RuntimeOption
import com.baidu.paddle.fastdeploy.pipeline.PPOCRBase
import com.baidu.paddle.fastdeploy.pipeline.PPOCRv2
import com.baidu.paddle.fastdeploy.pipeline.PPOCRv3
import com.baidu.paddle.fastdeploy.vision.OCRResult
import com.baidu.paddle.fastdeploy.vision.ocr.Classifier
import com.baidu.paddle.fastdeploy.vision.ocr.DBDetector
import com.baidu.paddle.fastdeploy.vision.ocr.Recognizer
import com.equationl.fastdeployocr.bean.OcrResult
import com.equationl.fastdeployocr.bean.OcrResultModel
import com.equationl.fastdeployocr.callback.OcrInitCallback
import com.equationl.fastdeployocr.callback.OcrRunCallback
import com.equationl.fastdeployocr.exception.InitModelException
import com.equationl.fastdeployocr.exception.NoResultException
import com.equationl.fastdeployocr.exception.RunModelException
import com.equationl.fastdeployocr.paddle.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


class OCR(private val context: Context) {

    private lateinit var predictor: PPOCRBase

    private var realModelPath = File(context.externalCacheDir, "OCRModels/")

    private var modelPath: String = "models"
    private var labelPath: String? = "ppocr_keys_v1.txt"
    private var cpuThreadNum: Int = 4
    private var cpuPowerMode: LitePowerMode = LitePowerMode.LITE_POWER_HIGH
    private var scoreThreshold: Float = 0.1f
    private var detModelFileName: String = "det"
    private var recModelFileName: String = "rec"
    private var clsModelFileName: String = "cls"
    private var runType: RunType = RunType.All
    private var recRunPrecision: RunPrecision = RunPrecision.LiteFp16
    private var detRunPrecision: RunPrecision = RunPrecision.LiteFp16
    private var clsRunPrecision: RunPrecision = RunPrecision.LiteFp16
    private var modelVersion: ModelVersion = ModelVersion.V3
    private var isDrwwTextPositionBox: Boolean = false

    /**
     * 获取 PPOCR 示例
     *
     * @return [PPOCRv3] or [PPOCRv2]，如果尚未初始化，返回 null
     * */
    fun getPredictor(): PPOCRBase? {
        if (this::predictor.isInitialized) {
            return predictor
        }

        return null
    }

    /**
     *
     * 初始化模型（同步）
     *
     * @param config 配置信息
     *
     * */
    @WorkerThread
    fun initModelSync(config: OcrConfig? = null): Result<Boolean>{
        if (config != null) {
            setConfig(config)
        }

        return try {
            checkFile()
            Result.success(
                initModel().initialized()
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /**
     * 初始化模型（异步）
     *
     * @param config 配置信息
     * @param callback 初始化回调
     * */
    @MainThread
    fun initModel(config: OcrConfig? = null, callback: OcrInitCallback) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch(Dispatchers.IO) {
            initModelSync(config).fold(
                {
                    coroutineScope.launch(Dispatchers.Main) {
                        if (it) {
                            callback.onSuccess()
                        }
                        else {
                            callback.onFail(InitModelException("未知错误"))
                        }
                    }
                },
                {
                    coroutineScope.launch(Dispatchers.Main) {
                        callback.onFail(it)
                    }
                }
            )
        }
    }


    /**
     * 开始运行识别模型（同步）
     *
     * @param bitmap 欲识别的图片
     * */
    @WorkerThread
    fun runSync(bitmap: Bitmap): Result<OcrResult> {
        return if (!this::predictor.isInitialized || !predictor.initialized()) {
            Result.failure(RunModelException("请先加载模型！"))
        } else {
            runModel(bitmap)
        }
    }

    /**
     * 开始运行识别模型（异步）
     *
     * @param bitmap 欲识别的图片
     * @param callback 识别结果回调
     * */
    @MainThread
    fun run(bitmap: Bitmap, callback: OcrRunCallback) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch(Dispatchers.IO) {
            runSync(bitmap).fold(
                {
                    coroutineScope.launch(Dispatchers.Main) {
                        callback.onSuccess(it)
                    }
                },
                {
                    coroutineScope.launch(Dispatchers.Main) {
                        callback.onFail(it)
                    }
                })
        }
    }

    /**
     * 释放模型
     * */
    fun releaseModel() {
        if (this::predictor.isInitialized) {
            predictor.release()
        }
    }


    private fun checkFile() {
        if (modelPath[0] == '/') {
            realModelPath = File(modelPath)
        }
        else {
            copyFile()
        }

        File(realModelPath, "$detModelFileName.pdmodel").let {
            if (!it.exists()) throw NoSuchFileException(it, reason = "Load Det Model Fail")
        }
        File(realModelPath, "$detModelFileName.pdiparams").let {
            if (!it.exists()) throw NoSuchFileException(it, reason = "Load Det Model Fail")
        }
        File(realModelPath, "$recModelFileName.pdmodel").let {
            if (!it.exists()) throw NoSuchFileException(it, reason = "Load Rec Model Fail")
        }
        File(realModelPath, "$recModelFileName.pdiparams").let {
            if (!it.exists()) throw NoSuchFileException(it, reason = "Load Rec Model Fail")
        }
        if (runType == RunType.All) { // 只有运行模式是全部运行时才需要检查cls模型
            File(realModelPath, "$clsModelFileName.pdmodel").let {
                if (!it.exists()) throw NoSuchFileException(it, reason = "Load Cls Model Fail")
            }
            File(realModelPath, "$clsModelFileName.pdiparams").let {
                if (!it.exists()) throw NoSuchFileException(it, reason = "Load Cls Model Fail")
            }
        }
        File(realModelPath, "$labelPath").let {
            if (!it.exists()) throw NoSuchFileException(it, reason = "Load Labels Fail")
        }

    }

    private fun copyFile() {
        Utils.copyDirectoryFromAssets(
            context,
            modelPath,
            realModelPath.absolutePath
        )
    }

    private fun initModel(): PPOCRBase {

        val detModelFile = "${realModelPath.absolutePath}/$detModelFileName.pdmodel"
        val detParamsFile = "${realModelPath.absolutePath}/$detModelFileName.pdiparams"
        val clsModelFile = "${realModelPath.absolutePath}/$clsModelFileName.pdmodel"
        val clsParamsFile = "${realModelPath.absolutePath}/$clsModelFileName.pdiparams"
        val recModelFile = "${realModelPath.absolutePath}/$recModelFileName.pdmodel"
        val recParamsFile = "${realModelPath.absolutePath}/$recModelFileName.pdiparams"
        val recLabelFilePath = "${realModelPath.absolutePath}/$labelPath"

        val detOption =  RuntimeOption()
        val clsOption =  RuntimeOption()
        val recOption =  RuntimeOption()

        detOption.setCpuThreadNum(cpuThreadNum)
        clsOption.setCpuThreadNum(cpuThreadNum)
        recOption.setCpuThreadNum(cpuThreadNum)
        detOption.setLitePowerMode(cpuPowerMode)
        clsOption.setLitePowerMode(cpuPowerMode)
        recOption.setLitePowerMode(cpuPowerMode)

        if (detRunPrecision == RunPrecision.LiteFp16) detOption.enableLiteFp16() else detOption.enableLiteInt8()
        if (clsRunPrecision == RunPrecision.LiteFp16) clsOption.enableLiteFp16() else clsOption.enableLiteInt8()
        if (recRunPrecision == RunPrecision.LiteFp16) recOption.enableLiteFp16() else recOption.enableLiteInt8()


        val detModel = DBDetector(detModelFile, detParamsFile, detOption)
        val clsModel = Classifier(clsModelFile, clsParamsFile, clsOption)
        val recModel = Recognizer(recModelFile, recParamsFile, recLabelFilePath, recOption)

        predictor = if (modelVersion == ModelVersion.V3) {
            when (runType) {
                RunType.All -> PPOCRv3(detModel, clsModel, recModel)
                RunType.WithDet -> PPOCRv3(detModel, recModel)
            }
        } else {
            when (runType) {
                RunType.All -> PPOCRv2(detModel, clsModel, recModel)
                RunType.WithDet -> PPOCRv2(detModel, recModel)
            }
        }


        return predictor
    }

    @OptIn(ExperimentalTime::class)
    private fun runModel(bitmap: Bitmap): Result<OcrResult> {
        try {
            val rawResult: OCRResult
            val inferenceTime = measureTime {
                rawResult = predictor.predict(bitmap, isDrwwTextPositionBox)
            }.toLong(DurationUnit.MILLISECONDS)

            if (rawResult.mText.isNullOrEmpty()) {
                throw NoResultException("Rec result is empty")
            }

            val rawResultList = arrayListOf<OcrResultModel>()
            var simpleText = ""
            rawResult.mText.forEachIndexed { index: Int, s: String? ->
                if (s != null) {
                    simpleText += "$s\n"
                    val box = rawResult.mBoxes.getOrNull(index)
                    val point = if (box == null) {
                        listOf()
                    }
                    else {
                        listOf(
                            Point(box[0], box[1]),
                            Point(box[2], box[3]),
                            Point(box[4], box[5]),
                            Point(box[6], box[7]),
                        )
                    }

                    val clsLabel = rawResult.mClsLabels.getOrNull(index)

                    rawResultList.add(
                        OcrResultModel(
                            point,
                            s,
                            rawResult.mRecScores.getOrElse(index) { -1f },
                            if (clsLabel == null) "-1" else if (clsLabel == 0) "0" else "180",
                            rawResult.mClsScores.getOrElse(index) { -1f }
                        )
                    )
                }
            }

            val ocrResult = OcrResult(
                simpleText,
                inferenceTime,
                bitmap,
                rawResultList,
                rawResult
            )

            return Result.success(ocrResult)
        } catch (e: Throwable) {
            return Result.failure(e)
        }
    }

    private fun setConfig(config: OcrConfig) {
        this.modelPath = config.modelPath
        this.labelPath = config.labelPath
        this.cpuThreadNum = config.cpuThreadNum
        this.cpuPowerMode = config.cpuPowerMode
        this.scoreThreshold = config.scoreThreshold
        this.detModelFileName = config.detModelFileName
        this.recModelFileName = config.recModelFileName
        this.clsModelFileName = config.clsModelFileName
        this.runType = config.runType
        this.recRunPrecision = config.recRunPrecision
        this.detRunPrecision = config.detRunPrecision
        this.clsRunPrecision = config.clsRunPrecision
        this.modelVersion = config.modelVersion
        this.isDrwwTextPositionBox = config.isDrwwTextPositionBox
    }

}