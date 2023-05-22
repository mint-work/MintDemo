package com.example.mintdemo.Tool.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.equationl.paddleocr4android.CpuPowerMode;
import com.equationl.paddleocr4android.OCR;
import com.equationl.paddleocr4android.OcrConfig;
import com.equationl.paddleocr4android.Util.paddle.OcrResultModel;
import com.equationl.paddleocr4android.bean.OcrResult;
import com.equationl.paddleocr4android.callback.OcrInitCallback;
import com.equationl.paddleocr4android.callback.OcrRunCallback;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import kotlin.jvm.internal.Intrinsics;
//  参考 https://github.com/equationl/paddleocr4android/tree/v1.1.0
public class PaddleOCR_Mint {
    private Context context;
    private Boolean Debug = false;
    private String TAG = "PaddleOCR_Mint";
    private OCR ocr;
    private OcrConfig config;

    public PaddleOCR_Mint(Context context) {
        this.context = context;
        ocr = new OCR(context);
        config = new OcrConfig();
    }
    /**
     * @创建时间 2022/9/17-16:00
     * @创建作者 Mint
     * @返回:
     * @注释: 初始化ocr库
     */
    public void ocrinit(Bitmap bitmap,Callback callback) {
        ocr.releaseModel();  // 释放模型
        config.setModelPath("ocr");
        config.setClsModelFilename("cls.nb"); // cls 模型
        config.setDetModelFilename("det_db.nb"); // det 模型
        config.setRecModelFilename("rec_crnn.nb"); // rec 模型
        config.setRunDet(true);
        config.setRunCls(true);
        config.setRunRec(true);
        config.setCpuPowerMode(CpuPowerMode.LITE_POWER_FULL);  // 使用所有核心运行
        config.setDrwwTextPositionBox(true);  // 绘制文本位置
        ocr.initModel(config, new OcrInitCallback() { //开始初始化
            public void onSuccess() {
                ocr.run(bitmap, new OcrRunCallback() {//开始识别
                    @Override
                    public void onSuccess(OcrResult result) {
                        String simpleText = result.getSimpleText();
                        Bitmap imgWithBox = result.getImgWithBox();
                        float inferenceTime = result.getInferenceTime();
                        List<OcrResultModel> outputRawResult = result.getOutputRawResult();
                        String text = "识别文字=\n" + simpleText + "\n识别时间=" + inferenceTime + " ms\n更多信息=\n";
                        List<String> wordLabels = ocr.getWordLabels();
                        for (int i = 0; i < outputRawResult.size(); i++) {
                            OcrResultModel ocrResultModel = outputRawResult.get(i);
                            for (int j = 0; j < ocrResultModel.getWordIndex().size(); j++) {
                                int index = ocrResultModel.getWordIndex().get(j);
                                Log.i(TAG, "onSuccess: text = " + wordLabels.get(index));
                            }
                            text += i + ": 文字方向：" + ocrResultModel.getClsLabel() + "；文字方向置信度：" + ocrResultModel.getClsConfidence() + "；识别置信度 " + ocrResultModel.getConfidence() + "；文字索引位置 " + ocrResultModel.getWordIndex() + "；文字位置：" + ocrResultModel.getPoints() + "\n";
                        }
                        Log.e(TAG, "识别成功日志："+text);
                        ProcessingResults p = new ProcessingResults(simpleText,inferenceTime,imgWithBox);
                        ocr.releaseModel();  // 释放模型
                        callback.succeed(p);
                    }

                    @Override
                    public void onFail(Throwable e) {
                        Log.e(TAG, "识别失败", e);
                        ocr.releaseModel();  // 释放模型
                        callback.fail("识别失败"+e.getMessage());
                    }
                });
            }

            public void onFail(@NotNull Throwable e) {
                Log.e(TAG, "模型初始化失败", e);
                ocr.releaseModel();  // 释放模型
                callback.fail("初始化失败"+e.getMessage());
            }
        });
    }

    /**
     * @创建时间 2022/9/17-16:00
     * @创建作者 Mint
     * @返回:
     * @注释: 初始化回调接口
     */
    public interface Callback{
        void succeed(ProcessingResults p);
        void fail(String e);
    }
}

