package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.Consts;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.AsyncSnippet;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vfite on 04.02.14.
 */

public class SnippetsUsers extends Snippets{
    private static final String TAG = SnippetsUsers.class.getSimpleName();

    public SnippetsUsers(Context context) {
        super(context);

        snippets.add(signInUserWithLogin);
        snippets.add(signInUserWithLoginSynchronous);
        //
        snippets.add(signInUserWithEmail);
        snippets.add(signInUserWithEmailSynchronous);
        //
        snippets.add(signInUsingSocialProvider);
        snippets.add(signInUsingSocialProviderSynchronous);
        //
        snippets.add(signOut);
        snippets.add(signOutSynchronous);
        //
        snippets.add(signUpUserNewCallback);
        snippets.add(signUpUserSynchronous);
        //
        snippets.add(signUpSignInUser);
        snippets.add(signUpSignInUserSynchronous);
        //
        //
        snippets.add(updateUser);
        snippets.add(updateUserSynchronous);
        //
        //
        snippets.add(deleteUserById);
        snippets.add(deleteUserByIdSynchronous);
        //
        snippets.add(deleteUserByExternalId);
        snippets.add(deleteUserByExternalIdSynchronous);
        //
        //
        snippets.add(resetPassword);
        snippets.add(resetPasswordSynchronous);
        //
        //
        snippets.add(getAllUsers);
        snippets.add(getAllUsersSynchronous);
        //
        snippets.add(getUsersByIds);
        snippets.add(getUsersByIdsSynchronous);
        //
        snippets.add(getUsersByLogins);
        snippets.add(getUsersByLoginsSynchronous);
        //
        snippets.add(getUsersByEmails);
        snippets.add(getUsersByEmailsSynchronous);
        //
        snippets.add(getUsersByPhoneNumbers);
        snippets.add(getUsersByPhoneNumbersSynchronous);
        //
        snippets.add(getUsersWithFacebookIDs);
        snippets.add(getUsersWithFacebookIDsSynchronous);
        //
        snippets.add(getUsersWithTwitterIDs);
        snippets.add(getUsersWithTwitterIDsSynchronous);
        //
        snippets.add(getUsersWithTags);
        snippets.add(getUsersWithTagsSynchronous);
        //
        snippets.add(getUsersWithFullName);
        snippets.add(getUsersWithFullNameSynchronous);
        //
        //
        snippets.add(getUserById);
        snippets.add(getUserByIdSynchronous);
        //
        snippets.add(getUserWithLogin);
        snippets.add(getUserWithLoginSynchronous);
        //
        snippets.add(getUserWithTwitterId);
        snippets.add(getUserWithTwitterIdSynchronous);
        //
        snippets.add(getUserWithFacebookId);
        snippets.add(getUserWithFacebookIdSynchronous);
        //
        snippets.add(getUserWithEmail);
        snippets.add(getUserWithEmailSynchronous);
        //
        snippets.add(getUserWithExternalId);
        snippets.add(getUserWithExternalIdSynchronous);

    }


    //
    ///////////////////////////////// Sign In with login ///////////////////////////////////////////
    //


