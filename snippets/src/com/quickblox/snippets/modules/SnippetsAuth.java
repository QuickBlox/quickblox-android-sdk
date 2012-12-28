package com.quickblox.snippets.modules;

import android.content.Context;
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

    public static int currentUserId = 0;
    public static String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

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
                    if(result.isSuccess()){
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        System.out.println(">>> Session = " + sessionResult.getSession());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithUser = new Snippet("create session", "with user login") {
        @Override
        public void execute() {
            String login = SnippetsUsers.LOGIN;
            String password = SnippetsUsers.PASSWORD;

            QBAuth.createSession(login, password, new QBCallbackImpl(){
                @Override
                public void onComplete(Result result) {
                    if(result.isSuccess()){
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        System.out.println(">>> Session = " + sessionResult.getSession());

                        // save current user ID
                        currentUserId = sessionResult.getSession().getUserId();
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithUserEmail = new Snippet("create session", "with user email") {
        @Override
        public void execute() {
            String email = SnippetsUsers.EMAIL;
            String password = SnippetsUsers.PASSWORD;

            QBAuth.createSessionByEmail(email, password, new QBCallbackImpl(){
                @Override
                public void onComplete(Result result) {
                    if(result.isSuccess()){
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        System.out.println(">>> Session = " + sessionResult.getSession());

                        // save current user ID
                        currentUserId = sessionResult.getSession().getUserId();
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet createSessionWithSocialProvider = new Snippet("create session with social provider") {
        @Override
        public void execute() {
            QBAuth.createSessionUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBCallbackImpl(){
                @Override
                public void onComplete(Result result) {
                    if(result.isSuccess()){
                        QBSessionResult sessionResult = (QBSessionResult) result;
                        System.out.println(">>> Session = " + sessionResult.getSession());

                        // save current user ID
                        currentUserId = sessionResult.getSession().getUserId();
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet destroySession = new Snippet("destroy session") {
        @Override
        public void execute() {
            QBAuth.deleteSession(new QBCallbackImpl(){
                @Override
                public void onComplete(Result result) {
                    if(result.isSuccess()){
                        System.out.println(">>> Session Destroy OK");
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };
}