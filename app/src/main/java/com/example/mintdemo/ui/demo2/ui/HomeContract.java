package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.hjq.permissions.OnPermissionCallback;


public interface HomeContract {
    interface Modle{

    }
    interface View{
        void windowLogs(String src,int position);
        void pictureDisplay(Bitmap src,int position);
    }
    interface presenter{
        Boolean permissionRequest(Context context, OnPermissionCallback onPermissionCallback);
        CameraClient getImage(Context context);
    }
}
