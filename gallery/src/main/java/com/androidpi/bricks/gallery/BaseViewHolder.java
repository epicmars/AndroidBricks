package com.androidpi.bricks.gallery;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import layoutbinder.annotations.BindLayout;

public abstract class BaseViewHolder<T, B extends ViewDataBinding> extends RecyclerView.ViewHolder{

    protected int layout;
    protected B binding;
    protected Context context;

    public BaseViewHolder(View view){
        super(view);
        this.context = view.getContext();
        this.binding = DataBindingUtil.bind(view);
        if(getClass().isAnnotationPresent(BindLayout.class)){
            BindLayout layout = getClass().getAnnotation(BindLayout.class);
            if (null == layout)
                throw new NullPointerException("ViewHolder that extends BaseViewHolder must be annotated with LayoutId");
            this.layout = layout.value();
        }
    }

    public int getLayout() {
        return layout;
    }

    public void setLayout(int layout) {
        this.layout = layout;
    }

    public B getBinding() {
        return binding;
    }

    public abstract void render(final T data, int position);

}