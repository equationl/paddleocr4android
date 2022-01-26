package com.equationl.paddleocr4android.Util.paddle;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static android.graphics.Color.*;

import androidx.annotation.Nullable;

import com.equationl.paddleocr4android.exception.InitModelException;
import com.equationl.paddleocr4android.exception.RunModelException;

public class Predictor {
    private static final String TAG = Predictor.class.getSimpleName();
    public boolean isLoaded = false;
    public int warmupIterNum = 1;
    public int inferIterNum = 1;
    public int cpuThreadNum = 4;
    public String cpuPowerMode = "LITE_POWER_HIGH";
    public String modelPath = "";
    public String modelName = "";
    protected OCRPredictorNative paddlePredictor = null;
    protected float inferenceTime = 0;
    // Only for object detection
    protected Vector<String> wordLabels = new Vector<String>();
    protected String inputColorFormat = "BGR";
    protected long[] inputShape = new long[]{1, 3, 960};
    protected float[] inputMean = new float[]{0.485f, 0.456f, 0.406f};
    protected float[] inputStd = new float[]{1.0f / 0.229f, 1.0f / 0.224f, 1.0f / 0.225f};
    protected float scoreThreshold = 0.1f;
    protected Bitmap inputImage = null;
    protected Bitmap outputImage = null;
    protected volatile String outputResult = "";
    protected float preprocessTime = 0;
    protected float postprocessTime = 0;
    protected ArrayList<OcrResultModel> rawResultArray;
    public String detModelFilename = "";
    public String recModelFilename = "";
    public String clsModelFilename = "";


    public Predictor() {
    }

    public boolean init(Context appCtx, String modelPath, @Nullable String labelPath) throws InitModelException {
        isLoaded = loadModel(appCtx, modelPath, cpuThreadNum, cpuPowerMode);
        /*if (!isLoaded) {
            return false;
        }*/
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }


    public boolean init(Context appCtx, String modelPath, @Nullable String labelPath, int cpuThreadNum, String cpuPowerMode,
                        String inputColorFormat,
                        long[] inputShape, float[] inputMean,
                        float[] inputStd, float scoreThreshold,
                        String[] modelFileNames) throws InitModelException {
        if (inputShape.length != 3) {
            throw new InitModelException("Size of input shape should be: 3");
        }
        if (inputMean.length != inputShape[1]) {
            throw new InitModelException("Size of input mean should be: " + Long.toString(inputShape[1]));
        }
        if (inputStd.length != inputShape[1]) {
            throw new InitModelException("Size of input std should be: " + Long.toString(inputShape[1]));
        }
        if (inputShape[0] != 1) {
            //FIXME 注意处理这个配置
            throw new InitModelException("Only one batch is supported in the image classification demo, you can use any batch size in " +
                    "your Apps!");
        }
        if (inputShape[1] != 1 && inputShape[1] != 3) {
            //FIXME 注意处理这个配置
            throw new InitModelException("Only one/three channels are supported in the image classification demo, you can use any " +
                    "channel size in your Apps!");
        }
        if (!inputColorFormat.equalsIgnoreCase("BGR")) {
            throw new InitModelException("Only  BGR color format is supported.");
        }
        if (modelFileNames.length != 3) {
            throw new InitModelException("Model file name list length should be 3");
        }

        this.detModelFilename = modelFileNames[0];
        this.recModelFilename = modelFileNames[1];
        this.clsModelFilename = modelFileNames[2];

        init(appCtx, modelPath, labelPath);

        this.inputColorFormat = inputColorFormat;
        this.inputShape = inputShape;
        this.inputMean = inputMean;
        this.inputStd = inputStd;
        this.scoreThreshold = scoreThreshold;
        return true;
    }

