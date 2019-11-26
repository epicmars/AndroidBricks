package com.androidpi.app.bricks.base.activity;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidpi.app.bricks.base.databinding.BindData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 列表适配器，一个列表适配的列表项展示什么内容只与RecyclerViewHolder的实现类有关。
 *
 * 一个RecyclerViewHolder类包含了所需要的布局信息，数据类型信息，RecyclerAdapter负责将负载数据项
 * 映射到一种RecyclerViewHolder的子类。
 *
 * Created by jastrelax on 2018/7/5.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerViewHolder>{

    private final SparseIntArray mDataViewMap = new SparseIntArray();
    private final SparseArray<Class<? extends RecyclerViewHolder>> mViewHolderMap= new SparseArray<>();
    private final List<Object> mPayloads = new ArrayList<>();
    private FragmentManager fragmentManager;
    private View.OnClickListener itemClickListener;

    /**
     * 注册一个或多个RecyclerViewHolder以用于数据展示。
     * @param clazzArray
     * @return
     */
    public RecyclerAdapter register(Class<? extends RecyclerViewHolder>... clazzArray) {

        for (Class clazz : clazzArray) {
            BindData bindLayout = (BindData) clazz.getAnnotation(BindData.class);
            // 建立数据类型到布局的映射
            for (Class dataType : bindLayout.dataTypes()) {
                mDataViewMap.append(dataType.hashCode(), bindLayout.value());
            }
            // 建立布局到RecyclerViewHolder的映射
            mViewHolderMap.put(bindLayout.value(), clazz);
        }
        return this;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Class<? extends RecyclerViewHolder> viewHolderClass = mViewHolderMap.get(viewType);
        // If viewHolderClass is null, the ViewHolder may not be registered
        return RecyclerViewHolder.instance(viewHolderClass, parent, fragmentManager, this);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Object item = mPayloads.get(position);
        holder.onBindInternal(item, position);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onDetachedToWindow();
    }

    @Override
    public void onViewRecycled(RecyclerViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onViewRecycled();
    }

    @Override
    public int getItemCount() {
        return mPayloads.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = mPayloads.get(position);
        return mDataViewMap.get(item.getClass().hashCode());
    }

    public List<Object> getPayloads() {
        return mPayloads;
    }

    public void setFragmentManager(FragmentManager mFragmentManager) {
        this.fragmentManager = mFragmentManager;
    }

    /**
     * Set adapter payloads.
     * @param payloads
     */
    public <T> void setPayloads(T... payloads) {
        setPayloads(Arrays.asList(payloads));
    }

    /**
     * Set adapter payloads.
     * @param payloads
     */
    public void setPayloads(Collection<?> payloads) {
        if (null == payloads) {
            return;
        }
        this.mPayloads.clear();
        this.mPayloads.addAll(payloads);
        notifyDataSetChanged();
    }

    /**
     * Add payload to current payloads.
     * @param payloads
     */
    public void addPayloads(Collection<?> payloads) {
        if (null == payloads || payloads.isEmpty()) {
            return;
        }
        int positionStart = mPayloads.size();
        int itemCount = payloads.size();
        this.mPayloads.addAll(payloads);
        notifyItemRangeInserted(positionStart, itemCount);
    }

    public void setItemClickListener(View.OnClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public View.OnClickListener getItemClickListener() {
        return itemClickListener;
    }
}