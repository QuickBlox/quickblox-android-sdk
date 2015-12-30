package com.quickblox.sample.chat.utils.qb.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.core.QBEntityCallback;

import java.util.List;

public class QbEntityCallbackTwoTypeWrapper<T, R> implements QBEntityCallback<T> {
    protected static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    protected QBEntityCallback<R> callback;

    public QbEntityCallbackTwoTypeWrapper(QBEntityCallback<R> callback) {
        this.callback = callback;
    }

    @Override
    public void onSuccess(T t, Bundle bundle) {
        // Do nothing, we want to trigger callback with another data type
    }

    @Override
    public void onSuccess() {
        onSuccessInMainThread();
    }

    @Override
    public void onError(List<String> errors) {
        onErrorInMainThread(errors);
    }

    protected void onSuccessInMainThread(final R result, final Bundle bundle) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result, bundle);
            }
        });
    }

    protected void onSuccessInMainThread() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess();
            }
        });
    }

    protected void onErrorInMainThread(final List<String> errors) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(errors);
            }
        });
    }
}
