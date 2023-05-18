package com.example.mintdemo.ui.demo1;


import com.example.mintdemo.base.mvp.BasePresenter;

public class LogPresenter extends BasePresenter<LongActivity,LongModle,LoginContract.Presenter> {

    @Override
    public LongModle getMold() {
        return new LongModle(this);
    }

    @Override
    public LoginContract.Presenter getContract() {
        return new LoginContract.Presenter() {
            @Override
            public void loginPresenterRequest(String name,String pws) {
                m.getContract().loginModleRequest(name,pws);
            }

            @Override
            public void longPresenterResults(Boolean src) {
                getview().getContract().longViewResults(src);
            }
        };
    }

}
