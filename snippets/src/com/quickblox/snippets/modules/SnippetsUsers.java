package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:29
 */
public class SnippetsUsers extends Snippets {

    public SnippetsUsers(Context context) {
        super(context);

        userIds = new ArrayList<String>();
        userIds.add("123");
        userTags = new ArrayList<String>();
        userTags.add("batman");
        userTags.add("superman");
        user = new QBUser(LOGIN, PASSWORD);
        user.setEmail(EMAIL);

        snippets.add(signInUserWithLogin);
        snippets.add(signInUserWithEmail);
        snippets.add(signInUsingSocialProvider);
        snippets.add(signOut);
        snippets.add(signUpUser);

        snippets.add(getAllUsers);
        snippets.add(getUsersByIds);
        snippets.add(getUserById);
        snippets.add(getUserWithLogin);
        snippets.add(getUserWithFullName);
        snippets.add(getUserWithTwitterId);
        snippets.add(getUserWithFacebookId);
        snippets.add(getUserWithEmail);
        snippets.add(getUserWithTags);
        snippets.add(getUserWithExternalId);

        snippets.add(updateUser);

        snippets.add(deleteUserById);
        snippets.add(deleteUserByExternalId);

        snippets.add(resetPassword);
    }

    // Test data
    public static final int USER_ID = 1234;

    public static final String LOGIN = "testuser";
    public static final String PASSWORD = "testpassword";
    public static final String EMAIL = "test123@test.com";
    public static final String FULL_NAME = "fullName";
    public static final String TWITTER_ID = "1233433";
    public static final String FACEBOOK_ID = "123";
    public static final String EXTERNAL_ID = "123";
    public static String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

    int userId = 0;
    Collection<String> userIds;
    Collection<String> userTags;
    QBUser user;


    Snippet signInUserWithLogin = new Snippet("sign in user (login)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser(LOGIN, PASSWORD);

            QBUsers.signIn(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                    if (result.isSuccess()) {
                        userId = user.getId();
                        Log.d("user.getTags()", user.getTags().toString());
                    }
                }
            });
        }
    };

    Snippet signInUserWithEmail = new Snippet("sign in user (email)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser();
            user.setEmail(EMAIL);
            user.setPassword(PASSWORD);

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

    Snippet signOut = new Snippet("sign out") {
        @Override
        public void execute() {
            QBUsers.signOut(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {

                }
            });
        }
    };

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

    Snippet getAllUsers = new Snippet("get all users") {
        @Override
        public void execute() {
            QBUsers.getUsers(new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    ArrayList<QBUser> qbUserPagedResult = ((QBUserPagedResult) result).getUsers();
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
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

    Snippet getUserById = new Snippet("get user by id") {
        @Override
        public void execute() {
            QBUsers.getUser(USER_ID, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getUserWithLogin = new Snippet("get user with login") {
        @Override
        public void execute() {
            QBUsers.getUserByLogin(LOGIN, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    QBUser qbUser = ((QBUserResult) result).getUser();
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getUserWithFullName = new Snippet("get user with full name") {
        @Override
        public void execute() {
            QBUsers.getUsersByFullName(FULL_NAME, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                    QBUser qbUser = ((QBUserResult) result).getUser();
                }

                @Override
                public void onComplete(Result result, Object context) {

                }
            });
        }
    };

    Snippet getUserWithTwitterId = new Snippet("get user with twitter id") {
        @Override
        public void execute() {
            QBUsers.getUserByTwitterId(TWITTER_ID, new QBCallback() {
                @Override
                public void onComplete(Result result) {

                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getUserWithFacebookId = new Snippet("get user with facebook id") {
        @Override
        public void execute() {
            QBUsers.getUserByFacebookId(FACEBOOK_ID, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);

                }

                @Override
                public void onComplete(Result result, Object context) {

                }
            });
        }
    };

    Snippet getUserWithEmail = new Snippet("get user with email") {
        @Override
        public void execute() {
            QBUsers.getUserByEmail(EMAIL, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {

                }
            });
        }
    };

    Snippet getUserWithTags = new Snippet("get user with tags") {
        @Override
        public void execute() {
            QBUsers.getUsersByTags(userTags, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet getUserWithExternalId = new Snippet("get user with external id") {
        @Override
        public void execute() {
            QBUsers.getUserByExternalId(EXTERNAL_ID, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet updateUser = new Snippet("update user") {
        @Override
        public void execute() {
            QBUsers.updateUser(user, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

    Snippet deleteUserById = new Snippet("delete user by id") {
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

    Snippet deleteUserByExternalId = new Snippet("delete user by external id") {
        @Override
        public void execute() {
            QBUsers.deleteByExternalId(EXTERNAL_ID, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {

                }
            });
        }
    };

    Snippet resetPassword = new Snippet("reset password") {
        @Override
        public void execute() {
            QBUsers.resetPassword(EMAIL, new QBCallback() {
                @Override
                public void onComplete(Result result) {
                    printResultToConsole(result);
                }

                @Override
                public void onComplete(Result result, Object context) {
                }
            });
        }
    };

}