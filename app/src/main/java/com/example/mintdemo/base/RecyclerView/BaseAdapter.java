package com.example.mintdemo.base.RecyclerView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class BaseAdapter<T, H extends BaseViewHolder> extends RecyclerView.Adapter<BaseViewHolder> {


    protected int layoutResId;

    protected List<T> datas;

    protected Context context;

    protected BaseViewHolder baseViewHolder;

    public OnItemClickListener mOnItemClickListener = null;


    public OnItemLongClickListener mOnItemLongClickListener = null;


    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }


    public BaseAdapter(Context context, int layoutResId) {
        this(context, layoutResId, null);
    }


    public BaseAdapter(Context context, int layoutResId, List<T> datas) {
        this.context = context;
        this.datas = datas == null ? new ArrayList<T>() : datas;
        this.layoutResId = layoutResId;
    }


    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
//        View inflate = LayoutInflater.from(context).inflate(layoutResId, viewGroup, false);
//        return new BaseViewHolder(inflate,mOnItemClickListener, mOnItemLongClickListener);

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutResId, viewGroup, false);
        return  new BaseViewHolder(view, mOnItemClickListener, mOnItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder viewHoder, int position) {
        baseViewHolder = viewHoder;
        T item = getItem(position);
        convert((H) viewHoder, item);
    }


    @Override
    public int getItemCount() {
        if (datas == null || datas.size() <= 0)
            return 0;
        return datas.size();
    }


    public T getItem(int position) {
        if (position >= datas.size()) return null;
        return datas.get(position);
    }


    public void clear() {
        for (Iterator it = datas.iterator(); it.hasNext(); ) {

            T t = (T) it.next();
            int position = datas.indexOf(t);
            it.remove();
            notifyItemRemoved(position);
        }
    }

    public void removeItem(T t) {
        int position = datas.indexOf(t);
        datas.remove(position);
        notifyItemRemoved(position);
        if (position != datas.size())
            notifyItemRangeRemoved(position, datas.size() - position);
    }

    public List<T> getDatas() {
        return datas;
    }


    public void addData(List<T> datas) {
        addData(0, datas);
    }

    public void addData(int position, List<T> list) {

        if (list != null && list.size() > 0) {
            for (T t : list) {
                datas.add(position, t);
                notifyItemInserted(position);
            }
        }
    }

    public void addData(int position, T data) {
        datas.add(position, data);
        notifyItemInserted(position);
    }

    public void refreshData(List<T> list) {
        if (list != null) {
            clear();
            int size = list.size();
            for (int i = 0; i < size; i++) {
                datas.add(i, list.get(i));
                notifyItemInserted(i);
            }
        }
    }

    public void loadMoreData(List<T> list) {

        if (list != null && list.size() > 0) {
            int size = list.size();
            int begin = datas.size();
            for (int i = 0; i < size; i++) {
                datas.add(list.get(i));
                notifyItemInserted(i + begin);
            }
        }
    }

    public BaseViewHolder getBaseViewHolder() {
        return baseViewHolder;
    }

    protected abstract void convert(H holder, T item);

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

}
