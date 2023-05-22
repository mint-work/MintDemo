package com.example.mintdemo.ui.demo2.ui;


import android.graphics.Bitmap;

import com.example.mintdemo.Tool.ocr.PaddleOCR_Mint;
import com.example.mintdemo.Tool.ocr.ProcessingResults;
import com.example.mintdemo.base.mvp.BaseModle;
import com.example.mintdemo.ui.demo2.Interface.ProjectCallback;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.MultipleBarcodeReader;
import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.Plate;

import java.util.ArrayList;
import java.util.List;

public class HomeModle extends BaseModle<HomePresenter,HomeContract.Modle>implements HomeContract.Modle {


    public HomeModle(HomePresenter homePresenter) {
        super(homePresenter);
    }

    @Override
    public HomeContract.Modle getContract() {
        return this;
    }
    //车牌识别
    @Override
    public void numberPlateIdentify(Bitmap bitmap, ProjectCallback projectCallback) {
        Plate[] plates =  HyperLPR3.getInstance().plateRecognition(bitmap, HyperLPR3.CAMERA_ROTATION_0, HyperLPR3.STREAM_BGRA);
        List<String> src = new ArrayList<>();
        for (Plate plate: plates) {
            src.add(plate.getCode());
        }
        projectCallback.success(src);
    }
    //二维码识别
    @Override
    public void qrCodeIdentify(Bitmap bitmap, ProjectCallback projectCallback) {
        List<String> results = new ArrayList<>();
        MultiFormatReader reader = new MultiFormatReader();
        try {
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            MultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
            Result[] resultArray = multiReader.decodeMultiple(binaryBitmap);
            for (Result result : resultArray) {
                results.add(result.getText());
            }
            projectCallback.success(results);
        } catch (NotFoundException e) {
            // 如果没有找到二维码，则返回空列表
            projectCallback.fail(e.getMessage());
        }
    }

    @Override
    public void colorIdentify() {

    }

    @Override
    public void scriptIdentify(Bitmap bitmap, ProjectCallback projectCallback) {
        new PaddleOCR_Mint(p.getview()).ocrinit(bitmap, new PaddleOCR_Mint.Callback() {
            @Override
            public void succeed(ProcessingResults p) {
                projectCallback.success(p);
            }

            @Override
            public void fail(String e) {
                projectCallback.fail(e);
            }
        });
    }

    @Override
    public void shapeIdentify() {

    }
}
