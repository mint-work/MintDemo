package com.example.mintdemo.ui.demo2.ui;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraCharacteristics;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mintdemo.R;
import com.example.mintdemo.base.mvp.BaseView;
import com.example.mintdemo.ui.demo2.adapter.ButtonsAdapter;
import com.example.mintdemo.ui.demo2.adapter.ButtonsData;
import com.example.mintdemo.ui.demo2.tool.FlexboxSpecingDecoration;
import com.example.mintdemo.ui.demo2.tool.MeasureUtil;
import com.example.mintdemo.ui.demo2.tool.StringTool;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.WhiteToastStyle;


import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends BaseView<HomePresenter,HomeContract.View> implements HomeContract.View {
    public RecyclerView buttons;
    public ButtonsAdapter ButtonsAdapter;
    private TextView systemLogs,cartLogs;
    private ImageView HomeImg,logImg1,logImg2;

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
        p.permissionRequest(mContext);//权限获取

    }



    /**
     * 底部按钮初始化
     */
    private void buttonInitialization(){
        buttons = (RecyclerView) findViewById(R.id.buttons);
        LinearLayoutManager linearLayoutManager3 = new LinearLayoutManager(this);
        linearLayoutManager3.setOrientation(LinearLayoutManager.HORIZONTAL);
        buttons.setLayoutManager(linearLayoutManager3);
        buttons.addItemDecoration(new FlexboxSpecingDecoration(MeasureUtil.dpToPx(mContext, 5)));

        List<ButtonsData> buttonsDatas = new ArrayList<>();
        buttonsDatas.add(new ButtonsData("全自动", item -> { //添加全自动按钮
            Log.e("1122","全自动开始运行");

        }));
        ButtonsAdapter = new ButtonsAdapter(HomeActivity.this, R.layout.item_button, buttonsDatas);
        buttons.setAdapter(ButtonsAdapter);
    }

    /**
     * 日志窗口初始化
     */
    private void logWindowInitialized() {
        ToastUtils.init(HomeActivity.this.getApplication());// 初始化吐司工具类
        systemLogs = (TextView) findViewById(R.id.systemLogs);
        cartLogs = (TextView) findViewById(R.id.cartLogs);
        HomeImg = (ImageView) findViewById(R.id.preview);
        logImg1 = (ImageView) findViewById(R.id.logimg1);
        logImg2 = (ImageView) findViewById(R.id.logimg2);
    }

    /**
     * 窗口日志刷新
     */
    @Override
    public void windowLogs(String src,int position) {
        String s1 = null;
        if(position==0){
            s1 = systemLogs.getText().toString();
        }else if(position==1){
            s1 = cartLogs.getText().toString();
        }else {
            return;
        }


        List<String> codeVoList = StringTool.getCodeVoList(s1);
        if(codeVoList.size()>5){
            List<String> s = new ArrayList<>();
            s.add(codeVoList.get(0));
            for (int i = 0; i < 4; i++) {
                s.add(codeVoList.get(codeVoList.size()-4+i));
            }
            codeVoList = s;
        }
        String test = codeVoList.get(0);
        for (int i = 0; i < codeVoList.size()-1; i++) {
            test = test + "\n" + codeVoList.get(i+1);
        }
        if(position==0){
            systemLogs.setText(test +"\n"+src);
        }else if(position==1){
            cartLogs.setText(test +"\n"+src);
        }

    }

    @Override
    public void pictureDisplay(Bitmap src,int position) {
        if(position==0){
            HomeImg.setImageBitmap(src);
        }else if(position==1){
            logImg1.setImageBitmap(src);
        }else if(position==2){
            logImg2.setImageBitmap(src);
        }
    }

    @Override
    public void getImage(Bitmap bitmap) {
        HomeImg.setImageBitmap(bitmap);
    }

}
