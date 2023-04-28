package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;

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
                // 申请多个权限
                //.permission(Permission.Group.CALENDAR)
                // 申请安装包权限
                //.permission(Permission.REQUEST_INSTALL_PACKAGES)
                // 申请悬浮窗权限
                //.permission(Permission.SYSTEM_ALERT_WINDOW)
                // 申请通知栏权限
                //.permission(Permission.NOTIFICATION_SERVICE)
                // 申请系统设置权限
                //.permission(Permission.WRITE_SETTINGS)
                // 设置权限请求拦截器
                //.interceptor(new PermissionInterceptor())
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
    public void CameraImaging() {

    }
}
