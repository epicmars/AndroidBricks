package com.androidpi.bricks.gallery.task;

public interface ExecuteListener<Result> {
    /**
     * 非UI线程中执行
     *
     * @return
     */
    public Result onExecute();

    /**
     * UI线程中获取结果
     *
     * @param result
     */
    public void onResult(Result result);
}