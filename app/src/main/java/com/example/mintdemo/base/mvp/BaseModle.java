package com.example.mintdemo.base.mvp;

//MVP设计框架中的M层负责数据的获取耗时操作等等
public abstract class BaseModle<P extends BasePresenter,CONTRACT> {
    protected P p;

    public BaseModle(P p) {
        this.p = p;
    }
    //这是一个抽象方法在子类继承的时候必须要实现这个方法用来获取契约
    public abstract CONTRACT getContract();
}
