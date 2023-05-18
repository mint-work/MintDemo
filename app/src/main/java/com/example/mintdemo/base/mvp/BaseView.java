package com.example.mintdemo.base.mvp;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.hjq.toast.Toaster;


/**
 * 可以封装加载展示框
 * @param <P>
 * @param <CONTRACT>
 */
public abstract class BaseView <P extends BasePresenter,CONTRACT> extends AppCompatActivity {
    protected P p;
    public Context mContext;
    public Application application;
    protected abstract int getLayoutId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        application = this.getApplication();
        Toaster.init(application);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE); //设置无标题
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());

        p = getPresenter(); //弱加载presenter
        p.bindView(this); //v层与p层绑定
        this.initData();
    }
    //View层销毁要处理的事情
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //与p层解除绑定
        p.unBindView();
    }

    //子类在继承的时候必须重写此方法获取 Presenter
    public abstract P getPresenter();
    //这是一个抽象方法在子类继承的时候必须要实现这个方法用来获取契约
    public abstract CONTRACT getContract();
    //如果Presenter 层出现问题要即使告诉View层
    public void error(Exception e){}
    protected abstract void initData();
}
