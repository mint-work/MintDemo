package com.example.mintdemo.ui.demo2.base;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.mintdemo.R;

public class BaseViewHolder extends RecyclerView.ViewHolder {


    private SparseArray<View> views;

    public BaseViewHolder(View itemView, BaseAdapter.OnItemClickListener onItemClickListener, BaseAdapter.OnItemLongClickListener onItemLongClickListener) {
        super(itemView);
        this.views = new SparseArray<View>();
        itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                Log.e("1122","snvo");
                onItemClickListener.onItemClick(v, getLayoutPosition());
            }
        });
        itemView.setOnLongClickListener(v -> {
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(v, getLayoutPosition());
            }
            return false;
        });
    }

    public BaseViewHolder(View itemView, BaseAdapter.OnItemClickListener onItemClickListener) {
        super(itemView);
        this.views = new SparseArray<View>();
        itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getLayoutPosition());
            }
        });
    }

    public BaseViewHolder(View itemView) {
        super(itemView);
        this.views = new SparseArray<View>();
    }

    public View getView(int viewId) {
        return retrieveView(viewId);
    }


    public <T extends View> T retrieveView(int viewId) {

        View view = views.get(viewId);

        if (view == null) {
            view = itemView.findViewById(viewId);
            views.put(viewId, view);
        }
        return (T) view;
    }
}
