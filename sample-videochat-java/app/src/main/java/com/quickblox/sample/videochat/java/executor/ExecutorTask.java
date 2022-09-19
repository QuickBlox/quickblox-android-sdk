package com.quickblox.sample.videochat.java.executor;

public interface ExecutorTask<T> {
    T onBackground() throws Exception;

    void onForeground(T result);

    void onError(Exception exception);
}