package com.equationl.paddleocr4android.Util.paddle;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.Log;

import androidx.annotation.Nullable;

import com.equationl.paddleocr4android.exception.InitModelException;
import com.equationl.paddleocr4android.exception.RunModelException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

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
    protected int detLongSize = 960;
    protected float scoreThreshold = 0.1f;
    protected Bitmap inputImage = null;
    protected Bitmap outputImage = null;
    protected volatile String outputResult = "";
    protected ArrayList<OcrResultModel> rawResultArray;
    public String detModelFilename = "";
    public String recModelFilename = "";
    public String clsModelFilename = "";
    public Boolean isDrwwTextPositionBox = false;


    public Predictor() {
    }

    public boolean init(Context appCtx, String modelPath, @Nullable String labelPath, boolean useOpencl, int cpuThreadNum, String cpuPowerMode) throws InitModelException {
        isLoaded = loadModel(appCtx, modelPath, useOpencl ? 1:0, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }


    public boolean init(Context appCtx, String modelPath, @Nullable String labelPath, boolean useOpencl, int cpuThreadNum, String cpuPowerMode,
                        int detLongSize, float scoreThreshold, String[] modelFileNames, Boolean isDrwwTextPositionBox) throws InitModelException {
        this.detLongSize = detLongSize;
        this.scoreThreshold = scoreThreshold;
        this.detModelFilename = modelFileNames[0];
        this.recModelFilename = modelFileNames[1];
        this.clsModelFilename = modelFileNames[2];
        this.isDrwwTextPositionBox = isDrwwTextPositionBox;

        return init(appCtx, modelPath, labelPath, useOpencl, cpuThreadNum, cpuPowerMode);
    }

    protected boolean loadModel(Context appCtx, String modelPath, int useOpencl, int cpuThreadNum, String cpuPowerMode) throws InitModelException {
        // Release model if exists
        releaseModel();

        // Load model
        if (modelPath.isEmpty()) {
            throw new InitModelException("modelPath is Empty!");
        }

        String realPath = modelPath;
        if (modelPath.charAt(0) != '/') {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            throw new InitModelException("Get Model Real Path Fail");
        }

        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.useOpencl = useOpencl;
        config.cpuThreadNum = cpuThreadNum;
        config.cpuPower = cpuPowerMode;
        config.detModelFilename = realPath + File.separator + this.detModelFilename;
        config.recModelFilename = realPath + File.separator + this.recModelFilename;
        config.clsModelFilename = realPath + File.separator + this.clsModelFilename;
        Log.i("Predictor", "model path" + config.detModelFilename + " ; " + config.recModelFilename + ";" + config.clsModelFilename);
        if (!new File(config.detModelFilename).canRead() ||
                !new File(config.detModelFilename).canRead() ||
                !new File(config.detModelFilename).canRead()
        ) {
            throw new InitModelException("无法读取模型，请检查模型路径是否正确且是否有权限读取！");
        }
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
            wordLabels.add(" ");
            Log.i(TAG, "Word label size: " + wordLabels.size());
        } catch (Exception e) {
            e.printStackTrace();
            throw new InitModelException("Load label Fail: " + e.getMessage());
        }
        return true;
    }

    public Vector<String> getWordLabels() {
        return wordLabels;
    }


    public boolean runModel(boolean run_det, boolean run_cls, boolean run_rec) throws RunModelException {
        if (inputImage == null || !isLoaded()) {
            throw new RunModelException("输入图片为空或模型为加载");
        }

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.runImage(inputImage, detLongSize, run_det ? 1:0, run_cls ? 1:0, run_rec ? 1:0);
        }

        warmupIterNum = 0; // do not need warm
        // Run inference
        Date start = new Date();
        ArrayList<OcrResultModel> results = paddlePredictor.runImage(inputImage, detLongSize, run_det? 1:0, run_cls? 1:0, run_rec? 1:0);
        Date end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;

        results = postprocess(results);

        rawResultArray = (ArrayList<OcrResultModel>) results.clone();
        Log.i(TAG, "[stat] Inference Time: " + inferenceTime + " ;Box Size " + results.size());
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
            r.setClsLabel(r.getClsIdx() == 1 ? "180" : "0");
        }
        return results;
    }

    private void drawResults(ArrayList<OcrResultModel> results) {
        StringBuilder outputResultSb = new StringBuilder();
        for (int i = results.size()-1; i >= 0; i--) {
            OcrResultModel result = results.get(i);
            StringBuilder sb = new StringBuilder("");
            /*if(result.getPoints().size()>0){
                sb.append("Det: ");
                for (Point p : result.getPoints()) {
                    sb.append("(").append(p.x).append(",").append(p.y).append(") ");
                }
            }*/
            if(result.getLabel().length() > 0){
                //sb.append("\n Rec: ").append(result.getLabel());
                //sb.append(",").append(result.getConfidence());
                sb.append(result.getLabel());
            }
            /*if(result.getClsIdx()!=-1){
                sb.append(" Cls: ").append(result.getClsLabel());
                sb.append(",").append(result.getClsConfidence());
            }*/
            Log.i(TAG, sb.toString()); // show LOG in Logcat panel
            //outputResultSb.append(i + 1).append(": ").append(sb.toString()).append("\n");
            outputResultSb.append(sb).append("\n");
        }
        outputResult = outputResultSb.toString();
        outputImage = inputImage;

        if (isDrwwTextPositionBox) {
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
                if(points.size()==0){
                    continue;
                }
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

}
