package com.quickblox.sample.core.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Utils;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.core.CoreApp;

import java.io.IOException;

public class SubscribeService extends IntentService {
    private static final String TAG = SubscribeService.class.getSimpleName();

    private static final String PREF_GCM_REG_ID = "registration_id";
    private static final String PREF_SENDER_ID = "sender_id";
    private static final String PREF_TYPE_CM = "registration_type_cm";

    private static final String EXTRA_TYPE_CM = "extraTypeCM";
    private static final String EXTRA_SENDER_ID = "extraSenderId";
    private static final String EXTRA_QB_ENVIRONMENT = "extraQbEnvironment";
    private static final String EXTRA_SUBSCRIBE = "extraSubscribe";


    public SubscribeService() {
        super(TAG);
    }

    public enum Type {
        GCM,
        FCM
    }

    private Type typeCM;
    private String senderId;
    private QBEnvironment qbEnvironment;
    private boolean subscribe;


    public static void subscribeToPushes(Context context, Type typeCM, String senderId, QBEnvironment qbEnvironment) {
        if (!checkPlayServicesAvailable(context)) {
            return;
        }
        Intent intent = new Intent(context, SubscribeService.class);
        intent.putExtra(EXTRA_TYPE_CM, typeCM);
        intent.putExtra(EXTRA_SENDER_ID, senderId);
        intent.putExtra(EXTRA_QB_ENVIRONMENT, qbEnvironment);
        intent.putExtra(EXTRA_SUBSCRIBE, true);
        context.startService(intent);
    }

    public static void unSubscribeToPushes(Context context) {
        Intent intent = new Intent(context, SubscribeService.class);
        intent.putExtra(EXTRA_SUBSCRIBE, false);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate()");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent start: ");

        parseIntentExtras(intent);
        String gcmRegId = SharedPrefsHelper.getInstance(this).get(PREF_GCM_REG_ID, "");

        if (subscribe) {

            if (TextUtils.isEmpty(gcmRegId)) {
                registerCM(typeCM, senderId, qbEnvironment);
            } else {
                Log.d(TAG, "Subscribe already");
            }

        } else {

            if (!TextUtils.isEmpty(gcmRegId)) {
                String senderId = SharedPrefsHelper.getInstance(this).get(PREF_SENDER_ID, "");
                Type typeCM = Type.valueOf(SharedPrefsHelper.getInstance(this).get(PREF_TYPE_CM, ""));
                Log.d(TAG, "senderId=" + senderId + ", typeCM=" + typeCM);
                unregisterCM(typeCM, senderId);
            }
        }
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            subscribe = (boolean) intent.getSerializableExtra(EXTRA_SUBSCRIBE);
            typeCM = (Type) intent.getSerializableExtra(EXTRA_TYPE_CM);
            senderId = (String) intent.getSerializableExtra(EXTRA_SENDER_ID);
            qbEnvironment = (QBEnvironment) intent.getSerializableExtra(EXTRA_QB_ENVIRONMENT);
        }
    }

    private void registerCM(Type typeCM, String senderId, QBEnvironment qbEnvironment) {
        String fcmRegId = (typeCM == Type.GCM) ? getTokenGCM(senderId) : getTokenFCM(senderId);

        if (TextUtils.isEmpty(fcmRegId)) {
            Log.w(TAG, "Device wasn't registered in " + typeCM);
        } else {
            Log.i(TAG, "Device registered in " + typeCM + ", regId=" + fcmRegId);
            subscribeToQB(fcmRegId, typeCM, qbEnvironment);
        }
    }


    private void unregisterCM(Type typeCM, String senderId) {
        if (typeCM == Type.GCM) {
            deleteTokenGCM(senderId);
        } else {
            deleteTokenFCM(senderId);
        }

        SharedPrefsHelper.getInstance(this).delete(PREF_GCM_REG_ID);
        SharedPrefsHelper.getInstance(this).delete(PREF_SENDER_ID);
        SharedPrefsHelper.getInstance(this).delete(PREF_TYPE_CM);

    }

    private void deleteTokenGCM(String senderId) {
        InstanceID instanceID = InstanceID.getInstance(CoreApp.getInstance());
        try {
            instanceID.deleteToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteTokenFCM(String senderId) {
        FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
        try {
            instanceID.deleteToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "deleteTokenFCM success!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTokenGCM(String senderId) {
        try {
            InstanceID instanceID = InstanceID.getInstance(CoreApp.getInstance());
            return instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        } catch (IOException e) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            Log.w(TAG, e);
            return null;
        }
    }

    private String getTokenFCM(String senderId) {
        FirebaseInstanceId instanceID = FirebaseInstanceId.getInstance();
        try {
            return instanceID.getToken(senderId, FirebaseMessaging.INSTANCE_ID_SCOPE);
        } catch (IOException e) {
            Log.w(TAG, e);
            return null;
        }
    }

    private void subscribeToQB(String gcmRegId, Type typeCM, QBEnvironment qbEnvironment) {
        QBSubscription qbSubscription = new QBSubscription();
        qbSubscription.setNotificationChannel(QBNotificationChannel.GCM);
        String androidId = Utils.generateDeviceId(CoreApp.getInstance());
        qbSubscription.setDeviceUdid(androidId);
        qbSubscription.setRegistrationID(gcmRegId);
        qbSubscription.setEnvironment(qbEnvironment);

        try {
            QBPushNotifications.createSubscription(qbSubscription).perform();
            Log.i(TAG, "Successfully subscribed for QB push messages");
            SharedPrefsHelper.getInstance(this).save(PREF_GCM_REG_ID, gcmRegId);
            SharedPrefsHelper.getInstance(this).save(PREF_SENDER_ID, senderId);
            SharedPrefsHelper.getInstance(this).save(PREF_TYPE_CM, typeCM);

        } catch (QBResponseException e) {
            Log.w(TAG, "Unable to subscribe for QB push messages; " + e.toString());
        }
    }

    public static boolean checkPlayServicesAvailable(Context context) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(context);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                Log.i(TAG, "GooglePlayServices are not available");
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "Service onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }
}