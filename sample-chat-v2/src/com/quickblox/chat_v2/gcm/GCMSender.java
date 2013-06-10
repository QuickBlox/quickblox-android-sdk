package com.quickblox.chat_v2.gcm;

import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushType;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public class GCMSender {
    public void sendPushNotifications(StringifyArrayList<Integer> userIds) {

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);

        //GCMMessage message = GCMMessage.newInstance()
        HashMap<String, String> data = new HashMap<String, String>();
        data.put("data.message", "Hello");
        data.put("data.type", "welcome message");
        event.setMessage(data);

        QBMessages.createEvent(event, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                Log.d("GCM", "send result = " + result);
            }
        });
    }


}
