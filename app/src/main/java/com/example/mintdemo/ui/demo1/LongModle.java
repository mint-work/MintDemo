package com.example.mintdemo.ui.demo1;


import com.example.mintdemo.base.mvp.BaseModle;

public class LongModle extends BaseModle<LogPresenter,LoginContract.Modle> {
    public LongModle(LogPresenter logPresenter) {
        super(logPresenter);
    }
    @Override
    public LoginContract.Modle getContract() {
        return new LoginContract.Modle() {
            @Override
            public void loginModleRequest(String name, String pws) {
                p.getContract().longPresenterResults(true);
            }
        };
    }
}
