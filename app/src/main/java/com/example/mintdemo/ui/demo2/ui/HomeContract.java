package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.example.mintdemo.ui.demo2.Interface.ProjectCallback;
import com.hjq.permissions.OnPermissionCallback;


public interface HomeContract {
    interface Modle{
        //车牌识别
        void numberPlateIdentify(Bitmap bitmap, ProjectCallback projectCallback);
        //二维码识别
        void qrCodeIdentify(Bitmap bitmap, ProjectCallback projectCallback);
        //颜色识别
        void colorIdentify();
        //文字识别
        void scriptIdentify(Bitmap bitmap, ProjectCallback projectCallback);
        //形状识别
        void shapeIdentify();
    }
    interface View{
        void windowLogs(String src,int position);
        void pictureDisplay(Bitmap src,int position);
    }
    interface presenter{
        //权限申请
        void permissionRequest(Context context, OnPermissionCallback onPermissionCallback);
        //获取手机信息
        void getInformation(Context context, TextureView textureView, HomePresenter.Initialize initialize);
        //功能转发
        void functionForwarding(int src, Bitmap bitmap, ProjectCallback projectCallback);
        //日志刷新转发
        void LogForwarding(String src, int position);
    }
}
