package com.quickblox.sample.videochat.conference.java.utils.qb;

import android.os.Bundle;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;

public class QBEntityCallbackImpl<T> implements QBEntityCallback<T> {

    public QBEntityCallbackImpl() {
    }

    @Override
    public void onSuccess(T result, Bundle bundle) {

    }

    @Override
    public void onError(QBResponseException e) {

    }
}