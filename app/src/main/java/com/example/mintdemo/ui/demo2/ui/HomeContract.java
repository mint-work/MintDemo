package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;

public interface HomeContract {
    interface Modle{

    }
    interface View{
        void windowLogs(String src,int position);
        void pictureDisplay(Bitmap src,int position);
        void getImage(Bitmap bitmap);
    }
    interface presenter{
        Boolean permissionRequest(Context context);
        void getImage();
    }
}
