package com.quickblox.simplesample.messages;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.sample.core.CoreApp;

import java.util.ArrayList;

public class QbUtils {

    private static final String TAG = QbUtils.class.getSimpleName();

    public static void subscribeToPushMessages(String gcmRegId, QBEntityCallback<ArrayList<QBSubscription>> callback) {
        TelephonyManager telephonyManager = (TelephonyManager) CoreApp.getInstance()
                .getSystemService(Context.TELEPHONY_SERVICE);
        String uniqueDeviceId = telephonyManager.getDeviceId();
        if (TextUtils.isEmpty(uniqueDeviceId)) {
            ContentResolver cr = App.getInstance().getContentResolver();
            uniqueDeviceId = Settings.Secure.getString(cr, Settings.Secure.ANDROID_ID); // for tablets
        }

        QBMessages.subscribeToPushNotificationsTask(gcmRegId, uniqueDeviceId,
                // Don't forget to change QBEnvironment to PRODUCTION when releasing application
                QBEnvironment.DEVELOPMENT,
                callback);
    }

}
