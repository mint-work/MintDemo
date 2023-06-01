package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.text.InputType;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.NumberPicker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.mintdemo.R;
import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.Tool.NetworkTool;
import com.example.mintdemo.base.mvp.BasePresenter;
import com.example.mintdemo.ui.demo2.Interface.ProjectCallback;
import com.example.mintdemo.ui.demo2.constant.Network;
import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.Toaster;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

public class HomePresenter extends BasePresenter<HomeActivity, HomeModle, HomeContract.presenter> implements HomeContract.presenter {

    public CameraClient cameraClient;
    public Camera2Utils camera2Utils;

    public  final int NUMBERPLATEIDENTIFY = 1;//车牌识别
    public  final int QRCODEIDENTIFY = 2;     //二维码识别
    public  final int COLORIDENTIFY = 3;      //颜色识别
    public  final int SCRIPTIDENTIFY = 4;     //文字识别
    public  final int SHAPEIDENTIFY = 5;      //形状识别

    @Override
    public HomeModle getMold() {
        return new HomeModle(this);
    }

    @Override
    public HomeContract.presenter getContract() {
        return this;
    }

    /**
     * 权限申请
     */
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

    /**
     * 图片获取渠道初始化
     */
    @Override
    public void getInformation(Context context, TextureView textureView, Initialize initialize) {
        String[] items = {"网络摄像头", "自带相机"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context).
                setIcon(R.mipmap.ic_launcher).
                setTitle("请选择图像获取模式").
                setItems(items, (dialog1, which) -> {
                    new Handler(context.getMainLooper()).post(() -> initialize.callback(which));
                    if (which == 0) {
                        connectWebcam(context, initialize);
                    } else if (which == 1) {
                        cameraImage(context, textureView, initialize);
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    /**
     * 功能转发
     */
    @Override
    public void functionForwarding(int src, Bitmap bitmap, ProjectCallback projectCallback) {
        switch (src){
            case NUMBERPLATEIDENTIFY:
                m.numberPlateIdentify(bitmap, projectCallback);
                break;
            case QRCODEIDENTIFY:
                m.qrCodeIdentify(bitmap, projectCallback);
                break;
            case COLORIDENTIFY:
                m.colorIdentify();
                break;
            case SCRIPTIDENTIFY:
                m.scriptIdentify(bitmap, projectCallback);
                break;
            case SHAPEIDENTIFY:
                m.shapeIdentify();
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void LogForwarding(String src, int position) {
        getview().windowLogs(src,position);
    }

    /**
     * 连接网络摄像头
     */
    private void connectWebcam(Context context, Initialize initialize) {
        new XPopup.Builder(context).asInputConfirm("请输入网络摄像头ip地址", "请输入ip地址。",
                text -> {
                    Network.TEST_URL = text;
                    new XPopup.Builder(context).asInputConfirm("请输入网络摄像头端口", "请输入端口。",
                            text1 -> {
                                Network.TEST_PORT = Integer.parseInt(text1);
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
                                            new Handler(context.getMainLooper()).post(() -> initialize.callback(0));
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
                                } else {
                                    loadingPopupView.dismiss();
                                    Toaster.showShort("网络连接摄像头失败请检查网络是否连接");
                                    new Handler(context.getMainLooper()).post(() -> initialize.callback(-1));
                                }
                            })
                            .show();
                })
                .show();
    }

    /**
     * 开启相机获取图像
     */
    private void cameraImage(Context context, TextureView textureView, Initialize initialize) {
        LoadingPopupView loadingPopupView = new XPopup.Builder(context).asLoading("正在初始化相机");
        loadingPopupView.show();

        Camera2Utils1.getInstance().setContext(context)
                .setCameraID(0)
                .setPreviewSize(new Size(1920,1080))
                .initCamera(textureView);


//        camera2Utils = Camera2Utils.getInstance().
//                setContext(context).
//                setCameraID(1).
//                setPreviewSize(new Size(720, 720)).
//                setTextureView(textureView);
//        camera2Utils.initCamera2(new Camera2Utils.StartupResults() {
//            @Override
//            public void onSuccess() {
//                Toaster.showShort("相机初始化成功");
//                loadingPopupView.dismiss();
//                new Handler(context.getMainLooper()).post(() -> initialize.callback(2));
//            }
//
//            @Override
//            public void onFailure(String e) {
//                Toaster.showShort("相机初始化失败" + e);
//                loadingPopupView.dismiss();
//                camera2Utils.closeCamera();
//                new Handler(context.getMainLooper()).post(() -> initialize.callback(-1));
//            }
//        });
    }

    /**
     * 硬件初始化回调
     */
    interface Initialize {
        void callback(int src);
    }
}
