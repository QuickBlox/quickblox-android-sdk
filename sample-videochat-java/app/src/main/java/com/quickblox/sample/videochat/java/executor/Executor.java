package com.quickblox.sample.videochat.java.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executor {
    private final int CORE_POOL_SIZE = 3;
    private final int MAX_POOL_SIZE = 3;
    private final int KEEP_ALIVE_TIME = 1;

    private final BlockingQueue<Runnable> threadQueue = new LinkedBlockingQueue<>();
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS, threadQueue);

    private static Executor instance;

    private static synchronized Executor getInstance() {
        if (instance == null) {
            instance = new Executor();
        }
        return instance;
    }

    private Executor() {
    }

    public static <T> void addTask(ExecutorTask<T> executorTask) {
        getInstance().threadPoolExecutor.execute(() -> {
            Handler mainHandler = new Handler(Looper.getMainLooper());

            try {
                T result = executorTask.onBackground();
                mainHandler.post(() -> executorTask.onForeground(result));
            } catch (Exception exception) {
                mainHandler.post(() -> executorTask.onError(exception));
            }
        });
    }
}