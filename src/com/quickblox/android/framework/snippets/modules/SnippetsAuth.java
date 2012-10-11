package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.auth.models.QBSession;
import com.quickblox.android.framework.modules.auth.net.results.QBSessionResult;
import com.quickblox.android.framework.modules.auth.net.server.QBAuth;
import com.quickblox.android.framework.modules.messages.models.QBDevice;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:28
 */
public class SnippetsAuth extends Snippets {

    public static int currentUserId = 0;

    public SnippetsAuth(Context context) {
        super(context);

        snippets.add(createSession);
        snippets.add(createSessionWithUser);
        snippets.add(createSessionWithUserEmail);
        snippets.add(createSessionWithDevice);
    }

    Snippet createSession = new Snippet("create session") {
        @Override
        public void execute() {
            QBAuth.authorizeApp(authCallback);
        }
    };

    Snippet createSessionWithUser = new Snippet("create session", "with user login") {
        @Override
        public void execute() {
            String login = SnippetsUsers.LOGIN;
            String password = SnippetsUsers.PASSWORD;

            QBAuth.authorizeApp(login, password, authCallback);
        }
    };

    Snippet createSessionWithUserEmail = new Snippet("create session", "with user email") {
        @Override
        public void execute() {
            String email = SnippetsUsers.EMAIL;
            String password = SnippetsUsers.PASSWORD;

            QBAuth.authorizeAppByEmail(email, password, authCallback);
        }
    };

    Snippet createSessionWithDevice = new Snippet("create session", "with device") {
        @Override
        public void execute() {
            String login = SnippetsUsers.LOGIN;
            String password = SnippetsUsers.PASSWORD;
            QBDevice device = new QBDevice(context);

            QBAuth.authorizeApp(login, password, device, authCallback);
        }
    };

    QBCallback authCallback = new QBCallback() {
        @Override
        public void onComplete(Result result) {
            printResultToConsole(result);

            if (result.isSuccess()) {
                QBSessionResult sessionResult = (QBSessionResult) result;
                QBSession session = sessionResult.getSession();

                System.out.println(">>> session token = " + session.getToken());

                if (session.getUserId() != null) {
                    currentUserId = session.getUserId();
                }
            }
        }
    };
}