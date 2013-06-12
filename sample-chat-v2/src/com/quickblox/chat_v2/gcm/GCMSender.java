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
import java.util.HashMap;
/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public class GCMSender {


    public void sendPushNotifications(final Integer ...user){
        if(Thread.currentThread().equals(Looper.getMainLooper().getThread())){
            createQbEvent(user);
        }else {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    createQbEvent(user);
                }
            });
        }
    }


    public void createQbEvent(Integer ...userId) {

        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.addAll(Arrays.asList(userId));

        Log.d("Push sender", "ids = " + userIds.toString());

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setNotificationChannel(QBNotificationChannel.GCM);
        event.setPushType(QBPushType.GCM);
        event.setMessage("indicator");
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
