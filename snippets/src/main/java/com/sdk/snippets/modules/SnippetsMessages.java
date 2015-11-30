package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.Consts;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.*;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.AsyncSnippet;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vfite on 10.02.14.
 */
public class SnippetsMessages extends Snippets{
    private static final String TAG = SnippetsMessages.class.getSimpleName();

    public SnippetsMessages(Context context) {
        super(context);

        snippets.add(createPushToken);
        snippets.add(createPushTokenSynchronous);
        //
        snippets.add(deletePushToken);
        snippets.add(deletePushTokenSynchronous);
        //
        //
        snippets.add(createSubscription);
        snippets.add(createSubscriptionSynchronous);
        //
        snippets.add(getSubscriptions);
        snippets.add(getSubscriptionsSynchronous);
        //
        snippets.add(deleteSubscription);
        snippets.add(deleteSubscriptionSynchronous);
        //
        //
        snippets.add(createEvent);
        snippets.add(createEventSynchronous);
        //
        snippets.add(getEventWithId);
        snippets.add(getEventWithIdSynchronous);
        //
        snippets.add(getEvents);
        snippets.add(getEventsSynchronous);
        //
        snippets.add(updateEvent);
        snippets.add(updateEventSynchronous);
        //
        snippets.add(deleteEvent);
        snippets.add(deleteEventSynchronous);
        //
        //
        snippets.add(subscribeToPushNotificationsTask);
        snippets.add(subscribeToPushNotificationsTaskSynchronous);
    }


    //
    /////////////////////////////////////// Create Push token //////////////////////////////////////
    //


