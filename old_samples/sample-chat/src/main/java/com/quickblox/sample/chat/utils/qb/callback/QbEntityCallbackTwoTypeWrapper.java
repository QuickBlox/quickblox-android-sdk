package com.quickblox.sample.chat.utils.qb.callback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

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
    public void onError(QBResponseException error) {
        onErrorInMainThread(error);
    }

    protected void onSuccessInMainThread(final R result, final Bundle bundle) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onSuccess(result, bundle);
            }
        });
    }

    protected void onErrorInMainThread(final QBResponseException error) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onError(error);
            }
        });
    }
}
