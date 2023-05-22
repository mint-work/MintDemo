package com.example.mintdemo.ui.demo2.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mintdemo.R;
import com.example.mintdemo.Tool.Pictures.BitmapCut;
import com.example.mintdemo.Tool.ocr.ProcessingResults;
import com.example.mintdemo.base.mvp.BaseView;
import com.example.mintdemo.ui.demo2.Interface.ProjectCallback;
import com.example.mintdemo.ui.demo2.adapter.ButtonsAdapter;
import com.example.mintdemo.ui.demo2.adapter.ButtonsData;
import com.example.mintdemo.Tool.Camera.Camera2Utils;
import com.example.mintdemo.ui.demo2.tool.FlexboxSpecingDecoration;
import com.example.mintdemo.ui.demo2.tool.MeasureUtil;
import com.example.mintdemo.ui.demo2.tool.Java.StringTool;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.toast.Toaster;
import com.hyperai.hyperlpr3.HyperLPR3;
import com.hyperai.hyperlpr3.bean.HyperLPRParameter;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.XPopupImageLoader;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseView<HomePresenter, HomeContract.View> implements HomeContract.View {
    public RecyclerView buttons;
    public ButtonsAdapter ButtonsAdapter;
    private List<ButtonsData> buttonsDatas = new ArrayList<>();
    private TextView systemLogs, cartLogs;
    private ImageView HomeImg, logImg1, logImg2, logImg3, logImg4;
    private TextureView textureView;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1001;
    private Bitmap bitmap;

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
        component();//组件初始化
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
                        ButtonsAdapter.addData(1,new ButtonsData("拍照", v -> {
                            p.camera2Utils.takePicture(new Camera2Utils.OnTakePictureCallback() {
                                @Override
                                public void onSuccess(Bitmap bitmap) {
                                    pictureDisplay(bitmap, 1);
                                }

                                @Override
                                public void onFailure(String e) {
                                    Toaster.showShort("拍照失败");
                                }
                            });
                        }));
                    } else {
                        textureView.setVisibility(View.GONE);
                        HomeImg.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> Toaster.showShort("你可以点击图片按钮打开系统相机获取图片识别"), 1000);
                        ButtonsAdapter.addData(0, new ButtonsData("系统相册", v -> {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
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
        buttonsDatas.add(new ButtonsData("全自动", item -> {
            Toaster.showShort("全自动按钮被点击");
        }));
        buttonsDatas.add(new ButtonsData("车牌识别", item -> {
            p.functionForwarding(p.NUMBERPLATEIDENTIFY, bitmap, new ProjectCallback() {
                @Override
                public void success(Object src) {
                    List<String> strings = (List<String>)src;
                    Toaster.showShort("车牌内容："+strings.get(0));
                }
                @Override
                public void fail(String s) {

                }
            });
        }));
        buttonsDatas.add(new ButtonsData("二维码识别", item -> {
            p.functionForwarding(p.QRCODEIDENTIFY, bitmap, new ProjectCallback() {
                @Override
                public void success(Object src) {
                    List<String> results = (List<String>) src;
                    Toaster.showShort("二维码内容：" + results.size());
                }

                @Override
                public void fail(String s) {
                    Toaster.showShort("识别失败：" + s);
                }
            });
        }));
        buttonsDatas.add(new ButtonsData("颜色识别", item -> {

        }));
        buttonsDatas.add(new ButtonsData("形状识别", item -> {

        }));
        buttonsDatas.add(new ButtonsData("文字识别", item -> {
            p.functionForwarding(p.SCRIPTIDENTIFY, bitmap, new ProjectCallback() {
                @Override
                public void success(Object src) {
                    ProcessingResults results = (ProcessingResults) src;
                    Toaster.showShort("文字识别内容：" + results.getSimpleText());
                    pictureDisplay(results.getBitmap1(),1);
                }

                @Override
                public void fail(String s) {
                    Toaster.showShort("识别失败：" + s);
                }
            });
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
                        .asImageViewer((ImageView) v, bitmap, false,-1,-1,-1,false,new XPopupImageLoader() {
                            @Override
                            public void loadImage(int position, @NonNull Object uri, @NonNull ImageView imageView) {
                                imageView.setImageBitmap((Bitmap) uri);
                                Toaster.showShort("长按保存图片");
                                imageView.setOnLongClickListener(v1 -> {
                                    boolean c = BitmapCut.saveBitmapToGallery(mContext, (Bitmap) uri, "c");
                                    if(c)Toaster.showShort("保存成功");
                                    else Toaster.showShort("保存失败");
                                    return false;
                                });
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
     * 组件初始化
     */
    private void component(){
        // 车牌识别算法配置参数
        HyperLPRParameter parameter = new HyperLPRParameter()
                .setDetLevel(HyperLPR3.DETECT_LEVEL_LOW)
                .setMaxNum(1)
                .setRecConfidenceThreshold(0.85f);
        // 初始化(仅执行一次生效)
        HyperLPR3.getInstance().init(mContext, parameter);
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
            bitmap = BitmapCut.getBitmapFromUri(mContext, data.getData());
            pictureDisplay(bitmap, 0);
        }
    }

}
