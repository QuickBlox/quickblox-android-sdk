package com.quickblox.chat_v2.gcm;

import android.content.Context;
import android.util.Log;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBDevice;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBPlatform;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.messages.result.QBSubscriptionArrayResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public final class GCMSubscriber {

    private final String TAG = GCMSubscriber.class.getCanonicalName();

    private GCMSubscriber() {
    }

    public static GCMSubscriber newInstance() {
        return new GCMSubscriber();
    }

    public void subscribe(String registrationId, String deviceId) {
        QBMessages.subscribeToPushNotificationsTask(registrationId, deviceId, QBEnvironment.DEVELOPMENT, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                if (result.getErrors().isEmpty()) {
                    Log.d(TAG, "Successfully subscribed to QB push!");
                } else {
                    Log.d(TAG, "Unsuccessfully subscribed to QB push!");
                }
            }

            @Override
            public void onComplete(Result result, Object o) {
                onComplete(result);
            }
        });
    }

    public void unsubscribe(String deviceId){
         getSubscriptionForDelete(deviceId);
    }

    private void deleteSubscription(QBSubscription subscription) {
        QBMessages.deleteSubscription(subscription, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                Log.v(TAG, "Unsubscribed from QB push!");
            }

            @Override
            public void onComplete(Result result, Object o) {
                onComplete(result);
            }
        });

    }

    private void getSubscriptionForDelete(final String deviceId) {
        QBMessages.getSubscriptions(new QBCallback() {

            @Override
            public void onComplete(Result result, Object context) {
                onComplete(result);
            }

            @Override
            public void onComplete(Result result) {
                if (result.getErrors().isEmpty()) {
                    Log.d(TAG, "Got subscriptions to QB push");
                    QBSubscription subscription = handleGetSubscription(result, deviceId);
                    if (subscription != null) {
                        deleteSubscription(subscription);
                    }
                }
            }
        });
    }

    private QBSubscription handleGetSubscription(Result result, String deviceId) {
        QBSubscriptionArrayResult tokenResult = (QBSubscriptionArrayResult) result;
        List<QBSubscription> subscriptionList = tokenResult.getSubscriptions();
        for (QBSubscription subscription : subscriptionList) {
            QBDevice device = subscription.getDevice();
            Log.v(TAG, "Device is " + device.getId());
            if (device.getId().equalsIgnoreCase(deviceId) &&
                    device.getPlatform() == QBPlatform.ANDROID) {
                return subscription;
            }
        }
        return null;
    }
}
