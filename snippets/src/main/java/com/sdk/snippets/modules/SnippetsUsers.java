package com.sdk.snippets.modules;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.QBEntityCallback;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.Digits;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Lo;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.Consts;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.sdk.snippets.core.ApplicationConfig;
import com.sdk.snippets.core.SnippetAsync;
import com.sdk.snippets.core.Snippet;
import com.sdk.snippets.core.Snippets;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Fabric;

/**
 * Created by vfite on 04.02.14.
 */

public class SnippetsUsers extends Snippets{
    private static final String TAG = SnippetsUsers.class.getSimpleName();

    private TwitterAuthConfig authConfig;

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
        snippets.add(signInUsingTwitterDigits);
        snippets.add(signInUsingTwitterDigitsSynchronous);
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
        snippets.add(getUsersWithTwitterDigitsIDs);
        snippets.add(getUsersWithTwitterDigitsIDsSynchronous);
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
        snippets.add(getUserWithTwitterDigitsId);
        snippets.add(getUserWithTwitterDigitsIdSynchronous);
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

            QBUsers.signIn(user).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle params) {
                    Log.i(TAG, ">>> User was successfully signed in:  " + user.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                       handleErrors(errors);
                }
            });
        }
    };

    Snippet signInUserWithLoginSynchronous = new SnippetAsync("sign in user (synchronous)", "with login" , context) {
        @Override
        public void executeAsync() {
            QBUser user = new QBUser();
            user.setLogin(ApplicationConfig.getInstance().getTestUserLogin1());
            user.setPassword(ApplicationConfig.getInstance().getTestUserPassword1());
            QBUser userResult = null;
            try {
                userResult =  QBUsers.signIn(user).perform();
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

            String email = "test987@test.com";
            String password = "testpassword";

            QBUsers.signInByEmail(email, password).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed in, " + user);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signInUserWithEmailSynchronous = new SnippetAsync("sign in user (synchronous)", "with email", context) {
        @Override
        public void executeAsync() {
            String email = "test987@test.com";
            String password = "testpassword";

            QBUser userResult = null;
            try {
                userResult =  QBUsers.signInByEmail(email, password).perform();
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

            QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed in, " + user);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };


    Snippet signInUsingSocialProviderSynchronous = new SnippetAsync("sign in user (synchronous)", "with social provider", context) {
        @Override
        public void executeAsync() {

            String facebookAccessToken = "AAAEra8jNdnkBABYf3ZBSAz9dgLfyK7tQNttIoaZA1cC40niR6HVS0nYuufZB0ZCn66VJcISM8DO2bcbhEahm2nW01ZAZC1YwpZB7rds37xW0wZDZD";

            QBUser userResult = null;
            try {
                userResult = QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, facebookAccessToken, null).perform();
            } catch (QBResponseException e) {
                setException(e);
            }
            if(userResult != null){
                Log.i(TAG, "User was successfully signed in,"+userResult);
            }
        }
    };


    //
    ////////////////////////// Sign in with Twitter Digits /////////////////////////////////
    //


    Snippet signInUsingTwitterDigits = new Snippet("sign in user", "with Twitter Digits") {
        @Override
        public void execute() {
            initTwitterDigits();
            authenticateWithTwitterDigits(false);

        }
    };

    Snippet signInUsingTwitterDigitsSynchronous = new SnippetAsync("sign in user (synchronous)", "with Twitter Digits", context) {
        @Override
        public void executeAsync() {
            initTwitterDigits();
            authenticateWithTwitterDigits(true);
        }
    };

    private void initTwitterDigits() {
        if(authConfig == null) {
            // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
            String consumerKey = "A1NXq7BxZ74NZ3dDzXA1HcSN7";
            String consumerSecret = "Piuy52Kf2m2iHVKpfpffi6xjvOYVI904O6sl1c50TLpntTVsl6";

            authConfig = new TwitterAuthConfig(consumerKey, consumerSecret);
            Fabric.with(context, new TwitterCore(authConfig), new Digits());
        }
    }

    private void authenticateWithTwitterDigits(final boolean isSync) {
        Digits.authenticate(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                Map<String, String> authHeaders = getAuthHeadersBySession(session);

                Lo.g(authHeaders);

                String xAuthServiceProvider = authHeaders.get("X-Auth-Service-Provider");
                String xVerifyCredentialsAuthorization = authHeaders.get("X-Verify-Credentials-Authorization");

                if (isSync) {
                    QBUser user = null;
                    try {
                        user = QBUsers.signInUsingTwitterDigits(xAuthServiceProvider, xVerifyCredentialsAuthorization).perform();
                    } catch (QBResponseException e) {
                        e.printStackTrace();
                    }
                    if (session != null) {
                        Log.i(TAG, "user: " + user);
                    }
                } else {
                    QBUsers.signInUsingTwitterDigits(xAuthServiceProvider, xVerifyCredentialsAuthorization).performAsync(
                            new QBEntityCallback<QBUser>() {
                                @Override
                                public void onSuccess(QBUser user, Bundle params) {
                                    Log.i(TAG, "user: " + user);
                                }

                                @Override
                                public void onError(QBResponseException errors) {
                                    handleErrors(errors);
                                }
                            });
                }
            }

            @Override
            public void failure(DigitsException exception) {
                log(exception.getMessage());
            }
        }, "+38");
    }

    private Map<String, String> getAuthHeadersBySession(DigitsSession digitsSession) {
        TwitterAuthToken authToken = (TwitterAuthToken) digitsSession.getAuthToken();
        DigitsOAuthSigning oauthSigning = new DigitsOAuthSigning(authConfig, authToken);

        return oauthSigning.getOAuthEchoHeadersForVerifyCredentials();
    }


    //
    ///////////////////////////////////////// Sign Out /////////////////////////////////////////////
    //


    Snippet signOut = new Snippet("sign out") {
        @Override
        public void execute() {
            QBUsers.signOut().performAsync(new QBEntityCallback<Void>(){

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> User was successfully signed out");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signOutSynchronous = new SnippetAsync("sign out (synchronous)", context) {
        @Override
        public void executeAsync() {
            try {
                QBUsers.signOut().perform();
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
            StringifyArrayList tags = new StringifyArrayList();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");

            QBUsers.signUp(user).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed up, " + user);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet signUpUserSynchronous = new SnippetAsync("sign up user (synchronous)", context) {
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
            StringifyArrayList tags = new StringifyArrayList();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");
            QBUser qbUserResult = null;
            try {
                qbUserResult = QBUsers.signUp(user).perform();
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
            StringifyArrayList tags = new StringifyArrayList();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");

            QBUsers.signUpSignInTask(user).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User was successfully signed up and signed in, " + user);
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });


        }
    };

    Snippet signUpSignInUserSynchronous = new SnippetAsync("sign up and sign in user (synchronous)", context) {
        @Override
        public void executeAsync() {
            final QBUser user = new QBUser("te12stuser12344443", "testpassword");
//            user.setEmail("test1233456789w0@test.com");
//            user.setExternalId("02345777");
//            user.setFacebookId("1233453457767");
//            user.setTwitterId("12334635457");
            user.setFullName("fullName5");
            user.setPhone("+18904567812");
            StringifyArrayList tags = new StringifyArrayList();
            tags.add("firstTag");
            tags.add("secondTag");
            tags.add("thirdTag");
            tags.add("fourthTag");
            user.setTags(tags);
            user.setWebsite("website.com");


            QBUser userResult = null;
            try {
                userResult = QBUsers.signUpSignInTask(user).perform();
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
            StringifyArrayList<String> tags = new StringifyArrayList();
            tags.add("man");
            user.setTags(tags);
//            user.setWebsite("google.com");
//            user.setFileId(-1);

            QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>(){
                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user);
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet updateUserSynchronous = new SnippetAsync("update user (synchronous)", context) {
        @Override
        public void executeAsync() {
            final QBUser user = new QBUser();
            user.setId(53779);
            user.setFullName("galog");
            user.setCustomData("my new custom data");
            user.setWebsite("google.com");

            QBUser userResult = null;
            try {
                userResult = QBUsers.updateUser(user).perform();
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
            QBUsers.deleteUser(userId).performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> User was successfully deleted");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteUserByIdSynchronous = new SnippetAsync("delete user (synchronous)", "by id", context) {
        @Override
        public void executeAsync() {

            int userId = 562;

            try {
                QBUsers.deleteUser(userId).perform();
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
            QBUsers.deleteByExternalId("568965444").performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> User was successfully deleted");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet deleteUserByExternalIdSynchronous = new SnippetAsync("delete user (synchronous)", "by external id", context) {
        @Override
        public void executeAsync() {

            try {
                QBUsers.deleteByExternalId("568965444").perform();
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
            QBUsers.resetPassword("test987@test.com").performAsync(new QBEntityCallback<Void>() {

                @Override
                public void onSuccess(Void result, Bundle bundle) {
                    Log.i(TAG, ">>> Email was sent");
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet resetPasswordSynchronous = new SnippetAsync("reset password (synchronous)", context) {
        @Override
        public void executeAsync() {

            try {
                QBUsers.resetPassword("test987@test.com").perform();
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

            Bundle bundle = new Bundle();

            QBUsers.getUsers(pagedRequestBuilder, bundle).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {

                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalEntries: " + params.getInt(Consts.TOTAL_ENTRIES));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_PAGES));
                }

                @Override
                public void onError(QBResponseException errors) {
                   handleErrors(errors);
                }
            });


//            QBUsers.getUsersByFilter()
        }
    };

    Snippet getAllUsersSynchronous = new SnippetAsync("get users (synchronous)", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();
            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsers(pagedRequestBuilder, params).perform();
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

            List<Integer> usersIds = new ArrayList<>();
            usersIds.add(378);
            usersIds.add(379);
            usersIds.add(380);

            Bundle bundle = new Bundle();

            QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, bundle).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };


    Snippet getUsersByIdsSynchronous = new SnippetAsync("get users (synchronous)", "by ids", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            List<Integer> usersIds = new ArrayList<>();
            usersIds.add(378);
            usersIds.add(379);
            usersIds.add(380);

            Bundle params = new Bundle();
            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByIDs(usersIds, pagedRequestBuilder, params).perform();
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

            StringifyArrayList usersLogins = new StringifyArrayList();
            usersLogins.add("igorquickblox2");
            usersLogins.add("john");

            Bundle params = new Bundle();

            QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, params).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByLoginsSynchronous = new SnippetAsync("get users (synchronous)", "by logins", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            StringifyArrayList usersLogins = new StringifyArrayList();
            usersLogins.add("igorquickblox2");
            usersLogins.add("john");

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByLogins(usersLogins, pagedRequestBuilder, params).perform();
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

            StringifyArrayList usersEmails = new StringifyArrayList();
            usersEmails.add("asd@ffg.fgg");
            usersEmails.add("ghh@ggh.vbb");

            QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByEmailsSynchronous = new SnippetAsync("get users (synchronous)", "by emails", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            StringifyArrayList usersEmails = new StringifyArrayList();
            usersEmails.add("asd@ffg.fgg");
            usersEmails.add("ghh@ggh.vbb");

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByEmails(usersEmails, pagedRequestBuilder, params).perform();
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

            StringifyArrayList usersPhones = new StringifyArrayList();
            usersPhones.add("980028312");
            usersPhones.add("765172323");

            QBUsers.getUsersByPhoneNumbers(usersPhones, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }

            });
        }
    };

    Snippet getUsersByPhoneNumbersSynchronous = new SnippetAsync("get users (synchronous)", "by phone numbers", context) {
        @Override
        public void executeAsync() {
            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            StringifyArrayList usersPhones = new StringifyArrayList();
            usersPhones.add("980028312");
            usersPhones.add("765172323");;

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByPhoneNumbers(usersPhones, pagedRequestBuilder, params).perform();
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
            StringifyArrayList facebookIDs = new StringifyArrayList();
            facebookIDs.add("11020002022222");
            facebookIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByFacebookId(facebookIDs, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithFacebookIDsSynchronous = new SnippetAsync("get users (synchronous)", "with facebook IDs", context) {
        @Override
        public void executeAsync() {
            StringifyArrayList facebookIDs = new StringifyArrayList();
            facebookIDs.add("11020002022222");
            facebookIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByFacebookId(facebookIDs, pagedRequestBuilder, params).perform();
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
            StringifyArrayList twitterIDs = new StringifyArrayList();
            twitterIDs.add("11020002022222");
            twitterIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByTwitterId(twitterIDs, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithTwitterIDsSynchronous = new SnippetAsync("get users (synchronous)", "with twitter IDs", context) {
        @Override
        public void executeAsync() {
            StringifyArrayList twitterIDs = new StringifyArrayList();
            twitterIDs.add("11020002022222");
            twitterIDs.add("10000045345444");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByTwitterId(twitterIDs, pagedRequestBuilder, params).perform();
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
    //////////////////////////////////////// Get users with Twitter Digits ID ///////////////////////////////////
    //


    Snippet getUsersWithTwitterDigitsIDs = new Snippet("get users", "with twitter digits IDs") {
        @Override
        public void execute() {
            ArrayList<String> twitterDigitsIDs = new ArrayList<String>();
            twitterDigitsIDs.add("3533173695");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByTwitterDigitsId(twitterDigitsIDs, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithTwitterDigitsIDsSynchronous = new SnippetAsync("get users (synchronous)", "with twitter digits IDs", context) {
        @Override
        public void executeAsync() {
            ArrayList<String> twitterDigitsIDs = new ArrayList<String>();
            twitterDigitsIDs.add("3533173695");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByTwitterDigitsId(twitterDigitsIDs, pagedRequestBuilder, params).perform();
            } catch (QBResponseException e) {
                handleErrors(e);
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
            StringifyArrayList userTags = new StringifyArrayList();
            userTags.add("man");
//            userTags.add("car");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            QBUsers.getUsersByTags(userTags, pagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithTagsSynchronous = new SnippetAsync("get users (synchronous)", "with tags", context) {
        @Override
        public void executeAsync() {
            StringifyArrayList userTags = new StringifyArrayList();
            userTags.add("man");
            userTags.add("car");

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = (ArrayList<QBUser>) QBUsers.getUsersByTags(userTags, pagedRequestBuilder, params).perform();
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
            QBUsers.getUsersByFullName(fullName, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                    Log.i(TAG, ">>> Users: " + users.toString());
                    Log.i(TAG, "currentPage: " + params.getInt(Consts.CURR_PAGE));
                    Log.i(TAG, "perPage: " + params.getInt(Consts.PER_PAGE));
                    Log.i(TAG, "totalPages: " + params.getInt(Consts.TOTAL_ENTRIES));
                }

                @Override
                public void onError(QBResponseException errors) {
                    handleErrors(errors);
                }
            });
        }
    };

    Snippet getUsersWithFullNameSynchronous = new SnippetAsync("get users (synchronous)", "with fullname", context) {
        @Override
        public void executeAsync() {

            QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
            pagedRequestBuilder.setPage(1);
            pagedRequestBuilder.setPerPage(10);

            Bundle params = new Bundle();

            ArrayList<QBUser> users = null;
            try {
                users = QBUsers.getUsersByFullName("bob", pagedRequestBuilder, params).perform();
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
            QBUsers.getUser(53779).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserByIdSynchronous = new SnippetAsync("get user (synchronous)", "by id", context) {
        @Override
        public void executeAsync() {
            QBUser user = null;
            try {
                user = QBUsers.getUser(53779).perform();
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
            QBUsers.getUserByLogin(login).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithLoginSynchronous = new SnippetAsync("get user (synchronous)", "with login", context) {
        @Override
        public void executeAsync() {
            String login = "testuser";

            QBUser user = null;
            try {
                user = QBUsers.getUserByLogin(login).perform();
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
            QBUsers.getUserByTwitterId(twitterId).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithTwitterIdSynchronous = new SnippetAsync("get user (synchronous)", "with twitter id", context) {
        @Override
        public void executeAsync() {
            String twitterId = "56802037340";

            QBUser user = null;
            try {
                user = QBUsers.getUserByTwitterId(twitterId).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };


    //
    //////////////////////////////////////// Get user with twitter digits ID //////////////////////////////
    //


    Snippet getUserWithTwitterDigitsId = new Snippet("get user", "with twitter digits id") {
        @Override
        public void execute() {
            String twitterDigitsId = "3533173695";
            QBUsers.getUserByTwitterDigitsId(twitterDigitsId).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }

                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithTwitterDigitsIdSynchronous = new SnippetAsync("get user (synchronous)", "with twitter digits id", context) {
        @Override
        public void executeAsync() {
            String twitterDigitsId = "3533173695";

            QBUser user = null;
            try {
                user = QBUsers.getUserByTwitterDigitsId(twitterDigitsId).perform();
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
            QBUsers.getUserByFacebookId(facebookId).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithFacebookIdSynchronous = new SnippetAsync("get user (synchronous)", "with facebook id", context) {
        @Override
        public void executeAsync() {
            String facebookId = "100003123141430";

            QBUser user = null;
            try {
                user = QBUsers.getUserByFacebookId(facebookId).perform();
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
            QBUsers.getUserByEmail(email).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithEmailSynchronous = new SnippetAsync("get user (synchronous)", "with email", context) {

        QBUser userByEmail;
        @Override
        public void executeAsync() {
            String email = "test123@test.com";
            try {
                userByEmail = QBUsers.getUserByEmail(email).perform();
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
            QBUsers.getUserByExternalId(externalId).performAsync(new QBEntityCallback<QBUser>() {

                @Override
                public void onSuccess(QBUser user, Bundle args) {
                    Log.i(TAG, ">>> User: " + user.toString());
                }
                @Override
                public void onError(QBResponseException errors) {
                }
            });
        }
    };

    Snippet getUserWithExternalIdSynchronous = new SnippetAsync("get user (synchronous)", "with external id", context) {
        @Override
        public void executeAsync() {
            String externalId = "123145235";

            QBUser user = null;
            try {
                user = QBUsers.getUserByExternalId(externalId).perform();
            } catch (QBResponseException e) {
                setException(e);
            }

            if(user != null){
                Log.i(TAG, ">>> User: " + user.toString());
            }
        }
    };
}
