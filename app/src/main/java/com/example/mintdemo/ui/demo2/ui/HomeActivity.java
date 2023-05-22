package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mintdemo.R;
import com.example.mintdemo.Tool.BitmapCut;
import com.example.mintdemo.base.mvp.BaseView;
import com.example.mintdemo.ui.demo2.adapter.ButtonsAdapter;
import com.example.mintdemo.ui.demo2.adapter.ButtonsData;
import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.ui.demo2.tool.CameraClient;
import com.example.mintdemo.ui.demo2.tool.FlexboxSpecingDecoration;
import com.example.mintdemo.ui.demo2.tool.MeasureUtil;
import com.example.mintdemo.ui.demo2.tool.Java.StringTool;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.toast.Toaster;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.XPopupImageLoader;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends BaseView<HomePresenter, HomeContract.View> implements HomeContract.View {
    public RecyclerView buttons;
    public ButtonsAdapter ButtonsAdapter;
    private List<ButtonsData> buttonsDatas = new ArrayList<>();
    private TextView systemLogs, cartLogs;
    private ImageView HomeImg, logImg1, logImg2, logImg3, logImg4;
    private TextureView textureView;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1001;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_home;
    }

    @Override
    public HomePresenter getPresenter() {
        return new HomePresenter();
    }

    @Override
    public HomeContract.View getContract() {
        return this;
    }

    @Override
    protected void initData() {
        buttonInitialization();//底部按钮初始化
        logWindowInitialized();//控件初始化
        //权限获取
        p.permissionRequest(mContext, new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                p.getInformation(mContext, textureView, src -> {
                    if (src == 0) {
                        textureView.setVisibility(View.VISIBLE);
                        HomeImg.setVisibility(View.GONE);
                    } else if (src == 1) {
                        textureView.setVisibility(View.GONE);
                        HomeImg.setVisibility(View.VISIBLE);
                    } else {
                        textureView.setVisibility(View.GONE);
                        HomeImg.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> Toaster.showShort("你可以点击图片按钮打开系统相机获取图片识别"), 2000);
                        ButtonsAdapter.addData(0, new ButtonsData("系统相册", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                intent.setType("image/*");
                                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
                            }
                        }));
                    }
                });
            }

            @Override
            public void onDenied(List<String> permissions, boolean never) {
                finish();
            }
        });
    }

    /**
     * 底部按钮初始化
     */
    private void buttonInitialization() {
        buttons = (RecyclerView) findViewById(R.id.buttons);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(this);
        linearLayoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
        buttons.setLayoutManager(linearLayoutManager3);
        buttons.addItemDecoration(new FlexboxSpecingDecoration(MeasureUtil.dpToPx(mContext, 5)));
        buttonsDatas.add(new ButtonsData("全自动", item -> { //添加全自动按钮
            Toaster.showShort("全自动按钮被点击");
        }));
        buttonsDatas.add(new ButtonsData("拍照", item -> { //添加全自动按钮
            Toaster.showShort("拍照按钮被点击");
        }));
        ButtonsAdapter = new ButtonsAdapter(HomeActivity.this, R.layout.item_button, buttonsDatas);
        buttons.setAdapter(ButtonsAdapter);
    }

    /**
     * 控件初始化
     */
    private void logWindowInitialized() {
        systemLogs = (TextView) findViewById(R.id.systemLogs);
        cartLogs = (TextView) findViewById(R.id.cartLogs);
        HomeImg = (ImageView) findViewById(R.id.preview);
        logImg1 = (ImageView) findViewById(R.id.logimg1);
        logImg2 = (ImageView) findViewById(R.id.logimg2);
        logImg3 = (ImageView) findViewById(R.id.logimg3);
        logImg4 = (ImageView) findViewById(R.id.logimg4);
        View.OnClickListener onClickListener = v -> {
            Drawable drawable = ((ImageView) v).getDrawable();
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                new XPopup.Builder(mContext)
                        .asImageViewer((ImageView) v, bitmap, new XPopupImageLoader() {
                            @Override
                            public void loadImage(int position, @NonNull Object uri, @NonNull ImageView imageView) {
                                imageView.setImageBitmap((Bitmap) uri);
                            }

                            @Override
                            public File getImageFile(@NonNull Context context, @NonNull Object uri) {
                                return null;
                            }
                        })
                        .show();
            }
        };
        HomeImg.setOnClickListener(onClickListener);
        logImg1.setOnClickListener(onClickListener);
        logImg2.setOnClickListener(onClickListener);
        logImg3.setOnClickListener(onClickListener);
        logImg4.setOnClickListener(onClickListener);


        textureView = (TextureView) findViewById(R.id.textureView);

    }

    /**
     * 窗口日志刷新
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void windowLogs(String src, int position) {
        String s1 = null;
        if (position == 0) {
            s1 = systemLogs.getText().toString();
        } else if (position == 1) {
            s1 = cartLogs.getText().toString();
        } else {
            return;
        }
        List<String> codeVoList = StringTool.getCodeVoList(s1);
        if (codeVoList.size() > 5) {
            List<String> s = new ArrayList<>();
            s.add(codeVoList.get(0));
            for (int i = 0; i < 4; i++) {
                s.add(codeVoList.get(codeVoList.size() - 4 + i));
            }
            codeVoList = s;
        }
        String test = codeVoList.get(0);
        for (int i = 0; i < codeVoList.size() - 1; i++) {
            test = test + "\n" + codeVoList.get(i + 1);
        }
        if (position == 0) {
            systemLogs.setText(test + "\n" + src);
        } else if (position == 1) {
            cartLogs.setText(test + "\n" + src);
        }
    }

    /**
     * 图片展示刷新
     */
    @Override
    public void pictureDisplay(Bitmap src, int position) {
        if (position == 0) {
            src = BitmapCut.ImageCrop(src, HomeImg.getHeight(), HomeImg.getWidth());
            src = BitmapCut.getRoundedCornerBitmap(src, 10);
            HomeImg.setImageBitmap(src);
        } else {
            src = BitmapCut.ImageCrop(src, logImg1.getHeight(), logImg1.getWidth());
            src = BitmapCut.getRoundedCornerBitmap(src, 15);
            if (position == 1) {
                logImg1.setImageBitmap(src);
            } else if (position == 2) {
                logImg2.setImageBitmap(src);
            } else if (position == 3) {
                logImg3.setImageBitmap(src);
            } else if (position == 4) {
                logImg4.setImageBitmap(src);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = BitmapCut.getBitmapFromUri(mContext, data.getData());
            pictureDisplay(bitmap, 0);
        }
    }

}
