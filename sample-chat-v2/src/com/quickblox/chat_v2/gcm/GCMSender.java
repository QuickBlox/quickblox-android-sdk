package com.quickblox.chat_v2.gcm;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationChannel;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushType;

import java.util.Arrays;

public class GCMSender {

    private String mHybridMessageBody;


    public void sendPushNotifications(final int pUserId, String pHybridMessageBody) {
        mHybridMessageBody = pHybridMessageBody;
        if (Thread.currentThread().equals(Looper.getMainLooper().getThread())) {
            createQbEvent(pUserId);
        } else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createQbEvent(pUserId);
                }
            });
        }
    }


    public void createQbEvent(int pUserId) {

        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.addAll(Arrays.asList(pUserId));

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setNotificationChannel(QBNotificationChannel.GCM);
        event.setPushType(QBPushType.GCM);
        event.setMessage(mHybridMessageBody);
        event.setName("QBPush");

        QBMessages.createEvent(event, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {

                if (result.getErrors().isEmpty()) {
                    Log.d("GCM", "Without error");
                } else {
                    Log.d("GCM", "Error = " + result.getErrors());
                }

            }
        });
    }
}
