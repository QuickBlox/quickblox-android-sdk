package com.quickblox.snippets.modules;

import android.content.Context;
import android.telephony.TelephonyManager;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.*;
import com.quickblox.module.messages.result.*;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 16:48
 */
public class SnippetsMessages extends Snippets {

    //Create push token with  Registration Id for Android
    String registrationId;
    String deviceId;
    int pushTokenId;
    int subscriptionId;
    int eventId;
    int userId = 53779;

    public SnippetsMessages(Context context) {
        super(context);

        snippets.add(createSubscription);
        snippets.add(createEvent);
        snippets.add(createPushToken);
        snippets.add(deletePushToken);
        snippets.add(getSubscriptions);
        snippets.add(deleteSubscription);
        snippets.add(getEventWithId);
        snippets.add(getEvents);
        snippets.add(getPullEvent);
        snippets.add(updateEvent);
        snippets.add(deleteEvent);
        deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        registrationId = "12345678";
    }

    Snippet createSubscription = new Snippet("subscription fot events", "(push listener)") {
        @Override
        public void execute() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
            QBMessages.createSubscription(subscription, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        if (((QBSubscriptionArrayResult) result).getSubscriptions().size() != 0) {
                            subscriptionId = ((QBSubscriptionArrayResult) result).getSubscriptions().get(0).getId();
                        }
                        System.out.println(">>> subscription created");
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
            QBMessages.getSubscriptions(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBSubscriptionArrayResult subscriptionArrayResult = (QBSubscriptionArrayResult) result;
                        System.out.println(">>> subscriptions count - " + subscriptionArrayResult.getSubscriptions().size());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet deleteSubscription = new Snippet("delete subscription") {
        @Override
        public void execute() {
            if (subscriptionId != 0) {
                QBMessages.deleteSubscription(subscriptionId, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            subscriptionId = 0;
                            System.out.println(">>> subscription deleted - ");
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println(">>> Create subscription before deleting.");
            }
        }
    };

    Snippet createEvent = new Snippet("create event (send push)") {
        @Override
        public void execute() {

            QBEvent event = new QBEvent();
            StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
            userIds.add(userId);

            event.setUserIds(userIds);
            event.setMessage("my push message");
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setPushType(QBPushType.GCM);
            event.setNotificationType(QBNotificationType.PUSH);

            // Before create event you should make device subscription for messages receiving
            QBMessages.createEvent(event, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBEventResult eventResult = (QBEventResult) result;
                        QBEvent newEvent = eventResult.getEvent();
                        eventId = newEvent.getId();
                        System.out.println(">>> new event: " + newEvent);
                    }
                }
            });
        }
    };

    Snippet getEventWithId = new Snippet("get event with id") {
        @Override
        public void execute() {
            if (eventId != 0) {
                QBMessages.getEvent(eventId, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBEventResult eventResult = (QBEventResult) result;
                            System.out.println(">>> Message: " + eventResult.getEvent().getMessage());
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {

                    }
                });
            } else {
                System.out.println(">>> Create event");
            }
        }
    };

    Snippet createPushToken = new Snippet("create push token") {
        @Override
        public void execute() {
            QBPushToken qbPushToken = new QBPushToken();
            qbPushToken.setEnvironment(QBEnvironment.DEVELOPMENT);
            qbPushToken.setDeviceUdid(deviceId);
            qbPushToken.setDevicePlatform("android");
            qbPushToken.setCis(registrationId);

            QBMessages.createPushToken(qbPushToken, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        pushTokenId = ((QBPushTokenResult) result).getPushToken().getId();
                        System.out.println(">>> PushTokeId: " + pushTokenId);
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object o) {
                }
            });
        }
    };

    Snippet getEvents = new Snippet("get Events") {
        @Override
        public void execute() {
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder(20, 1);
            QBMessages.getEvents(requestBuilder, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBEventPagedResult eventPagedResult = (QBEventPagedResult) result;
                        System.out.println(">>> Events count: " + eventPagedResult.getEvents().size());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getPullEvent = new Snippet("get pull events") {
        @Override
        public void execute() {

            QBMessages.getPullEvents(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBEventArrayResult eventArrayResult = (QBEventArrayResult) result;
                        System.out.println(">>> Pull events count: " + eventArrayResult.getEvents().size());
                    } else {
                        handleErrors(result);
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet updateEvent = new Snippet("update event") {
        @Override
        public void execute() {
            if (eventId != 0) {
                QBEvent event = new QBEvent();
                event.setId(eventId);
                event.setMessage("new push message");
                event.setEnvironment(QBEnvironment.DEVELOPMENT);
                event.setPushType(QBPushType.GCM);
                event.setNotificationType(QBNotificationType.PUSH);

                QBMessages.updateEvent(event, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            QBEventResult eventResult = (QBEventResult) result;
                            System.out.println(">>> New event message: " + eventResult.getEvent().getMessage());
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println(">>> create new event before update");
            }
        }
    };

    Snippet deleteEvent = new Snippet("delete event") {
        @Override
        public void execute() {
            if (eventId != 0) {
                QBMessages.deleteEvent(eventId, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            eventId = 0;
                            System.out.println(">>> event successfully deleted");
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println(">>> create new event before deleting");
            }
        }
    };


    Snippet deletePushToken = new Snippet("delete push token") {
        @Override
        public void execute() {
            if (pushTokenId != 0) {
                QBMessages.deletePushToken(pushTokenId, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);
                        if (result.isSuccess()) {
                            pushTokenId = 0;
                            System.out.println(">>> push token successfully deleted");
                        } else {
                            handleErrors(result);
                        }
                    }

                    @Override
                    public void onComplete(Result result, Object context) {
                    }
                });
            } else {
                System.out.println(">>> create push token before deleting");
            }
        }
    };

}
