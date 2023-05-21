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
        Boolean permissionRequest(Context context, OnPermissionCallback onPermissionCallback);
        void getImage(Context context, TextureView textureView);
    }
}