    Snippet createPushToken = new Snippet("create push token") {
        @Override
        public void execute() {
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }
            //
            QBPushToken qbPushToken = new QBPushToken();
            qbPushToken.setEnvironment(QBEnvironment.DEVELOPMENT);
            qbPushToken.setDeviceUdid(deviceId);
            qbPushToken.setCis("APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o");

            QBMessages.createPushToken(qbPushToken, new QBEntityCallbackImpl<QBPushToken>() {

                @Override
                public void onSuccess(QBPushToken pushToken, Bundle args) {
                    Log.i(TAG, ">>> PushToken: " + pushToken.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createPushTokenSynchronous = new AsyncSnippet("create push token (synchronous)", context) {
        @Override
        public void executeAsync() {
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }

            QBPushToken qbPushToken = new QBPushToken();
            qbPushToken.setEnvironment(QBEnvironment.DEVELOPMENT);
            qbPushToken.setDeviceUdid(deviceId);
            qbPushToken.setCis("APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o");

            QBPushToken pushToken = null;
            try {
                pushToken = QBMessages.createPushToken(qbPushToken);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(pushToken != null){
                Log.i(TAG, ">>> PushToken: " + pushToken.toString());
            }
        }
    };


    //
    /////////////////////////////////////// Delete Push token //////////////////////////////////////
    //


    Snippet deletePushToken = new Snippet("delete push token") {
        @Override
        public void execute() {
            QBMessages.deletePushToken(1473068, new QBEntityCallbackImpl<Void>() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> push token successfully deleted");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deletePushTokenSynchronous = new AsyncSnippet("delete push token (synchronous)", context) {
        @Override
        public void executeAsync() {

            try {
                QBMessages.deletePushToken(1473068);
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };

    //
    /////////////////////////////////////// Create Subscription ////////////////////////////////////
    //


    Snippet createSubscription = new Snippet("create subscription") {
        @Override
        public void execute() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            QBMessages.createSubscription(subscription, new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {

                @Override
                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                    Log.i(TAG, ">>> Subscription: " + subscriptions.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createSubscriptionSynchronous = new AsyncSnippet("create subscription (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);

            ArrayList<QBSubscription> createdSubscriptions = null;
            try {
                createdSubscriptions =  QBMessages.createSubscription(subscription);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(createdSubscriptions != null){
                Log.i(TAG, ">>> Subscription: " + createdSubscriptions.toString());
            }
        }
    };


    //
    /////////////////////////////////////// Get Subscription ///////////////////////////////////////
    //


    Snippet getSubscriptions = new Snippet("get subscriptions") {
        @Override
        public void execute() {
            QBMessages.getSubscriptions(new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {

                @Override
                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                    Log.i(TAG, ">>> Subscriptions: " + subscriptions.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getSubscriptionsSynchronous = new AsyncSnippet("get subscriptions (synchronous)", context) {
        @Override
        public void executeAsync() {
            ArrayList<QBSubscription> subscriptions = null;
            try {
                subscriptions = QBMessages.getSubscriptions();
            } catch (QBResponseException e) {
                setException(e);
            }
            if(subscriptions != null){
                Log.i(TAG, ">>> Subscriptions: " + subscriptions.toString());
            }
        }
    };


    //
    ////////////////////////////////////// Delete Subscription /////////////////////////////////////
    //


    Snippet deleteSubscription = new Snippet("delete subscription") {
        @Override
        public void execute() {
            QBMessages.deleteSubscription(1558628, new QBEntityCallbackImpl<Void>() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> subscription successfully deleted");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteSubscriptionSynchronous = new AsyncSnippet("delete subscriptions (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBMessages.deleteSubscription(1558628);
                Log.i(TAG, ">>> subscription successfully deleted");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////////// Create Event /////////////////////////////////////////
    //


    Snippet createEvent = new Snippet("create event (send push)") {
        @Override
        public void execute() {
            // recipient
            StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
//            userIds.add(ApplicationConfig.getInstance().getTestUserId1());
            userIds.add(2792282);
//            userIds.add(2792283);

            QBEvent event = new QBEvent();
            event.setUserIds(userIds);
            event.setType(QBEventType.ONE_SHOT);
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setNotificationType(QBNotificationType.PUSH);

            // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
            //
//            event.setMessage("Gonna send Push Notification!");

            // generic push with custom parameters
            //
            JSONObject json = new JSONObject();
            try {
                json.put("message", "hello to all");
                json.put("param1", "value1");
                json.put("ios_badge", "4");
            } catch (Exception e) {
                e.printStackTrace();
            }
            event.setMessage(json.toString());

            // Android based push
            //
//            event.setPushType(QBPushType.GCM);
//            HashMap<String, String> data = new HashMap<String, String>();
//            data.put("data.message", "Hello");
//            data.put("data.type", "welcome message");
//            event.setMessage(data);

            QBMessages.createEvent(event, new QBEntityCallbackImpl<QBEvent>() {
                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> new event: " + qbEvent.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createEventSynchronous = new AsyncSnippet("create event (send push) (synchronous)", context) {
        @Override
        public void executeAsync() {
            // recipient
            StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
            userIds.add(ApplicationConfig.getInstance().getTestUserId1());

            QBEvent event = new QBEvent();
            event.setUserIds(userIds);
            event.setType(QBEventType.ONE_SHOT);
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setNotificationType(QBNotificationType.PUSH);

            // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
            //
            event.setMessage("Gonna send Push Notification!");

            // generic push with custom parameters
            //
//            JSONObject json = new JSONObject();
//            try {
//                json.put("message", "hello");
//                json.put("param1", "value1");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            event.setMessage(json.toString());

            // Android based push
            //
//            event.setPushType(QBPushType.GCM);
//            HashMap<String, String> data = new HashMap<String, String>();
//            data.put("data.message", "Hello");
//            data.put("data.type", "welcome message");
//            event.setMessage(data);


            QBEvent createdEvent = null;
            try {
                createdEvent = QBMessages.createEvent(event);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(createdEvent != null){
                Log.i(TAG, ">>> Event: " + createdEvent.toString());
            }
        }
    };


    //
    ////////////////////////////////////////// Get Event ///////////////////////////////////////////
    //


    Snippet getEventWithId = new Snippet("get event", "with id") {
        @Override
        public void execute() {
            QBMessages.getEvent(1454324, new QBEntityCallbackImpl<QBEvent>() {

                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> event: " + qbEvent.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getEventWithIdSynchronous = new AsyncSnippet("get event (synchronous)", "with id", context) {
        public QBEvent event;

        @Override
        public void executeAsync() {
            try {
                event = QBMessages.getEvent(1454324);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(event != null){
                Log.i(TAG, ">>> event: " + event.toString());
            }
        }
    };


    //
    ////////////////////////////////////////// Get Events ///////////////////////////////////////////
    //


    Snippet getEvents = new Snippet("get events") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            QBMessages.getEvents(requestBuilder, new QBEntityCallbackImpl<ArrayList<QBEvent>>() {

                @Override
                public void onSuccess(ArrayList<QBEvent> events, Bundle args) {
                    Log.i(TAG, ">>> Events: " + events.toString());
                    Log.i(TAG, "currentPage: " + args.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + args.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + args.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });

        }
    };

    Snippet getEventsSynchronous = new AsyncSnippet("get events (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            Bundle params = new Bundle();
            ArrayList<QBEvent> events = null;
            try {
                events = QBMessages.getEvents(requestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(events != null){
                Log.i(TAG, ">>> Events: " + events.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Update Event ///////////////////////////////////////////
    //


    Snippet updateEvent = new Snippet("update event") {
        @Override
        public void execute() {
            QBEvent event = new QBEvent();
            event.setId(1470629);
            event.setActive(true); // send it again

            QBMessages.updateEvent(event, new QBEntityCallbackImpl<QBEvent>() {

                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> event: " + qbEvent.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet updateEventSynchronous = new AsyncSnippet("update events (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBEvent event = new QBEvent();
            event.setId(1454326);
            event.setActive(true); // send it again

            QBEvent updatedEvent = null;
            try {
                updatedEvent = QBMessages.updateEvent(event);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(updatedEvent != null){
                Log.i(TAG, ">>> Event: " + updatedEvent.toString());
            }
        }
    };


    //
    /////////////////////////////////////// Delete Event ///////////////////////////////////////////
    //


    Snippet deleteEvent = new Snippet("delete event") {
        @Override
        public void execute() {
            QBMessages.deleteEvent(1454324, new QBEntityCallbackImpl<Void>() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> event successfully deleted");
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteEventSynchronous = new AsyncSnippet("delete events (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBMessages.deleteEvent(1454326);

                Log.i(TAG, ">>> event successfully deleted");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////////////// Tasks /////////////////////////////////////////////
    //


    Snippet subscribeToPushNotificationsTask = new Snippet("TASK: subscribe to push notifications") {
        @Override
        public void execute() {
            String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }

            QBMessages.subscribeToPushNotificationsTask(registrationID, deviceId, QBEnvironment.DEVELOPMENT, new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {

                @Override
                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                    Log.i(TAG, ">>> subscription created" + subscriptions.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });

        }
    };

    Snippet subscribeToPushNotificationsTaskSynchronous = new AsyncSnippet("TASK: subscribe to push notifications (synchronous)", context) {
        @Override
        public void executeAsync() {
            String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }

            ArrayList<QBSubscription> subscriptions = null;
            try {
                subscriptions = QBMessages.subscribeToPushNotificationsTask(registrationID, deviceId, QBEnvironment.DEVELOPMENT);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(subscriptions != null){
                Log.i(TAG, ">>> subscription created: " + subscriptions);
            }
        }
    };

}
