package com.example.mintdemo.ui.demo1;

public interface LoginContract {
    interface Modle{
        //联网获取登陆数据
        void loginModleRequest(String name,String pws);
    }
    interface View{
        //登陆数据回传
        void longViewResults(Boolean src);
    }
    interface Presenter{
        //通知数据登陆
        void loginPresenterRequest(String name,String pws);
        //通知view层刷新数据
        void longPresenterResults(Boolean src);
    }
}
