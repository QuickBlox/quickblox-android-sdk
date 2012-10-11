package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import android.widget.Toast;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.messages.models.*;
import com.quickblox.android.framework.modules.messages.net.results.QBEventResult;
import com.quickblox.android.framework.modules.messages.net.server.QBMessages;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 16:48
 */
public class SnippetsMessages extends Snippets {

    public SnippetsMessages(Context context) {
        super(context);

        snippets.add(createSubscription);
        snippets.add(createEvent);
        snippets.add(comingSoon);
    }

    Snippet createSubscription = new Snippet("subscription fot events", "(push listener)") {
        @Override
        public void execute() {
            QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);

            QBMessages.createSubscription(subscription, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }
            });
        }
    };

    Snippet createEvent = new Snippet("create event (send push)") {
        @Override
        public void execute() {
            QBEvent event = new QBEvent();
            event.setMessage("my push message");
            event.setEnvironment(QBEnvironment.DEVELOPMENT);
            event.setPushType(QBPushType.GCM);
            event.setNotificationType(QBNotificationType.PUSH);

            // Before create event you should make device subscription for messages receiving
            QBMessages.createEvent(event, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        QBEventResult eventResult = (QBEventResult) result;
                        QBEvent newEvent = eventResult.getEvent();
                        System.out.println(">>> new event: " + newEvent);
                    }
                }
            });
        }
    };

    Snippet comingSoon = new Snippet("coming soon...", "detailed Messages sample coming soon...") {
        @Override
        public void execute() {
            Toast.makeText(context, "detailed Messages sample coming soon...", Toast.LENGTH_SHORT).show();
        }
    };
}