    protected boolean loadModel(Context appCtx, String modelPath, int cpuThreadNum, String cpuPowerMode) throws InitModelException {
        // Release model if exists
        releaseModel();

        // Load model
        if (modelPath.isEmpty()) {
            throw new InitModelException("modelPath is Empty!");
        }
        String realPath = modelPath;
        if (!modelPath.substring(0, 1).equals("/")) {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            throw new InitModelException("Get Model Real Path Fail");
        }

        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.cpuThreadNum = cpuThreadNum;
        config.detModelFilename = realPath + File.separator + this.detModelFilename;
        config.recModelFilename = realPath + File.separator + this.recModelFilename;
        config.clsModelFilename = realPath + File.separator + this.clsModelFilename;
        Log.e("Predictor", "model path" + config.detModelFilename + " ; " + config.recModelFilename + ";" + config.clsModelFilename);
        if (
                !new File(config.detModelFilename).canRead() ||
                !new File(config.detModelFilename).canRead() ||
                !new File(config.detModelFilename).canRead()
        ) {
            throw new InitModelException("无法读取模型，请检查模型路径是否正确且是否有权限读取！");
        }
        config.cpuPower = cpuPowerMode;
        paddlePredictor = new OCRPredictorNative(config);

        this.cpuThreadNum = cpuThreadNum;
        this.cpuPowerMode = cpuPowerMode;
        this.modelPath = realPath;
        this.modelName = realPath.substring(realPath.lastIndexOf("/") + 1);
        return true;
    }

    public void releaseModel() {
        if (paddlePredictor != null) {
            paddlePredictor.destory();
            paddlePredictor = null;
        }
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
        modelPath = "";
        modelName = "";
    }

    protected boolean loadLabel(Context appCtx, @Nullable String labelPath) throws InitModelException {
        wordLabels.clear();
        wordLabels.add("black");
        // Load word labels from file
        try {
            // 不使用 label
            if (labelPath == null) {
                wordLabels.clear();
                return true;
            }
            InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
            int available = assetsInputStream.available();
            byte[] lines = new byte[available];
            assetsInputStream.read(lines);
            assetsInputStream.close();
            String words = new String(lines);
            String[] contents = words.split("\n");
            for (String content : contents) {
                wordLabels.add(content);
            }
            Log.i(TAG, "Word label size: " + wordLabels.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitModelException("Load label Fail: " + e.getMessage());
        }
        return true;
    }


    public boolean runModel() throws RunModelException {
        if (inputImage == null || !isLoaded()) {
            throw new RunModelException("输入图片为空或模型为加载");
        }

        // Pre-process image, and feed input tensor with pre-processed data
        Bitmap scaleImage = Utils.resizeWithStep(inputImage, Long.valueOf(inputShape[2]).intValue(), 32);

        Date start = new Date();
        int channels = (int) inputShape[1];
        int width = scaleImage.getWidth();
        int height = scaleImage.getHeight();
        float[] inputData = new float[channels * width * height];
        if (channels == 3) {
            int[] channelIdx = null;
            if (inputColorFormat.equalsIgnoreCase("RGB")) {
                channelIdx = new int[]{0, 1, 2};
            } else if (inputColorFormat.equalsIgnoreCase("BGR")) {
                channelIdx = new int[]{2, 1, 0};
            } else {
                throw new RunModelException("Unknown color format " + inputColorFormat + ", only RGB and BGR color format is " +
                        "supported!");
            }

            int[] channelStride = new int[]{width * height, width * height * 2};
            int[] pixels=new int[width*height];
            scaleImage.getPixels(pixels,0,scaleImage.getWidth(),0,0,scaleImage.getWidth(),scaleImage.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];
                float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                        (float) blue(color) / 255.0f};
                inputData[i] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                inputData[i + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                inputData[i+ channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];
            }
        } else if (channels == 1) {
            int[] pixels=new int[width*height];
            scaleImage.getPixels(pixels,0,scaleImage.getWidth(),0,0,scaleImage.getWidth(),scaleImage.getHeight());
            for (int i = 0; i < pixels.length; i++) {
                int color = pixels[i];
                float gray = (float) (red(color) + green(color) + blue(color)) / 3.0f / 255.0f;
                inputData[i] = (gray - inputMean[0]) / inputStd[0];
            }
        } else {
            throw new RunModelException("Unsupported channel size " + Integer.toString(channels) + ",  only channel 1 and 3 is " +
                    "supported!");
        }
        float[] pixels = inputData;
        Log.i(TAG, "pixels " + pixels[0] + " " + pixels[1] + " " + pixels[2] + " " + pixels[3]
                + " " + pixels[pixels.length / 2] + " " + pixels[pixels.length / 2 + 1] + " " + pixels[pixels.length - 2] + " " + pixels[pixels.length - 1]);
        Date end = new Date();
        preprocessTime = (float) (end.getTime() - start.getTime());

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        }
        warmupIterNum = 0; // do not need warm
        // Run inference
        start = new Date();
        ArrayList<OcrResultModel> results = paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;

