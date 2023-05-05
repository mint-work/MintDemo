package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.mintdemo.base.mvp.BasePresenter;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;


import java.util.List;

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
    public Boolean permissionRequest(Context context) {
        XXPermissions.with(context)
                // 申请单个权限
                .permission(Permission.RECORD_AUDIO)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .permission(Permission.READ_EXTERNAL_STORAGE)
                .permission(Permission.CAMERA)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)

                // 设置权限请求拦截器
                //.interceptor(new PermissionInterceptor()) //要记得继承
                // 设置不触发错误检测机制
                //.unchecked()
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {

                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {

                    }
                });
        return null;
    }

    @Override
    public void getImage() {
        //判断是否有网路摄像头

        //打开系统相机

        //调用图片展示函数
        //getview().getImage();
    }
}
