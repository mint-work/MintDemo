package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.os.Build;
import android.os.Handler;
import android.util.Size;
import android.view.TextureView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.mintdemo.R;
import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.Tool.NetworkTool;
import com.example.mintdemo.base.mvp.BasePresenter;
import com.example.mintdemo.ui.demo2.constant.Network;
import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;

public class HomePresenter extends BasePresenter<HomeActivity, HomeModle, HomeContract.presenter> implements HomeContract.presenter {

    public CameraClient cameraClient;
    public Camera2Utils camera2Utils;


    @Override
    public HomeModle getMold() {
        return new HomeModle(this);
    }

    @Override
    public HomeContract.presenter getContract() {
        return this;
    }


    @Override
    public void permissionRequest(Context context, OnPermissionCallback onPermissionCallback) {
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
    }

    @Override
    public void getInformation(Context context, TextureView textureView,Initialize initialize) {
        String[] items = {"网络摄像头", "自带相机"};
        new AlertDialog.Builder(context).
                setIcon(R.mipmap.ic_launcher).
                setTitle("请选择图像获取模式").
                setItems(items, (dialog1, which) -> {
                    new Handler(context.getMainLooper()).post(() -> initialize.callback(which));
                    if (which==0){
                        connectWebcam(context,initialize);
                    }else if(which==1){
                        cameraImage(context, textureView,initialize);
                    }
        }).show();
    }

    /**
     * 连接网络摄像头
     */
    private void connectWebcam(Context context,Initialize initialize) {
        LoadingPopupView loadingPopupView = new XPopup.Builder(context).asLoading("正在连接网络摄像头");
        loadingPopupView.show();
        //判断是否有网路摄像头
        if (NetworkTool.isWifi(context)) {
            cameraClient = new CameraClient(Network.TEST_URL, Network.TEST_PORT);
            cameraClient.start(new CameraClient.Outcome() {
                @Override
                public void succeed() {
                    new Handler(context.getMainLooper()).post(() -> loadingPopupView.dismiss());
                    Toaster.showShort("网络摄像头连接成功");
                }

                @Override
                public void fail(String e) {
                    new Handler(context.getMainLooper()).post(() -> loadingPopupView.dismiss());
                    Toaster.showShort("网络连接摄像头失败:" + e);
                    new Handler(context.getMainLooper()).post(() -> initialize.callback(-1));
                }

                @Override
                public void output(Bitmap bitmap) {
                    getview().pictureDisplay(bitmap, 0);//展示图片
                }
            });
        }else {
            loadingPopupView.dismiss();
            Toaster.showShort("网络连接摄像头失败请检查网络是否连接");
            new Handler(context.getMainLooper()).post(() -> initialize.callback(-1));
        }
    }

    /**
     * 开启相机获取图像
     */
    public void cameraImage(Context context, TextureView textureView,Initialize initialize) {
        LoadingPopupView loadingPopupView = new XPopup.Builder(context).asLoading("正在初始化相机");
        loadingPopupView.show();
       camera2Utils = Camera2Utils.getInstance().
                setContext(context).
                setCameraID(0).
                setPreviewSize(new Size(720, 720)).
                setTextureView(textureView);
        camera2Utils.initCamera2(new Camera2Utils.StartupResults() {
            @Override
            public void onSuccess() {
                Toaster.showShort("相机初始化成功");
                loadingPopupView.dismiss();
            }
            @Override
            public void onFailure(String e) {
                Toaster.showShort("相机初始化失败"+e);
                loadingPopupView.dismiss();
                new Handler(context.getMainLooper()).post(() -> initialize.callback(-1));
            }
        });
    }
    /**
     * 硬件初始化回调
     */
    interface Initialize{
        void callback(int src);
    }


}
