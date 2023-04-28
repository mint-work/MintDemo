package com.example.mintdemo.ui.demo2.adapter;

import android.content.Context;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.mintdemo.R;
import com.example.mintdemo.ui.demo2.base.BaseAdapter;
import com.example.mintdemo.ui.demo2.base.BaseViewHolder;

import java.util.List;

/**
 * 底部按钮适配器
 */
public class ButtonsAdapter extends BaseAdapter<ButtonsData, BaseViewHolder> {
    public ButtonsAdapter(Context context, int layoutResId, List<ButtonsData> datas) {
        super(context, layoutResId, datas);
    }

    @Override
    protected void convert(BaseViewHolder holder, ButtonsData item) {
        Button view = (Button)holder.getView(R.id.item_btuuon);
        view.setText(item.getName());
        if(item.getItem()){ //防止重复初始化
            view.setOnClickListener(item.getClickStandard());
            item.setItem(false);
        }

    }
}


