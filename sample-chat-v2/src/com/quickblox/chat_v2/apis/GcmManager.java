package com.quickblox.chat_v2.apis;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushType;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.messages.result.QBSubscriptionArrayResult;

import java.util.HashMap;

/**
 * Created by andrey on 07.06.13.
 */

public class GcmManager {

    private Context context;

    public GcmManager(Context context) {
        this.context = context;
    }



    // Subscribe to Push Notifications
    public void subscribeToPushNotifications(String registrationID) {
        String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        QBMessages.subscribeToPushNotificationsTask(registrationID, deviceId, QBEnvironment.DEVELOPMENT, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                Log.d("GCM", "subscribe result = " + result);
            }
        });
    }

    // unregister from QuickBlox
    public void unSubscribe() {
        QBMessages.getSubscriptions(new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBSubscriptionArrayResult subscriptionArrayResult = (QBSubscriptionArrayResult) result;
                    String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

                    for (QBSubscription subscription : subscriptionArrayResult.getSubscriptions()) {
                        if (subscription.getDevice().getId().equals(deviceId)) {
                            QBMessages.deleteSubscription(subscription.getId(), new QBCallbackImpl() {
                                @Override
                                public void onComplete(Result result) {
                                    if (result.isSuccess()) {
                                        Log.d("GCM", "unsubscribe result = " + result);
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }

        );

    }

}
