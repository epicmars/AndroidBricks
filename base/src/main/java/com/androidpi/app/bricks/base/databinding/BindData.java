package com.androidpi.app.bricks.base.databinding;

import androidx.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jastrelax on 2018/7/31.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface BindData {
    /**
     * 布局资源
     * @return
     */
    @LayoutRes int value() default 0;

    /**
     * 与视图绑定的数据类型。
     * @return
     */
    Class<?>[] dataTypes() default {};
}
