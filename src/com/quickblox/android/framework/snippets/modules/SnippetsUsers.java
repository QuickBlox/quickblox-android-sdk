package com.quickblox.android.framework.snippets.modules;

import android.content.Context;
import com.quickblox.android.framework.base.definitions.QBCallback;
import com.quickblox.android.framework.base.net.results.Result;
import com.quickblox.android.framework.modules.users.models.QBUser;
import com.quickblox.android.framework.modules.users.net.server.QBUsers;
import com.quickblox.android.framework.snippets.Snippet;
import com.quickblox.android.framework.snippets.Snippets;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:29
 */
public class SnippetsUsers extends Snippets {

    public SnippetsUsers(Context context) {
        super(context);

        snippets.add(signUpUser);
        snippets.add(signInUser);
        snippets.add(deleteUser);
    }

    // Test data
    public static final String LOGIN = "testuser";
    public static final String PASSWORD = "testpassword";
    public static final String EMAIL = "test@test.com";

    int userId = 0;

    Snippet signUpUser = new Snippet("sign up user (register)") {
        @Override
        public void execute() {
            final QBUser user = new QBUser(LOGIN, PASSWORD, EMAIL);

            System.out.println("user instance before request : " + user);

            QBUsers.signUp(user, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    System.out.println(">>> user instance after request: " + user);
                }
            });
        }
    };

    Snippet signInUser = new Snippet("sign in user (login)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser(LOGIN, PASSWORD);

            QBUsers.signIn(user, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        userId = user.getId();
                    }
                }
            });
        }
    };

    Snippet deleteUser = new Snippet("delete user") {
        @Override
        public void execute() {
            if (userId != 0) {
                QBUsers.deleteUser(userId, new QBCallback() {
                    @Override
                    public void onComplete(Result result) {
                        printResultToConsole(result);

                        if (result.isSuccess()) {
                            userId = 0;
                        }
                    }
                });
            } else {
                System.out.println("Sign in  user first.");
            }
        }
    };
}