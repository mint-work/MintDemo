package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.util.Size;
import android.view.TextureView;

import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.Tool.NetworkTool;
import com.example.mintdemo.base.mvp.BasePresenter;
import com.example.mintdemo.ui.demo2.constant.Network;
import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;

public class HomePresenter extends BasePresenter<HomeActivity, HomeModle, HomeContract.presenter> implements HomeContract.presenter {

    public CameraClient cameraClient;
    public Camera2Utils camera2Utils;
    public int mark = 0;

    @Override
    public HomeModle getMold() {
        return new HomeModle(this);
    }

    @Override
    public HomeContract.presenter getContract() {
        return this;
    }


    @Override
    public Boolean permissionRequest(Context context, OnPermissionCallback onPermissionCallback) {
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
    public void getImage(Context context, TextureView textureView) {
        //判断是否有网路摄像头
        if (NetworkTool.isWifi(context)) {//连接网络摄像头
            cameraClient = new CameraClient(Network.TEST_URL, Network.TEST_PORT);
            cameraClient.start(new CameraClient.Outcome() {
                @Override
                public void succeed(Bitmap bitmap) {
                    mark = 1;
                    getview().pictureDisplay(bitmap, 0);//展示图片
                }

                @Override
                public void Fail(String e) {
                    //Toaster.showShort("网络连接摄像头失败:" + e + "\n已经为打开手机摄像头");
                    camera2Utils = cameraImage(context, textureView);
                }
            });
            return;
        }
        Toaster.showShort("网络连接摄像头失败");
        camera2Utils = cameraImage(context, textureView);
    }

    /**
     * 开启相机获取图像
     */
    private Camera2Utils cameraImage(Context context, TextureView textureView) {
        Camera2Utils camera2Utils = Camera2Utils.getInstance().
                setContext(context).
                setCameraID(1).
                setPreviewSize(new Size(720, 720)).
                setTextureView(textureView);
        new Thread(() -> {
            try {
                camera2Utils.initCamera2(new Camera2Utils.StartupResults() {
                    @Override
                    public void onSuccess() {
                        mark = 2;
                    }
                    @Override
                    public void onFailure(CameraAccessException e) {
                        mark = 0;
                        Toaster.showShort("手机摄像头打开失败请检查手机硬件是否支持");
                    }
                });
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }).start();
        return camera2Utils;

    }

}
