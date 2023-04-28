package com.example.mintdemo.ui.demo2.tool;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


/**
 * RecyclerView + Flexbox 间隔距离
 */
public class FlexboxSpecingDecoration extends RecyclerView.ItemDecoration {

    private int space;

    public FlexboxSpecingDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.right = space;
        outRect.top = space;
    }
}
