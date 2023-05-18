package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;

import com.example.mintdemo.Tool.NetworkTool;
import com.example.mintdemo.base.mvp.BasePresenter;
import com.example.mintdemo.ui.demo2.constant.Network;
import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;


public class HomePresenter extends BasePresenter<HomeActivity,HomeModle,HomeContract.presenter> implements HomeContract.presenter {


    @Override
    public HomeModle getMold() {
        return new HomeModle(this);
    }

    @Override
    public HomeContract.presenter getContract() {
        return this;
    }


    @Override
    public Boolean permissionRequest(Context context , OnPermissionCallback onPermissionCallback) {
        String[] per = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            per = new String[]{
                    Permission.CAMERA,
                    Permission.READ_MEDIA_IMAGES,
                    Permission.READ_MEDIA_AUDIO,
                    Permission.MANAGE_EXTERNAL_STORAGE};
        } else {
            per = new String[]{
                    Permission.CAMERA,
                    Permission.READ_MEDIA_IMAGES,
                    Permission.READ_MEDIA_AUDIO};
        }
        XXPermissions.with(context)
                // 申请单个权限
                .permission(per)
                // 设置权限请求拦截器
                //.interceptor(new PermissionInterceptor()) //要记得继承
                // 设置不触发错误检测机制
                //.unchecked()
                .request(onPermissionCallback);//所有权限请求结束回调
        return null;
    }

    @Override
    public CameraClient getImage(Context context) {
        //判断是否有网路摄像头
        Boolean src = NetworkTool.isWifi(context);
        if(src){//连接网络摄像头
            CameraClient cameraClient = new CameraClient(Network.TEST_URL, Network.TEST_PORT);
            cameraClient.start(new CameraClient.Outcome() {
                @Override
                public void succeed(Bitmap bitmap) {
                    getview().pictureDisplay(bitmap,0);//展示图片
                }

                @Override
                public void Fail() {

                }
            });
            return cameraClient;
        }else {//打开系统相机

        }
        return null;
    }

}