        results = postprocess(results);

        rawResultArray = (ArrayList<OcrResultModel>) results.clone();
        Log.i(TAG, "[stat] Preprocess Time: " + preprocessTime
                + " ; Inference Time: " + inferenceTime + " ;Box Size " + results.size());
        drawResults(results);

        return true;
    }


    public boolean isLoaded() {
        return paddlePredictor != null && isLoaded;
    }

    public String modelPath() {
        return modelPath;
    }

    public String modelName() {
        return modelName;
    }

    public int cpuThreadNum() {
        return cpuThreadNum;
    }

    public String cpuPowerMode() {
        return cpuPowerMode;
    }

    public float inferenceTime() {
        return inferenceTime;
    }

    public Bitmap inputImage() {
        return inputImage;
    }

    public Bitmap outputImage() {
        return outputImage;
    }

    public String outputResult() {
        return outputResult;
    }

    public ArrayList<OcrResultModel> outputRawResult() {
        return rawResultArray;
    }

    public float preprocessTime() {
        return preprocessTime;
    }

    public float postprocessTime() {
        return postprocessTime;
    }


    public void setInputImage(Bitmap image) {
        if (image == null) {
            return;
        }
        this.inputImage = image.copy(Bitmap.Config.ARGB_8888, true);
    }

    private ArrayList<OcrResultModel> postprocess(ArrayList<OcrResultModel> results) {
        for (OcrResultModel r : results) {
            StringBuffer word = new StringBuffer();
            for (int index : r.getWordIndex()) {
                if (index >= 0 && index < wordLabels.size()) {
                    word.append(wordLabels.get(index));
                } else {
                    Log.e(TAG, "Word index is not in label list:" + index);
                    word.append("×");
                }
            }
            r.setLabel(word.toString());
        }
        return results;
    }

    private void drawResults(ArrayList<OcrResultModel> results) {
        StringBuffer outputResultSb = new StringBuffer("");
        for (int i = 0; i < results.size(); i++) {
            OcrResultModel result = results.get(i);
            StringBuilder sb = new StringBuilder("");
            sb.append(result.getLabel());
            sb.append(" ").append(result.getConfidence());
            sb.append("; Points: ");
            for (Point p : result.getPoints()) {
                sb.append("(").append(p.x).append(",").append(p.y).append(") ");
            }
            Log.i(TAG, sb.toString()); // show LOG in Logcat panel
            outputResultSb//.append(i + 1).append(": ")
                            .append(result.getLabel()).append("\n");
        }
        outputResult = outputResultSb.toString();
        outputImage = inputImage;
        Canvas canvas = new Canvas(outputImage);
        Paint paintFillAlpha = new Paint();
        paintFillAlpha.setStyle(Paint.Style.FILL);
        paintFillAlpha.setColor(Color.parseColor("#3B85F5"));
        paintFillAlpha.setAlpha(50);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#3B85F5"));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        for (OcrResultModel result : results) {
            Path path = new Path();
            List<Point> points = result.getPoints();
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = points.size() - 1; i >= 0; i--) {
                Point p = points.get(i);
                path.lineTo(p.x, p.y);
            }
            canvas.drawPath(path, paint);
            canvas.drawPath(path, paintFillAlpha);
        }
    }

}
