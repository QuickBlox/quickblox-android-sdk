package com.quickblox.snippets.modules;

import android.content.Context;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.snippets.Snippet;
import com.quickblox.snippets.Snippets;

import java.util.ArrayList;

/**
 * User: Oleg Soroka
 * Date: 01.10.12
 * Time: 19:29
 */
public class SnippetsUsers extends Snippets {

    public SnippetsUsers(Context context) {
        super(context);

        snippets.add(signInUserWithLogin);
        snippets.add(signInUserWithEmail);
        snippets.add(signInUsingSocialProvider);
        snippets.add(signOut);
        snippets.add(signUpUser);

        snippets.add(getAllUsers);
        snippets.add(getUsersByIds);
        snippets.add(getUsersById);
        snippets.add(getUserWithLogin);
        snippets.add(getUsersWithFullName);
        snippets.add(getUserWithTwitterId);
        snippets.add(getUserWithFacebookId);
        snippets.add(getUserWithEmail);
        snippets.add(getUsersWithTags);
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

            QBUsers.signIn(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    }else{
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
            user.setEmail("test123@test.com");
            user.setPassword("testpassword");

            QBUsers.signIn(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    }else{
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

            QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User was successfully signed in, " + qbUserResult.getUser().toString());
                    }else{
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
                        System.out.println(">>> User was successfully signed out");
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet signUpUser = new Snippet("sign up user (register)") {
        @Override
        public void execute() {
            final QBUser user = new QBUser("testuser", "testpassword", "test123@test.com");

            QBUsers.signUp(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User was successfully signed up, " + qbUserResult.getUser().toString());
                    }else{
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
            pagedRequestBuilder.setCurrentPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsers(pagedRequestBuilder, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult)result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        System.out.println(">>> Users: " + users.toString());
                    }else{
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
            pagedRequestBuilder.setCurrentPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> userIds = new ArrayList<String>();
            userIds.add("123");
            userIds.add("8819");

            QBUsers.getUsersByIDs(userIds, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult)result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        System.out.println(">>> Users: " + users.toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUsersById = new Snippet("get user by id") {
        @Override
        public void execute() {
            QBUsers.getUser(546, new QBCallbackImpl() {
                @Override
                 public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithLogin = new Snippet("get user with login") {
        @Override
        public void execute() {
            QBUsers.getUserByLogin("testuser", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };


    Snippet getUsersWithFullName = new Snippet("get user with full name") {
        @Override
        public void execute() {
            QBUsers.getUsersByFullName("fullName", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserPagedResult usersResult = (QBUserPagedResult)result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        System.out.println(">>> Users: " + users.toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithTwitterId = new Snippet("get user with twitter id") {
        @Override
        public void execute() {
            QBUsers.getUserByTwitterId("5680203734", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithFacebookId = new Snippet("get user with facebook id") {
        @Override
        public void execute() {
            QBUsers.getUserByFacebookId("10000312314143", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithEmail = new Snippet("get user with email") {
        @Override
        public void execute() {
            QBUsers.getUserByEmail("test123@test.com", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
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
                        QBUserPagedResult usersResult = (QBUserPagedResult)result;
                        ArrayList<QBUser> users = usersResult.getUsers();
                        System.out.println(">>> Users: " + users.toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet getUserWithExternalId = new Snippet("get user with external id") {
        @Override
        public void execute() {
            QBUsers.getUserByExternalId("123145235", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
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
            user.setId(567);
            user.setFullName("Merelyn");
            QBUsers.updateUser(user, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        QBUserResult qbUserResult = (QBUserResult) result;
                        System.out.println(">>> User: " + qbUserResult.getUser().toString());
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet deleteUserById = new Snippet("delete user by id") {
        @Override
        public void execute() {
            QBUsers.deleteUser(562, new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> User was successfully deleted");
                    }else{
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
                        System.out.println(">>> User was successfully deleted");
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };

    Snippet resetPassword = new Snippet("reset password") {
        @Override
        public void execute() {
            QBUsers.resetPassword("test123@test.com", new QBCallbackImpl() {
                @Override
                public void onComplete(Result result) {
                    if (result.isSuccess()) {
                        System.out.println(">>> Email was sent");
                    }else{
                        handleErrors(result);
                    }
                }
            });
        }
    };
}