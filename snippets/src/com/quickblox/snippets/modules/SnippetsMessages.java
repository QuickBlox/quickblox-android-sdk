package com.quickblox.snippets.modules;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationChannel;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushToken;
import com.quickblox.module.messages.model.QBPushType;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.messages.result.QBEventArrayResult;
import com.quickblox.module.messages.result.QBEventPagedResult;
import com.quickblox.module.messages.result.QBEventResult;
import com.quickblox.module.messages.result.QBPushTokenResult;
import com.quickblox.module.messages.result.QBSubscribeToPushNotificationsResult;
import com.quickblox.module.messages.result.QBSubscriptionArrayResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 16:48
 */
public class SnippetsMessages extends Snippets {

    private static final String TAG = SnippetsMessages.class.getSimpleName();

    public SnippetsMessages(Context context) {
        super(context);

        snippets.add(createPushToken);
        snippets.add(deletePushToken);

        snippets.add(createSubscription);
        snippets.add(getSubscriptions);
        snippets.add(deleteSubscription);

        snippets.add(createEvent);
        snippets.add(getEventWithId);
        snippets.add(getEvents);
        snippets.add(getPullEvent);
        snippets.add(updateEvent);
        snippets.add(deleteEvent);

        snippets.add(subscribeToPushNotificationsTask);
    }

    //
    ///////////////////////////////////////////// Push token /////////////////////////////////////////////
    //
    Snippet createPushToken = new Snippet("create push token") {
        @Override
        public void execute() {
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDevice";
            }

            QBPushToken qbPushToken = new QBPushToken();
            qbPushToken.setEnvironment(QBEnvironment.DEVELOPMENT);
            qbPushToken.setDeviceUdid(deviceId);
            qbPushToken.setCis("2342hiyf2352959fg9af03fgfg0fahoo018273af");


            QBMessages.createPushToken(qbPushToken, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBPushTokenResult pushTokenResult = (QBPushTokenResult) result;
                        Log.i(TAG, ">>> PushToken: " + pushTokenResult.getPushToken().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };



    Snippet deletePushToken = new Snippet("delete push token") {
        @Override
        public void execute() {
            QBMessages.deletePushToken(13998, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> push token successfully deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    //
    ///////////////////////////////////////////// Subscription /////////////////////////////////////////////
    //
    Snippet createSubscription = new Snippet("subscription fot events", "(push listener)") {
        @Override
        public void execute() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            QBMessages.createSubscription(subscription, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSubscriptionArrayResult subscriptionResult = (QBSubscriptionArrayResult) result;
                        Log.i(TAG, ">>> subscription created" + subscriptionResult.getSubscriptions().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getSubscriptions = new Snippet("get subscriptions") {
        @Override
        public void execute() {
            QBMessages.getSubscriptions(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSubscriptionArrayResult subscriptionArrayResult = (QBSubscriptionArrayResult) result;
                        Log.i(TAG, ">>> subscriptions - " + subscriptionArrayResult.getSubscriptions().toString());
                    } else {
                        handleErrors(result);
                    }
                }

            });
        }
    };

    Snippet deleteSubscription = new Snippet("delete subscription") {
        @Override
        public void execute() {
            QBMessages.deleteSubscription(14824, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> subscription deleted - ");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    //
    ///////////////////////////////////////////// Event /////////////////////////////////////////////
    //
    Snippet createEvent = new Snippet("create event (send push)") {
        @Override
        public void execute() {

            // recipient
            StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
            userIds.add(3055);
//            userIds.add(960);

            QBEvent event = new QBEvent();
            event.setUserIds(userIds);
            event.setName("Magic Push");
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setNotificationType(QBNotificationType.PUSH);

            // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
            event.setMessage("Gonna send Push Notification!");

            // Android based push
//            event.setPushType(QBPushType.GCM);
//            HashMap<String, String> data = new HashMap<String, String>();
//            data.put("data.message", "Hello");
//            data.put("data.type", "welcome message");
//            event.setMessage(data);

            QBMessages.createEvent(event, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBEventResult eventResult = (QBEventResult) result;
                        Log.i(TAG, ">>> new event: " + eventResult.getEvent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getEventWithId = new Snippet("get event with id") {
        @Override
        public void execute() {
            QBMessages.getEvent(25245, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBEventResult eventResult = (QBEventResult) result;
                        Log.i(TAG, ">>> Event: " + eventResult.getEvent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet getEvents = new Snippet("get Events") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            QBMessages.getEvents(requestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBEventPagedResult eventPagedResult = (QBEventPagedResult) result;
                        ArrayList<QBEvent> events = eventPagedResult.getEvents();
                        Log.i(TAG, ">>> Events: " + events.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getPullEvent = new Snippet("get pull events") {
        @Override
        public void execute() {

            QBMessages.getPullEvents(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBEventArrayResult eventArrayResult = (QBEventArrayResult) result;
                        Log.i(TAG, ">>> Pull event list: " + eventArrayResult.getEvents().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateEvent = new Snippet("update event") {
        @Override
        public void execute() {
            QBEvent event = new QBEvent();
            event.setId(25245);
            event.setMessage("Gonna send Push Notification again!");
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setPushType(QBPushType.GCM);
            event.setNotificationType(QBNotificationType.PUSH);

            QBMessages.updateEvent(event, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBEventResult eventResult = (QBEventResult) result;
                        Log.i(TAG, ">>> Event: " + eventResult.getEvent().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteEvent = new Snippet("delete event") {
        @Override
        public void execute() {
            QBMessages.deleteEvent(25245, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> event successfully deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //
    Snippet subscribeToPushNotificationsTask = new Snippet("TASK: Subscribe to push notifications") {
        @Override
        public void execute() {
            String registrationID = "2342hiyf2352959fg9af03fgfg0fahoo018273af";
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDevice";
            }

            QBMessages.subscribeToPushNotificationsTask(registrationID, deviceId, QBEnvironment.PRODUCTION, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSubscribeToPushNotificationsResult subscribeToPushNotificationsResult = (QBSubscribeToPushNotificationsResult) result;
                        Log.i(TAG, ">>> subscription created" + subscribeToPushNotificationsResult.getSubscriptions().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });

        }
    };
}
