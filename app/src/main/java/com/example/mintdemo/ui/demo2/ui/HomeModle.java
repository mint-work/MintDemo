package com.example.mintdemo.ui.demo2.ui;


import com.example.mintdemo.base.mvp.BaseModle;

public class HomeModle extends BaseModle<HomePresenter,HomeContract.Modle> {


    public HomeModle(HomePresenter homePresenter) {
        super(homePresenter);
    }

    @Override
    public HomeContract.Modle getContract() {
        return null;
    }
}
