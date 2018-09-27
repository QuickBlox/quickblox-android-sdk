package com.quickblox.sample.pushnotifications.activities;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBPushNotifications;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBNotificationChannel;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.core.ui.activity.CoreSplashActivity;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.configs.CoreConfigUtils;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.sample.pushnotifications.R;
import com.quickblox.sample.pushnotifications.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class SplashActivity extends CoreSplashActivity {
    private String message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            message = getIntent().getExtras().getString(GcmConsts.EXTRA_GCM_MESSAGE);
        }

        if (checkConfigsWithSnackebarError()){
            signInQB();
        }
    }

    private void signInQB() {
        if (!checkSignIn()) {
            QBUser qbUser = CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME);

            QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    //testCreateSubscription(qbUser);
                    proceedToTheNextActivity();
                }

                @Override
                public void onError(QBResponseException e) {
                    showSnackbarError(null, R.string.splash_create_session_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signInQB();
                        }
                    });
                }
            });
        } else {
            proceedToTheNextActivityWithDelay();
        }
    }

    private void testCreateSubscription(QBUser qbUser) {
        String deviceId = "";
        QBSubscription subscription = new QBSubscription(QBNotificationChannel.GCM);
        subscription.setEnvironment(QBEnvironment.DEVELOPMENT);
        //
        //try {
            deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            deviceId = ((TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();

        //} catch (SecurityException securityException) {
        //    Toaster.shortToast("Security Exception");
        //}
        if(deviceId == null && deviceId.length() < 1){
            deviceId = "UniversalDeviceId";
        }
        subscription.setDeviceUdid(deviceId);
        /*//
        String registrationID = "APA91bGr9AcS9Wgv4p4BkBQAg_1YrJZpfa5GMXg7LAQU0lya8gbf9Iw1360602PunkWk_NOsLS2xEK8tPeBCBfSH4fobt7zW4KVlWGjUfR3itFbVa_UreBf6c-rZ8uP_0_vxPCO65ceqgnjvQqD6j8DjLykok7VF7UBBjsMZrTIFjKwmVeJqb1o";
        subscription.setRegistrationID(registrationID);*/

        QBPushNotifications.createSubscription(subscription).performAsync(new QBEntityCallback<ArrayList<QBSubscription>>() {

            @Override
            public void onSuccess(ArrayList<QBSubscription> subscriptions, Bundle args) {
                Toaster.shortToast("Custom Subscription Created");
                proceedToTheNextActivity();
            }

            @Override
            public void onError(QBResponseException errors) {
                Toaster.shortToast("Error with Subscription creation");
            }
        });

    }

    @Override
    protected String getAppName() {
        return getString(R.string.app_title);
    }

    @Override
    protected void proceedToTheNextActivity() {
        MessagesActivity.start(this, message);
        finish();
    }

    @Override
    protected boolean sampleConfigIsCorrect() {
        boolean result = super.sampleConfigIsCorrect();
        result = result && CoreConfigUtils.getUserFromConfig(Consts.SAMPLE_CONFIG_FILE_NAME) != null;
        return result;
    }


}