package com.quickblox.sample.chat.gcm;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.utils.Consts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GooglePlayServicesHelper {
    private static final String TAG = GooglePlayServicesHelper.class.getSimpleName();

    private static final String PREF_APP_VERSION = "appVersion";
    private static final String PREF_GCM_REG_ID = "registration_id";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public void registerForGcmIfPossible(Activity activity) {
        // Check device for Play Services APK.
        // If check succeeds, proceed with GCM registration.
        if (checkGooglePlayServices(activity)) {
            String gcmRegId = getGcmRegIdFromPreferences();
            if (TextUtils.isEmpty(gcmRegId)) {
                registerInGcmInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     *
     * @param activity activity where you check Google Play Services availability
     */
    public boolean checkGooglePlayServices(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            Log.i(TAG, "This device is not supported.");
            activity.finish();
        }
        return false;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInGcmInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(App.getInstance());
                    return gcm.register(Consts.GCM_SENDER_ID);
                } catch (IOException e) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    Log.w(TAG, e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String gcmRegId) {
                if (TextUtils.isEmpty(gcmRegId)) {
                    Log.w(TAG, "Device wasn't registered in GCM");
                } else {
                    Log.i(TAG, "Device registered in GCM, regId=" + gcmRegId);
                    subscribeToQbPushNotifications(gcmRegId);
                    saveGcmRegIdToPreferences(gcmRegId);
                }
            }
        }.execute();
    }

    /**
     * @return Application's {@code SharedPreferences}
     */
    private SharedPreferences getSharedPreferences() {
        // This sample app persists gcmRegistrationId in shared preferences,
        // but how you store gcmRegistrationId in your app is up to you
        Context context = App.getInstance();
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    /**
     * Subscribe to Push Notifications
     *
     * @param gcmRegId registration ID
     */
    private void subscribeToQbPushNotifications(String gcmRegId) {
        TelephonyManager telephonyManager = (TelephonyManager) App.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        String uniqueDeviceId = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(uniqueDeviceId)) {
            ContentResolver cr = App.getInstance().getContentResolver();
            uniqueDeviceId = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID); // for tablets
        }

        // Don't forget to change QBEnvironment environment to PRODUCTION when releasing application
        QBMessages.subscribeToPushNotificationsTask(gcmRegId, uniqueDeviceId, QBEnvironment.DEVELOPMENT,
                new QBEntityCallbackImpl<ArrayList<QBSubscription>>() {
                    @Override
                    public void onSuccess(ArrayList<QBSubscription> qbSubscriptions, Bundle bundle) {
                        Log.i(TAG, "Successfully subscribed for QB push messages");
                    }

                    @Override
                    public void onError(List<String> errors) {
                        Log.w(TAG, "Unable to subscribe for QB push messages; " + errors.toString());
                    }
                });
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param gcmRegId registration ID
     */
    private void saveGcmRegIdToPreferences(String gcmRegId) {
        int appVersion = App.getInstance().getAppVersion();
        Log.i(TAG, "Saving gcmRegId on app version " + appVersion);

        // We save both gcmRegId and current app version,
        // so we can check if app was updated next time we need to get gcmRegId
        SharedPreferences preferences = getSharedPreferences();
        preferences.edit()
                .putString(PREF_GCM_REG_ID, gcmRegId)
                .putInt(PREF_APP_VERSION, appVersion)
                .apply();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID or app was updated since the last gcm registration.
     */
    private String getGcmRegIdFromPreferences() {
        SharedPreferences prefs = getSharedPreferences();

        // Check if app was updated; if so, we must request new gcmRegId
        // since the existing gcmRegId is not guaranteed to work
        // with the new app version
        int registeredVersion = prefs.getInt(PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = App.getInstance().getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }

        return prefs.getString(PREF_GCM_REG_ID, "");
    }
}