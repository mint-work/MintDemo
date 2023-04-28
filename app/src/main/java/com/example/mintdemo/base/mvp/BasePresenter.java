package com.example.mintdemo.base.mvp;

import java.lang.ref.WeakReference;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class BasePresenter<V extends BaseView, M extends BaseModle, CONTRACT> {
    protected M m;
    //弱引用V层
    private WeakReference<V> vWeakReference;

    public BasePresenter() {
//        Type[] types = ((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments();
//        try {
//            m =  (M)((Class)types[1]).newInstance();
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        } catch (InstantiationException e) {
//            throw new RuntimeException(e);
//        }
        m = getMold();
    }

    //P层与V层进行弱绑定
    public void bindView(V v) {
        vWeakReference = new WeakReference<>(v);
    }

    //P层与V层解除绑定
    public void unBindView() {
        if (vWeakReference != null) {
            vWeakReference.clear();
            vWeakReference = null;
            System.gc();
        }
    }

    public V getview() {
        if (vWeakReference != null) {
            return vWeakReference.get();
        }
        return null;
    }

    public abstract M getMold();
    public abstract CONTRACT getContract();
}
