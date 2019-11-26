package com.androidpi.app.bricks.base.activity;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidpi.app.bricks.base.databinding.BindData;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * 一个ViewHolder仅关联一个布局，本身不限制所展示的实体类型，但具体实现类需要知道要展示哪种数据以实现相应的展示逻辑。
 * <p>
 * 它所代表的一个列表项拥有类似一个activity或fragment的作用，可以拥有独立的业务功能，并不仅限于被动的数据展示，
 * 即也可以使用ViewModel实现业务逻辑。
 * <p>
 * 约定优先于配置：尽可能少使用类型参数
 * <p>
 * Created by jastrelax on 2018/7/5.
 */

public abstract class RecyclerViewHolder<VDB extends ViewDataBinding> extends RecyclerView.ViewHolder
        implements LifecycleOwner {

    private final LifecycleRegistry mRegistry = new LifecycleRegistry(this);

    protected VDB binding;
    protected List<Class> dataType = new ArrayList<>();
    protected FragmentManager fragmentManager;
    protected RecyclerAdapter adapter;

    public RecyclerViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * 根据BaseViewHolder类型和父布局实例化一个BaesViewHolder的子类。
     *
     * @param clazz  BaseViewHolder的实现类。
     * @param parent RecyclerView父布局。
     * @return
     */
    public static RecyclerViewHolder instance(Class<? extends RecyclerViewHolder> clazz, ViewGroup parent) {
        if (null == clazz) {
            throw new NullPointerException("The view holder class to be instantiated is null, it may not be " +
                    "registered in RecyclerAdapter.");
        }
        BindData bindLayout = clazz.getAnnotation(BindData.class);
        if (null == bindLayout) {
            throw new NullPointerException("A view holder must be annotated with BindData!");
        }
        int layoutId = bindLayout.value();
        try {
            ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false);
            Constructor<? extends RecyclerViewHolder> constructor = clazz.getDeclaredConstructor(View.class);
            RecyclerViewHolder viewHolder = constructor.newInstance(binding.getRoot());
            viewHolder.binding = binding;
            viewHolder.dataType = Arrays.asList(bindLayout.dataTypes());
            return viewHolder;
        } catch (InflateException e) {
            Timber.e(e, "布局展开错误。");
        } catch (NoSuchMethodException e) {
            logError(clazz, e);
        } catch (IllegalAccessException e) {
            Timber.e(e, "%s 实例化错误。", clazz);
        } catch (InvocationTargetException e) {
            Timber.e(e, "%s 实例化错误。", clazz);
        } catch (InstantiationException e) {
            Timber.e(e, "%s 实例化错误。", clazz);
        }
        return null;
    }

    public static RecyclerViewHolder instance(Class<? extends RecyclerViewHolder> clazz, ViewGroup parent, FragmentManager fm, RecyclerAdapter adapter) {
        RecyclerViewHolder holder = instance(clazz, parent);
        if (holder != null) {
            holder.fragmentManager = fm;
            holder.adapter = adapter;
        }
        return holder;
    }

    private static void logError(Class<? extends RecyclerViewHolder> clazz, NoSuchMethodException e) {
        Timber.e(e, "%s 实例化错误。", clazz);
    }

    /**
     * 解析数据。
     *
     * @param data
     * @param position
     */
    final void onBindInternal(Object data, int position) {
        if (null == data) {
            Timber.w("Data is null!");
            return;
        }
        if (!dataType.contains(data.getClass())) {
            Timber.e("Data type doesn't match the contract!");
            return;
        }
        onBindData(data, position);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return this.mRegistry;
    }

    /**
     * 绑定数据。
     */
    public abstract <T> void onBindData(T data, int position);

    protected void onAttachedToWindow() {
        // empty
    }

    protected void onDetachedToWindow() {
        // empty
    }

    public void onViewRecycled() {
        // empty
    }
}
