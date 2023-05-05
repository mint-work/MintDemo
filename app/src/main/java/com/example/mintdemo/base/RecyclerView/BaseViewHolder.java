package com.example.mintdemo.base.RecyclerView;

import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

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