    Snippet signInUserWithLogin = new Snippet("sign in user", "with login") {
        @Override
        public void execute() {

            final QBUser user = new QBUser(ApplicationConfig.getInstance().getTestUserLogin1(),
                    ApplicationConfig.getInstance().getTestUserPassword1());

            final QBRequestCanceler canceler = QBUsers.signIn(user, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle params) {
                    Log.i(TAG, ">>> User was successfully signed in:  " + user.toString());
                }

                @Override
                public void onError(List<String> errors) {
                       handleErrors(errors);
                }
            });
        }
    };

    Snippet signInUserWithLoginSynchronous = new AsyncSnippet("sign in user (synchronous)", "with login" , context) {
        @Override
        public void executeAsync() {
            QBUser user = new QBUser();
            user.setLogin(ApplicationConfig.getInstance().getTestUserLogin1());
            user.setPassword(ApplicationConfig.getInstance().getTestUserPassword1());
            QBUser userResult = null;
            try {
                userResult =  QBUsers.signIn(user);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(userResult != null){
                Log.i(TAG, "User was successfully signed in: "+userResult);
            }
        }
    };


    //
    ///////////////////////////////// Sign In with email ///////////////////////////////////////////
    //


    Snippet signInUserWithEmail = new Snippet("sign in user", "with email") {
        @Override
        public void execute() {

            final QBUser user = new QBUser();
            user.setEmail("test987@test.com");
            user.setPassword("testpassword");

            QBUsers.signIn(user, new QBEntityCallbackImpl<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed in, " + user);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signInUserWithEmailSynchronous = new AsyncSnippet("sign in user (synchronous)", "with email", context) {
        @Override
        public void executeAsync() {
            QBUser user = new QBUser();
            user.setEmail("test987@test.com");
            user.setPassword("testpassword");
            QBUser userResult = null;
            try {
                userResult =  QBUsers.signIn(user);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(userResult != null){
                Log.i(TAG, "User was successfully signed in,"+userResult);
            }
        }
    };


    //
    ///////////////////////////////// Sign In with social provider /////////////////////////////////
    //


    Snippet signInUsingSocialProvider = new Snippet("sign in user", "with social provider") {
        @Override
        public void execute() {
            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null, new QBEntityCallbackImpl<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed in, " + user);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };


    Snippet signInUsingSocialProviderSynchronous = new AsyncSnippet("sign in user (synchronous)", "with social provider", context) {
        @Override
        public void executeAsync() {

            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBUser userResult = null;
            try {
                userResult = QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(userResult != null){
                Log.i(TAG, "User was successfully signed in,"+userResult);
            }
        }
    };


    //
    ///////////////////////////////////////// Sign Out /////////////////////////////////////////////
    //


    Snippet signOut = new Snippet("sign out") {
        @Override
        public void execute() {
            QBUsers.signOut(new QBEntityCallbackImpl(){

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> User was successfully signed out");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signOutSynchronous = new AsyncSnippet("sign out (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBUsers.signOut();
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////////////// Sign Up //////////////////////////////////////////////
    //


    Snippet signUpUserNewCallback = new Snippet("sign up user") {
        @Override
        public void execute() {

            final QBUser user = new QBUser("testu33ser12344443", "testpassword");
//            user.setEmail("test123456789w0@test.com");
//            user.setExternalId("02345777");
//            user.setFacebookId("1233453457767");
//            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            user.setCustomData("my custom data");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");

            QBUsers.signUp(user, new QBEntityCallbackImpl<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed up, " + user);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signUpUserSynchronous = new AsyncSnippet("sign up user (synchronous)", context) {
        @Override
        public void executeAsync() {

            final QBUser user = new QBUser("tes33tuser12344443", "testpassword");
//            user.setEmail("test1234567589w0@test.com");
//            user.setExternalId("02345777");
//            user.setFacebookId("1233453457767");
//            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            user.setCustomData("my custom data");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");
            QBUser qbUserResult = null;
            try {
                qbUserResult = QBUsers.signUp(user);
            } catch (QBResponseException e) {
                setException(e);
            }
            if(qbUserResult != null){
                Log.i(TAG, ">>> User was successfully signed up, " + qbUserResult);
            }
        }
    };


    //
    ///////////////////////////////// Sign Up and Sign In task /////////////////////////////////////
    //


    Snippet signUpSignInUser = new Snippet("sign up and sign in user") {
        @Override
        public void execute() {

            final QBUser user = new QBUser("testu33ser12344443", "testpassword");
//            user.setEmail("test3123456789w0@test.com");
//            user.setExternalId("02345777");
//            user.setFacebookId("1233453457767");
//            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");

            QBUsers.signUpSignInTask(user, new QBEntityCallbackImpl<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed up and signed in, " + user);
                }

                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });


        }
    };

    Snippet signUpSignInUserSynchronous = new AsyncSnippet("sign up and sign in user (synchronous)", context) {
        @Override
        public void executeAsync() {
            final QBUser user = new QBUser("te12stuser12344443", "testpassword");
//            user.setEmail("test1233456789w0@test.com");
//            user.setExternalId("02345777");
//            user.setFacebookId("1233453457767");
//            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            StringifyArrayList<String> tags = new StringifyArrayList<String>();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");


            QBUser userResult = null;
            try {
                userResult = QBUsers.signUpSignInTask(user);
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };



    //
    ///////////////////////////////// Update user ///////////////////////////////////////////
    //

    Snippet updateUser = new Snippet("update user") {
        @Override
        public void execute() {
            final QBUser user = new QBUser();
            user.setId(1501966);
            user.setFullName("Monro");
//            user.setEmail("test987@te2st.com");
//            user.setExternalId("987");
//            user.setFacebookId("987");
//            user.setTwitterId("987");
//            user.setFullName("galog");
//            user.setPhone("+123123123");
//            user.setCustomData("my new custom data");
//            StringifyArrayList<String> tags = new StringifyArrayList<String>();
//            tags.add("man");
//            user.setTags(tags);
//            user.setWebsite("google.com");
//            user.setFileId(-1);

            QBUsers.updateUser(user, new QBEntityCallbackImpl<QBUser>(){
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user);
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateUserSynchronous = new AsyncSnippet("update user (synchronous)", context) {
        @Override
        public void executeAsync() {
            final QBUser user = new QBUser();
            user.setId(53779);
            user.setFullName("galog");
            user.setCustomData("my new custom data");
            user.setWebsite("google.com");

            QBUser userResult = null;
            try {
                userResult = QBUsers.updateUser(user);
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////// Delete user ///////////////////////////////////////////
    //


    Snippet deleteUserById = new Snippet("delete user", "by id") {
        @Override
        public void execute() {

            int userId = 562;
            QBUsers.deleteUser(userId, new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> User was successfully deleted");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteUserByIdSynchronous = new AsyncSnippet("delete user (synchronous)", "by id", context) {
        @Override
        public void executeAsync() {

            int userId = 562;

            try {
                QBUsers.deleteUser(userId);
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    ///////////////////////////////// Delete user ///////////////////////////////////////////
    //


    Snippet deleteUserByExternalId = new Snippet("delete user", "by external id") {
        @Override
        public void execute() {
            QBUsers.deleteByExternalId("568965444", new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> User was successfully deleted");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteUserByExternalIdSynchronous = new AsyncSnippet("delete user (synchronous)", "by external id", context) {
        @Override
        public void executeAsync() {

            try {
                QBUsers.deleteByExternalId("568965444");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    //////////////////////////////////////// Resey password ////////////////////////////////////////
    //


    Snippet resetPassword = new Snippet("reset password") {
        @Override
        public void execute() {
            QBUsers.resetPassword("test987@test.com", new QBEntityCallbackImpl() {

                @Override
                public void onSuccess() {
                    Log.i(TAG, ">>> Email was sent");
                }

                @Override
                public void onError(List errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet resetPasswordSynchronous = new AsyncSnippet("reset password (synchronous)", context) {
        @Override
        public void executeAsync() {

            try {
                QBUsers.resetPassword("test987@test.com");
            } catch (QBResponseException e) {
                setException(e);
            }
        }
    };


    //
    //////////////////////////////////////// Get users /////////////////////////////////////////////
    //


    Snippet getAllUsers = new Snippet("get users") {
        @Override
        public void execute() {

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(5);

            QBUsers.getUsers(pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {

                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalEntries: " + params.getInt(Consts.TOTAL_ENTRIES));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_PAGES));
                }

                @Override
                public void onError(List<String> errors) {
                   handleErrors(errors);
                }
            });

        }
    };

    Snippet getAllUsersSynchronous = new AsyncSnippet("get users (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();
            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsers(pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users by IDs //////////////////////////////////////
    //


    Snippet getUsersByIds = new Snippet("get users", "by ids") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            List<Integer> usersIds = new ArrayList<Integer>();
            usersIds.add(378);
            usersIds.add(379);
            usersIds.add(380);

            QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };


    Snippet getUsersByIdsSynchronous = new AsyncSnippet("get users (synchronous)", "by ids", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            List<Integer> usersIds = new ArrayList<Integer>();
            usersIds.add(378);
            usersIds.add(379);
            usersIds.add(380);

            Bundle params = new Bundle();
            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users by logins ///////////////////////////////////
    //


    Snippet getUsersByLogins = new Snippet("get users", "by logins") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersLogins = new ArrayList<String>();
            usersLogins.add("igorquickblox2");
            usersLogins.add("john");

            QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByLoginsSynchronous = new AsyncSnippet("get users (synchronous)", "by logins", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersLogins = new ArrayList<String>();
            usersLogins.add("igorquickblox2");
            usersLogins.add("john");

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
               setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users by emails ///////////////////////////////////
    //


    Snippet getUsersByEmails = new Snippet("get users", "by emails") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersEmails = new ArrayList<String>();
            usersEmails.add("asd@ffg.fgg");
            usersEmails.add("ghh@ggh.vbb");

            QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByEmailsSynchronous = new AsyncSnippet("get users (synchronous)", "by emails", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersEmails = new ArrayList<String>();
            usersEmails.add("asd@ffg.fgg");
            usersEmails.add("ghh@ggh.vbb");

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users by phone numbers ////////////////////////////
    //


    Snippet getUsersByPhoneNumbers = new Snippet("get users", "by phone numbers") {
        @Override
        public void execute() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersPhones = new ArrayList<String>();
            usersPhones.add("980028312");
            usersPhones.add("765172323");

            QBUsers.getUsersByPhoneNumbers(usersPhones, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByPhoneNumbersSynchronous = new AsyncSnippet("get users (synchronous)", "by phone numbers", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            ArrayList<String> usersPhones = new ArrayList<String>();
            usersPhones.add("980028312");
            usersPhones.add("765172323");;

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByPhoneNumbers(usersPhones, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users with Facebook IDs ///////////////////////////////////
    //


    Snippet getUsersWithFacebookIDs = new Snippet("get users", "with facebook IDs") {
        @Override
        public void execute() {
            ArrayList<String> facebookIDs = new ArrayList<String>();
            facebookIDs.add("11020002022222");
            facebookIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByFacebookId(facebookIDs, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithFacebookIDsSynchronous = new AsyncSnippet("get users (synchronous)", "with facebook IDs", context) {
        @Override
        public void executeAsync() {
            ArrayList<String> facebookIDs = new ArrayList<String>();
            facebookIDs.add("11020002022222");
            facebookIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByFacebookId(facebookIDs, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users with Twitter IDs ///////////////////////////////////
    //


    Snippet getUsersWithTwitterIDs = new Snippet("get users", "with twitter IDs") {
        @Override
        public void execute() {
            ArrayList<String> twitterIDs = new ArrayList<String>();
            twitterIDs.add("11020002022222");
            twitterIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByTwitterId(twitterIDs, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithTwitterIDsSynchronous = new AsyncSnippet("get users (synchronous)", "with twitter IDs", context) {
        @Override
        public void executeAsync() {
            ArrayList<String> twitterIDs = new ArrayList<String>();
            twitterIDs.add("11020002022222");
            twitterIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByTwitterId(twitterIDs, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users with tags ///////////////////////////////////
    //


    Snippet getUsersWithTags = new Snippet("get users", "with tags") {
        @Override
        public void execute() {
            ArrayList<String> userTags = new ArrayList<String>();
            userTags.add("man");
//            userTags.add("car");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByTags(userTags, pagedRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithTagsSynchronous = new AsyncSnippet("get users (synchronous)", "with tags", context) {
        @Override
        public void executeAsync() {
            ArrayList<String> userTags = new ArrayList<String>();
            userTags.add("man");
            userTags.add("car");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByTags(userTags, pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get users with fullname ///////////////////////////////////
    //


    Snippet getUsersWithFullName = new Snippet("get users", "with full name") {
        @Override
        public void execute() {
            String fullName = "bob";
            QBUsers.getUsersByFullName(fullName, null,  new QBEntityCallbackImpl<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(List<String> errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithFullNameSynchronous = new AsyncSnippet("get users (synchronous)", "with fullname", context) {
        @Override
        public void executeAsync() {

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByFullName("bob", pagedRequestBuilder, params);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(users != null){
                Log.i(TAG, ">>> Users: " + users.toString());
                Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
            }
        }
    };


    //
    //////////////////////////////////////// Get user by ID ////////////////////////////////////////
    //


    Snippet getUserById = new Snippet("get user", "by id") {
        @Override
        public void execute() {
            QBUsers.getUser(53779, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserByIdSynchronous = new AsyncSnippet("get user (synchronous)", "by id", context) {
        @Override
        public void executeAsync() {
            QBUser user = null;
            try {
                user = QBUsers.getUser(53779);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with login ///////////////////////////////////
    //


    Snippet getUserWithLogin = new Snippet("get user", "with login") {
        @Override
        public void execute() {
            String login = "testuser";
            QBUsers.getUserByLogin(login, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserWithLoginSynchronous = new AsyncSnippet("get user (synchronous)", "with login", context) {
        @Override
        public void executeAsync() {
            String login = "testuser";

            QBUser user = null;
            try {
                user = QBUsers.getUserByLogin(login);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with twitter ID //////////////////////////////
    //


    Snippet getUserWithTwitterId = new Snippet("get user", "with twitter id") {
        @Override
        public void execute() {
            String twitterId = "56802037340";
            QBUsers.getUserByTwitterId(twitterId, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserWithTwitterIdSynchronous = new AsyncSnippet("get user (synchronous)", "with twitter id", context) {
        @Override
        public void executeAsync() {
            String twitterId = "56802037340";

            QBUser user = null;
            try {
                user = QBUsers.getUserByTwitterId(twitterId);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with facebook ID /////////////////////////////
    //


    Snippet getUserWithFacebookId = new Snippet("get user", "with facebook id") {
        @Override
        public void execute() {
            String facebookId = "100003123141430";
            QBUsers.getUserByFacebookId(facebookId, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserWithFacebookIdSynchronous = new AsyncSnippet("get user (synchronous)", "with facebook id", context) {
        @Override
        public void executeAsync() {
            String facebookId = "100003123141430";

            QBUser user = null;
            try {
                user = QBUsers.getUserByFacebookId(facebookId);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with email /////////////////////////////
    //


    Snippet getUserWithEmail = new Snippet("get user", "with email") {
        @Override
        public void execute() {
            String email = "test123@test.com";
            QBUsers.getUserByEmail(email, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserWithEmailSynchronous = new AsyncSnippet("get user (synchronous)", "with email", context) {

        QBUser userByEmail;
        @Override
        public void executeAsync() {
            String email = "test123@test.com";
            try {
                userByEmail = QBUsers.getUserByEmail(email);
            } catch (QBResponseException e) {
                setException(e);
            }
        }

        @Override
        protected void postExecute() {
            super.postExecute();
            if( userByEmail != null){
                Log.i(TAG, ">>> User: " + userByEmail.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with external ID /////////////////////////////
    //


    Snippet getUserWithExternalId = new Snippet("get user", "with external id") {
        @Override
        public void execute() {
            String externalId = "123145235";
            QBUsers.getUserByExternalId(externalId, new QBEntityCallbackImpl<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(List<String> errors) {
                    super.onError(errors);
                }
            });
        }
    };

    Snippet getUserWithExternalIdSynchronous = new AsyncSnippet("get user (synchronous)", "with external id", context) {
        @Override
        public void executeAsync() {
            String externalId = "123145235";

            QBUser user = null;
            try {
                user = QBUsers.getUserByExternalId(externalId);
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };
}
