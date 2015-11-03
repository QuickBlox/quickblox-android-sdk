package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.account.model.QBAccountSettings;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.executor.QBExecutor;
import com.quickblox.core.server.BaseService;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.model.QBSession;
import com.quickblox.users.model.QBUser;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.AsyncSnippet;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.util.concurrent.TimeUnit;

/**
 * Created by vfite on 22.01.14.
 */
public class SnippetsAuth extends Snippets{

    private static final String TAG = SnippetsAuth.class.getSimpleName();

    public SnippetsAuth(Context context) {
        super(context);

        snippets.add(setAutoUpdateSettings);
        snippets.add(deleteAutoUpdateSettings);
        snippets.add(forceRequestSettings);

        snippets.add(createSession);
        snippets.add(createSessionSynchronous);
        //
        snippets.add(createSessionWithUser);
        snippets.add(createSessionWithUserSynchronous);
        //
        snippets.add(createSessionWithSocialProvider);
        snippets.add(createSessionWithSocialProviderSynchronous);
        //
        snippets.add(destroySession);
        snippets.add(destroySessionSynchronous);
        //
        snippets.add(getSession);
        snippets.add(getSessionSynchronous);
    }


    //
    /////////////////////////////////// Create session /////////////////////////////////////////////
    //

    Snippet setAutoUpdateSettings = new Snippet("set auto update settings mode") {
        @Override
        public void execute() {

            QBSettings.getInstance().setAccountKey(ApplicationConfig.getInstance().getAccountKey());

            QBSettings.AutoUpdateMode updateMode = new QBSettings.AutoUpdateMode(context,
                    ApplicationConfig.getInstance().getAppId());
            updateMode.setUpdateTimePeriod(TimeUnit.MINUTES.toMillis(1));
            QBSettings.getInstance().setUpdateMode(updateMode);
        }
    };

    Snippet deleteAutoUpdateSettings = new Snippet("delete auto update mode ") {
        @Override
        public void execute() {
            QBSettings.getInstance().setUpdateMode(null);
        }
    };

    Snippet forceRequestSettings = new Snippet("force request settings ") {
        @Override
        public void execute() {
            QBExecutor <QBAccountSettings> as = QBSettings.synchronizeFromAccount(ApplicationConfig.getInstance().getAccountKey());
            Bundle b = new Bundle();
            as.makeAsync(new QBEntityCallback<QBAccountSettings>() {
                @Override
                public void onSuccess(QBAccountSettings qbAccountSettings, Bundle bundle) {

                }

                @Override
                public void onError(QBResponseException e) {
                    handleErrors(e);
                }
            }, b);
        }
    };


    Snippet createSession = new Snippet("create session") {
        @Override
        public void execute() {

            QBAuth.createSession(new QBEntityCallback<QBSession>() {

                @Override
                public void onSuccess(QBSession session, Bundle params) {
                    Log.i(TAG, "session created, token = " + session.getToken());
                }

                @Override
                public void onError(QBResponseException e) {
                    handleErrors(e);
                }
            });
        }
    };

    Snippet createSessionSynchronous = new AsyncSnippet("create session (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBSession session = null;
            try {
                session = QBAuth.createSession();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(session != null){
                Log.i(TAG, "session created, token = " + session.getToken());
            }
        }
    };


    //
    /////////////////////////////// Create session with user ///////////////////////////////////////
    //


    Snippet createSessionWithUser = new Snippet("create session", "with user") {
        @Override
        public void execute() {

            QBAuth.createSession(new QBUser(ApplicationConfig.getInstance().getTestUserLogin1(),
                    ApplicationConfig.getInstance().getTestUserPassword1()), new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle args) {
                    Log.i(TAG, "session created, token = " + session.getToken());
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });

            try {
                BaseService.createFromExistentToken("31ed199120fb998dc472aea785a1825809ad5c04", null);
            } catch (BaseServiceException e) {
                e.printStackTrace();
            }
        }
    };

    Snippet createSessionWithUserSynchronous = new AsyncSnippet("create session (synchronous)", "with user", context) {
        @Override
        public void executeAsync() {
            QBSession session = null;
            try {
                QBUser user = new QBUser(ApplicationConfig.getInstance().getTestUserLogin1(),
                        ApplicationConfig.getInstance().getTestUserPassword1());
                session = QBAuth.createSession(user);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(session != null){
                Log.i(TAG, "session created, token = " + session.getToken());
            }
        }
    };


    //
    ////////////////////////// Create session with social provider /////////////////////////////////
    //


    Snippet createSessionWithSocialProvider = new Snippet("create session", "with social provider") {
        @Override
        public void execute() {

            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBAuth.createSessionUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBEntityCallback<QBSession>() {

                @Override
                public void onSuccess(QBSession session,  Bundle args) {
                    Log.i(TAG, "session created, token = "+session.getToken());
                }

                @Override
                public void onError(QBResponseException eroors) {
                    handleErrors(eroors);
                }
            });
        }
    };

    Snippet createSessionWithSocialProviderSynchronous = new AsyncSnippet("create session (synchronous)", "with social provider", context) {
        @Override
        public void executeAsync() {
            QBSession session = null;
            try {
                String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

                session = QBAuth.createSessionUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(session != null){
                Log.i(TAG, "session created, token = " + session.getToken());
            }
        }
    };


    //
    ///////////////////////////////////// Destroy session //////////////////////////////////////////
    //


    Snippet destroySession = new Snippet("destroy session") {
        @Override
        public void execute() {
            QBAuth.deleteSession(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.i(TAG, "success");
                }

                @Override
                public void onError(QBResponseException e) {
                    handleErrors(e);
                }
            });
        }
    };

    Snippet destroySessionSynchronous = new AsyncSnippet("delete session (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBAuth.deleteSession();
                Log.i(TAG, "session destroyed ");
            } catch (QBResponseException e) {
                Log.i(TAG, "destroy fail");
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////// Get session //////////////////////////////////////////
    //


    Snippet getSession = new Snippet("get session") {
        @Override
        public void execute() {

            QBAuth.getSession(new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession qbSession, Bundle bundle) {
                    Log.i(TAG, "session: " + qbSession);
                }

                @Override
                public void onError(QBResponseException strings) {

                }
            });
        }
    };

    Snippet getSessionSynchronous = new AsyncSnippet("get session (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBSession session = QBAuth.getSession();
                Log.i(TAG, "session: " + session);
            } catch (QBResponseException e) {
                Log.i(TAG, "get session fail");
                setException(e);
            }
        }
    };

}
