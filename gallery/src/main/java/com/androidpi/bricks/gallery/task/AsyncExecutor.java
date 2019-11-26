package com.androidpi.bricks.gallery.task;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Process;

import com.androidpi.app.bricks.common.AppUtil;

import java.util.ArrayDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 后台任务执行,提供串行任务执行
 */
public class AsyncExecutor {

    // 常量
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 32;
    // private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    // private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    // private static final int KEEP_ALIVE = 1;
    private static final int KEEP_ALIVE = 1;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "AsyncExecutor #" + mCount.getAndIncrement());
        }
    };
    private static final int POST_RESULT = 0;
    public final Executor SERIAL_EXECUTOR = AppUtil.hasHoneycomb() ? new SerialExecutor()
            : Executors.newSingleThreadExecutor(sThreadFactory);
    public final Executor DUAL_THREAD_EXECUTOR = Executors.newFixedThreadPool(2, sThreadFactory);
    private final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(32);

    public final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory, new ThreadPoolExecutor.DiscardOldestPolicy());
    private final AtomicBoolean mTaskInvoked = new AtomicBoolean();
    private final Handler mHandler = new Handler(new Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case POST_RESULT:
                    ExecuteResult result = (ExecuteResult) msg.obj;
                    result.listener.onResult(result.result);
                    break;
            }
            return true;
        }
    });

    public static AsyncExecutor instance() {
        return AsyncExecutorHolder.instance;
    }

    public void execute(Runnable task) {
        SERIAL_EXECUTOR.execute(task);
    }

    /**
     * 带返回结果的异步执行
     *
     * @param listener
     */
    public <Result> void executeWithResult(final ExecuteListener<Result> listener) {

        Callable<Result> callable = new Callable<Result>() {

            @Override
            public Result call() throws Exception {
                mTaskInvoked.set(true);
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                return postResult(listener, listener.onExecute());
            }
        };

        FutureTask<Result> future = new FutureTask<Result>(callable) {
            @Override
            protected void done() {
                try {
                    postResultIfNotInvoked(listener, get());
                } catch (InterruptedException e) {

                } catch (ExecutionException e) {

                } catch (CancellationException e) {
                    postResultIfNotInvoked(listener, null);
                }
            }
        };

        SERIAL_EXECUTOR.execute(future);
    }

    private <Result> void postResultIfNotInvoked(final ExecuteListener<Result> listener, Result result) {
        final boolean invoked = mTaskInvoked.get();
        if (!invoked)
            postResult(listener, result);
    }

    private <Result> Result postResult(final ExecuteListener<Result> listener, Result result) {
        Message msg = mHandler.obtainMessage(POST_RESULT);
        msg.obj = new ExecuteResult<>(listener, result);
        msg.sendToTarget();
        return result;
    }

    private static class AsyncExecutorHolder {
        private static final AsyncExecutor instance = new AsyncExecutor();
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private class SerialExecutor implements Executor {

        final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        Runnable active;

        @Override
        public synchronized void execute(final Runnable r) {
            // 提交任务到队列
            tasks.offer(new Runnable() {

                @Override
                public void run() {
                    try {
                        r.run();
                    } finally {
                        scheduleNext();
                    }
                }
            });
            // 任务调度
            if (active == null) {
                scheduleNext();
            }
        }

        protected void scheduleNext() {
            if ((active = tasks.poll()) != null) {
                THREAD_POOL_EXECUTOR.execute(active);
            }
        }
    }

    private class ExecuteResult<Result> {
        ExecuteListener<Result> listener;
        Result result;

        public ExecuteResult(ExecuteListener<Result> listener, Result result) {
            this.listener = listener;
            this.result = result;
        }
    }
}