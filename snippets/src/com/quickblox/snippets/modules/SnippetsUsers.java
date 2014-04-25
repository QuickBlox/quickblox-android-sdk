package com.quickblox.snippets.modules;

import android.content.Context;
import android.util.Log;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:29
 */
public class SnippetsUsers extends Snippets {

    private static final String TAG = SnippetsUsers.class.getSimpleName();

    public SnippetsUsers(Context context) {
        super(context);

        snippets.add(signInUserWithLogin);
        snippets.add(signInUserWithEmail);
        snippets.add(signInUsingSocialProvider);
        snippets.add(signOut);
        snippets.add(signUpUser);

        snippets.add(getAllUsers);
        snippets.add(getUsersByIds);
        snippets.add(getUsersByLogins);
        snippets.add(getUsersByEmails);
        snippets.add(getUsersWithFullName);
        snippets.add(getUsersWithTags);

        snippets.add(getUserById);
        snippets.add(getUserWithLogin);
        snippets.add(getUserWithTwitterId);
        snippets.add(getUserWithFacebookId);
        snippets.add(getUserWithEmail);
        snippets.add(getUserWithExternalId);

        snippets.add(updateUser);

        snippets.add(deleteUserById);
        snippets.add(deleteUserByExternalId);

        snippets.add(resetPassword);
    }

    Snippet signInUserWithLogin = new Snippet("sign in user (login)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser("testuser", "testpassword");

            final QBRequestCanceler canceler = QBUsers.signIn(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;

                        Log.i(TAG, ">>> lastRequestedAt, " + qbUserResult.getUser().getLastRequestAt());

                        Log.i(TAG, ">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet signInUserWithEmail = new Snippet("sign in user (email)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser();
            user.setEmail("test987@test.com");
            user.setPassword("testpassword");

            QBUsers.signIn(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet signInUsingSocialProvider = new Snippet("sign in using social provider") {
        @Override
        public void execute() {
            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBUsers.signInUsingSocialProvider(QBProvider.TWITTER, facebookAccessToken, null, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet signOut = new Snippet("sign out") {
        @Override
        public void execute() {
            QBUsers.signOut(new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> User was successfully signed out");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet signUpUser = new Snippet("sign up user (register)") {
        @Override
        public void execute() {

            final QBUser user = new QBUser("testuser12344443", "testpassword");
            user.setEmail("test123456789w0@test.com");
            user.setExternalId("02345777");
            user.setFacebookId("1233453457767");
            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");

            QBUsers.signUp(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;

                        Log.i(TAG, ">>> User was successfully signed up, " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getAllUsers = new Snippet("get all users") {
        @Override
        public void execute() {

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(2);
            pagedRequestBuilder.setPerPage(5);

            QBUsers.getUsers(pagedRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());


                        Log.i(TAG, "currentPage: " + usersResult.getCurrentPage());
                        Log.i(TAG, "totalEntries: " + usersResult.getTotalEntries());
                        Log.i(TAG, "perPage: " + usersResult.getPerPage());
                        Log.i(TAG, "totalPages: " + usersResult.getTotalPages());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet getUsersByIds = new Snippet("get users by ids") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            List<Integer> usersIds = new ArrayList<Integer>();
            usersIds.add(378);
            usersIds.add(379);
            usersIds.add(380);

            QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUsersByLogins = new Snippet("get users by logins") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersLogins = new ArrayList<String>();
            usersLogins.add("bob");
            usersLogins.add("john");

            QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUsersByEmails = new Snippet("get users by emails") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersEmails = new ArrayList<String>();
            usersEmails.add("asd@ffg.fgg");
            usersEmails.add("ghh@ggh.vbb");

            QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUsersWithFullName = new Snippet("get user with full name") {
        @Override
        public void execute() {
            String fullName = "fullName";
            QBUsers.getUsersByFullName(fullName, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUsersWithTags = new Snippet("get users with tags") {
        @Override
        public void execute() {
            ArrayList<String> userTags = new ArrayList<String>();
            userTags.add("man");
            userTags.add("car");

            QBUsers.getUsersByTags(userTags, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult) result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        Log.i(TAG, ">>> Users: " + users.toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet getUserById = new Snippet("get user by id") {
        @Override
        public void execute() {
            QBUsers.getUser(53779, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithLogin = new Snippet("get user with login") {
        @Override
        public void execute() {
            String login = "testuser";
            QBUsers.getUserByLogin(login, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithTwitterId = new Snippet("get user with twitter id") {
        @Override
        public void execute() {
            String twitterId = "56802037340";
            QBUsers.getUserByTwitterId(twitterId, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithFacebookId = new Snippet("get user with facebook id") {
        @Override
        public void execute() {
            String facebookId = "100003123141430";
            QBUsers.getUserByFacebookId(facebookId, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithEmail = new Snippet("get user with email") {
        @Override
        public void execute() {
            String email = "test123@test.com";
            QBUsers.getUserByEmail(email, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithExternalId = new Snippet("get user with external id") {
        @Override
        public void execute() {
            String externalId = "123145235";
            QBUsers.getUserByExternalId(externalId, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet updateUser = new Snippet("update user") {
        @Override
        public void execute() {
            final QBUser user = new QBUser();
            user.setId(53779);
            user.setFullName("Monro");
            user.setEmail("test987@test.com");
            user.setExternalId("987");
            user.setFacebookId("987");
            user.setTwitterId("987");
            user.setFullName("galog");
            user.setPhone("+123123123");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("man");
            user.setTags(tags);

            user.setWebsite("google.com");


            QBUsers.updateUser(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;

                        Log.i(TAG, ">>> updatedAt: " + qbUserResult.getUser().getUpdatedAt());
                        Log.i(TAG, ">>> createdAt: " + qbUserResult.getUser().getCreatedAt());

                        Log.i(TAG, ">>> User: " + qbUserResult.getUser().toString());
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteUserById = new Snippet("delete user by id") {
        @Override
        public void execute() {

            int userId = 562;
            QBUsers.deleteUser(userId, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> User was successfully deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet deleteUserByExternalId = new Snippet("delete user by external id") {
        @Override
        public void execute() {
            QBUsers.deleteByExternalId("568965444", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> User was successfully deleted");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet resetPassword = new Snippet("reset password") {
        @Override
        public void execute() {
            QBUsers.resetPassword("test987@test.com", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        Log.i(TAG, ">>> Email was sent");
                    } else {
                        handleErrors(result);
                    }
                }
            });
        }
    };
}