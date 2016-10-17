package com.quickblox.sample.core.service;

import android.app.Activity;
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

    private static final String EXTRA_TYPE_CM = "extraTypeCM";
    private static final String EXTRA_SENDER_ID = "extraSenderId";
    private static final String EXTRA_QB_ENVIRONMENT = "extraQbEnvironment";
    private static final String EXTRA_SUBSCRIBE = "extraSubscribe";
    private static final int PLAY_SERVICES_REQUEST_CODE = 9000;


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


    public static void subscribeToPushes(Activity activity, Type typeCM, String senderId, QBEnvironment qbEnvironment) {
        if (!checkPlayServicesAvailable(activity)) {
            return;
        }
        Intent intent = new Intent(activity, SubscribeService.class);
        intent.putExtra(EXTRA_TYPE_CM, typeCM);
        intent.putExtra(EXTRA_SENDER_ID, senderId);
        intent.putExtra(EXTRA_QB_ENVIRONMENT, qbEnvironment);
        intent.putExtra(EXTRA_SUBSCRIBE, true);
        activity.startService(intent);
    }

    public static void unSubscribeToPushes(Context context, Type typeCM, String senderId) {
        Intent intent = new Intent(context, SubscribeService.class);
        intent.putExtra(EXTRA_TYPE_CM, typeCM);
        intent.putExtra(EXTRA_SENDER_ID, senderId);
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
            subscribeToQB(fcmRegId, qbEnvironment);
        }
    }


    private void unregisterCM(Type typeCM, String senderId) {
        if (typeCM == Type.GCM) {
            deleteTokenGCM(senderId);
        } else {
            deleteTokenFCM(senderId);
        }

        SharedPrefsHelper.getInstance(this).delete(PREF_GCM_REG_ID);

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
//        return instanceID.getToken();
    }

    private void subscribeToQB(String gcmRegId, QBEnvironment qbEnvironment) {
        QBSubscription qbSubscription = new QBSubscription();
        qbSubscription.setNotificationChannel(QBNotificationChannel.GCM);
        String androidId = Utils.generateDeviceId(CoreApp.getInstance());
        qbSubscription.setDeviceUdid(androidId);
        qbSubscription.setRegistrationID(gcmRegId);
        qbSubscription.setEnvironment(qbEnvironment);

        try {
            QBPushNotifications.createSubscription(qbSubscription).perform();
            Log.i(TAG, "Successfully subscribed for QB push messages");
            SharedPrefsHelper.getInstance(SubscribeService.this).save(PREF_GCM_REG_ID, gcmRegId);

        } catch (QBResponseException e) {
            Log.w(TAG, "Unable to subscribe for QB push messages; " + e.toString());
        }
    }

    private static boolean checkPlayServicesAvailable(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_REQUEST_CODE)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
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