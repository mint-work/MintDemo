package com.example.mintdemo.Tool.ocr;

import android.graphics.Bitmap;

/**
 * @创建时间 2022/9/17-16:00
 * @创建作者 Mint
 * @返回:
 * @注释: 图片处理对象
 */
public class ProcessingResults {
    private String simpleText; //处理过后得到的文字
    private float inferenceTime;//处理时间
    private Bitmap bitmap1;//处理过后的图片

    public ProcessingResults(String simpleText, float inferenceTime, Bitmap bitmap1) {
        this.simpleText = simpleText;
        this.inferenceTime = inferenceTime;
        this.bitmap1 = bitmap1;
    }

    public String getSimpleText() {
        return simpleText;
    }

    public void setSimpleText(String simpleText) {
        this.simpleText = simpleText;
    }

    public float getInferenceTime() {
        return inferenceTime;
    }

    public void setInferenceTime(float inferenceTime) {
        this.inferenceTime = inferenceTime;
    }

    public Bitmap getBitmap1() {
        return bitmap1;
    }

    public void setBitmap1(Bitmap bitmap1) {
        this.bitmap1 = bitmap1;
    }
}
