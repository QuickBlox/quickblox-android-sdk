package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.Collection;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:29
 */
public class SnippetsUsers extends Snippets {

    public SnippetsUsers(Context context, Collection<String> userIds) {
        super(context);

        this.userIds = userIds;
        snippets.add(signUpUser);
        snippets.add(signInUser);
        snippets.add(deleteUser);
        snippets.add(getUsersByIds);
        snippets.add(signInUsingSocialProvider);
    }

    // Test data
    public static final String LOGIN = "testuser";
    public static final String PASSWORD = "testpassword";
    public static final String EMAIL = "test@test.com";
    public static String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

    int userId = 0;
    Collection<String> userIds;


    Snippet signUpUser = new Snippet("sign up user (register)") {
        @Override
        public void execute() {
            final QBUser user = new QBUser(LOGIN, PASSWORD, EMAIL);

            System.out.println("user instance before request : " + user);

            QBUsers.signUp(user, new QBCallbackImpl() {
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

            QBUsers.signIn(user, new QBCallbackImpl() {
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
                QBUsers.deleteUser(userId, new QBCallbackImpl() {
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

    Snippet getUsersByIds = new Snippet("get users by ids") {
        @Override
        public void execute() {

            QBUsers.getUserByIDs(userIds, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                }
            });
        }
    };

    Snippet signInUsingSocialProvider = new Snippet("sign in using social provider") {
        @Override
        public void execute() {
            QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        userId = qbUserResult.getUser().getId();
                    }
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

}