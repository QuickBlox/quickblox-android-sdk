package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.auth.result.QBSessionResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:28
 */
public class SnippetsAuth extends Snippets {

    private static final String TAG = SnippetsAuth.class.getSimpleName();

    public SnippetsAuth(Context context) {
        super(context);

        snippets.add(createSession);
        snippets.add(createSessionWithUser);
        snippets.add(createSessionWithUserEmail);
        snippets.add(createSessionWithSocialProvider);
        snippets.add(destroySession);
    }

    Snippet createSession = new Snippet("create session") {
        @Override
        public void execute() {
            QBAuth.createSession(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        Log.i(TAG, ">>> Session = " + sessionResult.getSession());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithUser = new Snippet("create session", "with user login") {
        @Override
        public void execute() {

            QBAuth.createSession("AndroidGirl", "AndroidGirl", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        Log.i(TAG, ">>> Session = " + sessionResult.getSession());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithUserEmail = new Snippet("create session", "with user email") {
        @Override
        public void execute() {

            QBAuth.createSessionByEmail("test123@test.com", "testpassword", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        Log.i(TAG, ">>> Session = " + sessionResult.getSession());

                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithSocialProvider = new Snippet("create session with social provider") {
        @Override
        public void execute() {

            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBAuth.createSessionUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        Log.i(TAG, ">>> Session = " + sessionResult.getSession());

                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet destroySession = new Snippet("destroy session") {
        @Override
        public void execute() {
            QBAuth.deleteSession(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> Session Destroy OK");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}