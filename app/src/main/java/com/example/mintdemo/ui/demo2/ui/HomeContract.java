package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.TextureView;

import com.hjq.permissions.OnPermissionCallback;

import java.util.Map;


public interface HomeContract {
    interface Modle{

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
    }
}
