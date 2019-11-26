package com.androidpi.app.bricks.base.activity;

import android.content.Context;
import android.content.res.Resources;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

public abstract class BaseViewHolder<T, B extends ViewDataBinding> extends RecyclerView.ViewHolder{

    protected B binding;

    public BaseViewHolder(View itemView) {
        super(itemView);
        binding = DataBindingUtil.bind(itemView);
    }

    public abstract void bindData(T data);

    protected Context getContext() {
        return itemView.getContext();
    }

    protected Resources getResource() {
        return itemView.getResources();
    }
}
