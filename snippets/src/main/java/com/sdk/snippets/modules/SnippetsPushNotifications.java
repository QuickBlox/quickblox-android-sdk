package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.quickblox.core.Consts;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.*;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.SnippetAsync;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by vfite on 10.02.14.
 */
public class SnippetsPushNotifications extends Snippets{
    private static final String TAG = SnippetsPushNotifications.class.getSimpleName();

    public SnippetsPushNotifications(Context context) {
        super(context);

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
    /////////////////////////////////////// Create Subscription ////////////////////////////////////
    //


    Snippet createSubscription = new Snippet("create subscription") {
        @Override
        public void execute() {

            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
            //
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }
            subscription.setDeviceUdid(deviceId);
            //
            String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
            subscription.setRegistrationID(registrationID);

            QBPushNotifications.createSubscription(subscription).performAsync(new QBEntityCallback<ArrayList<QBSubscription>>() {

                @Override
                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                    Log.i(TAG, ">>> Subscription: " + subscriptions.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet createSubscriptionSynchronous = new SnippetAsync("create subscription (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            //
            subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
            //
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }
            subscription.setDeviceUdid(deviceId);
            //
            String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
            subscription.setRegistrationID(registrationID);


            ArrayList<QBSubscription> createdSubscriptions = null;
            try {
                createdSubscriptions =  QBPushNotifications.createSubscription(subscription).perform();
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
            QBPushNotifications.getSubscriptions().performAsync(new QBEntityCallback<ArrayList<QBSubscription>>() {

                @Override
                public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                    Log.i(TAG, ">>> Subscriptions: " + subscriptions.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getSubscriptionsSynchronous = new SnippetAsync("get subscriptions (synchronous)", context) {
        @Override
        public void executeAsync() {
            ArrayList<QBSubscription> subscriptions = null;
            try {
                subscriptions = QBPushNotifications.getSubscriptions().perform();
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
            QBPushNotifications.deleteSubscription(1558628).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> subscription successfully deleted");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteSubscriptionSynchronous = new SnippetAsync("delete subscriptions (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBPushNotifications.deleteSubscription(1558628).perform();
                Log.i(TAG, ">>> subscription successfully deleted");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////////// Create Event /////////////////////////////////////////
    //

    protected QBEvent buildEvent(){
        // recipient
        StringifyArrayList<Integer> userIds = new StringifyArrayList<>();
        userIds.add(ApplicationConfig.getInstance().getTestUserId1());
        userIds.add(ApplicationConfig.getInstance().getTestUserId2());
        userIds.add(5179218);
        userIds.add(301);

        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setType(QBEventType.ONE_SHOT);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);

//            // generic push - will be delivered to all platforms (Android, iOS, WP, Blackberry..)
//            //
//            event.setMessage("This is simple generic push notification!");


//            // generic push with custom parameters - http://quickblox.com/developers/Messages#Use_custom_parameters
//            //
//            JSONObject json = new JSONObject();
//            try {
//                json.put("message", "This is generic push notification with custom params!");
//                json.put("param1", "value1");
//                json.put("ios_badge", "4"); // iOS badge value
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            //
//            event.setMessage(json.toString());


//            // Android based push
//            //
//            event.setPushType(QBPushType.GCM);
//            HashMap<String, Object> data = new HashMap<>();
//            data.put("data.message", "This is Android based push notification!");
//            data.put("data.param1", "value1");
//            //
//            event.setMessage(data);


        // iOS based push
        //
        event.setPushType(QBPushType.APNS);
        HashMap<String, Object> data = new HashMap<>();
        Map<String, String> aps = new HashMap<>();
        aps.put("alert", "You have 3 new messages");
        aps.put("badge", "3");
        data.put("aps", aps);
        //
        event.setMessage(data);

        return event;
    }

    Snippet createEvent = new Snippet("create event (send push)") {
        @Override
        public void execute() {

            QBEvent event = buildEvent();

            QBPushNotifications.createEvent(event).performAsync(new QBEntityCallback<QBEvent>() {
                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> new event: " + qbEvent.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };


    Snippet createEventSynchronous = new SnippetAsync("create event (send push) (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBEvent event = buildEvent();

            QBEvent createdEvent = null;
            try {
                createdEvent = QBPushNotifications.createEvent(event).perform();
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
            QBPushNotifications.getEvent(1454324).performAsync(new QBEntityCallback<QBEvent>() {

                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> event: " + qbEvent.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getEventWithIdSynchronous = new SnippetAsync("get event (synchronous)", "with id", context) {
        public QBEvent event;

        @Override
        public void executeAsync() {
            try {
                event = QBPushNotifications.getEvent(1454324).perform();
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
            Bundle bundle = new Bundle();
            QBPushNotifications.getEvents(requestBuilder, bundle).performAsync(new QBEntityCallback<ArrayList<QBEvent>>() {

                @Override
                public void onSuccess(ArrayList<QBEvent> events, Bundle args) {
                    Log.i(TAG, ">>> Events: " + events.toString());
                    Log.i(TAG, "currentPage: " + args.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + args.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + args.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });

        }
    };

    Snippet getEventsSynchronous = new SnippetAsync("get events (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            Bundle params = new Bundle();
            ArrayList<QBEvent> events = null;
            try {
                events = QBPushNotifications.getEvents(requestBuilder, params).perform();
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

            QBPushNotifications.updateEvent(event).performAsync(new QBEntityCallback<QBEvent>() {

                @Override
                public void onSuccess(QBEvent qbEvent, Bundle args) {
                    Log.i(TAG, ">>> event: " + qbEvent.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet updateEventSynchronous = new SnippetAsync("update events (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBEvent event = new QBEvent();
            event.setId(1454326);
            event.setActive(true); // send it again

            QBEvent updatedEvent = null;
            try {
                updatedEvent = QBPushNotifications.updateEvent(event).perform();
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
            QBPushNotifications.deleteEvent(1454324).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> event successfully deleted");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteEventSynchronous = new SnippetAsync("delete events (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBPushNotifications.deleteEvent(1454326).perform();

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
            if (deviceId == null) {
                deviceId = "UniversalDeviceId";
            }

            QBSubscription subscription = new QBSubscription();
            subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
            subscription.setDeviceUdid(deviceId);
            subscription.setRegistrationID(registrationID);

            QBPushNotifications.createSubscription(subscription).performAsync(
                    new QBEntityCallback<ArrayList<QBSubscription>>() {

                        @Override
                        public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                            Log.i(TAG, ">>> subscription created" + subscriptions.toString());
                        }

                        @Override
                        public void onError(QBResponseException errors) {
                            handleErrors(errors);
                        }
                    });

        }
    };

    Snippet subscribeToPushNotificationsTaskSynchronous = new SnippetAsync("TASK: subscribe to push notifications (synchronous)", context) {
        @Override
        public void executeAsync() {
            String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
            String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            if(deviceId == null){
                deviceId = "UniversalDeviceId";
            }

            QBSubscription subscription = new QBSubscription();
            subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
            subscription.setDeviceUdid(deviceId);
            subscription.setRegistrationID(registrationID);

            ArrayList<QBSubscription> subscriptions = null;
            try {
                subscriptions = QBPushNotifications.createSubscription(subscription).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if(subscriptions != null){
                Log.i(TAG, ">>> subscription created: " + subscriptions);
            }
        }
    };

}
