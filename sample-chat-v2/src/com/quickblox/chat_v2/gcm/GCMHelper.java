package com.quickblox.chat_v2.gcm;

import android.app.Activity;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;

/**
 * Created with IntelliJ IDEA.
 * User: Nikolay Dymura
 * Date: 5/27/13
 * E-mail: nikolay.dymura@gmail.com
 */
public final class GCMHelper {

    public static final String SENDER_ID = "328124915270";

    private static final String TAG = GCMHelper.class.getCanonicalName();

    private GCMHelper() {
    }

    public static void register(Activity activity) {
        try {
            GCMRegistrar.checkDevice(activity);
            GCMRegistrar.checkManifest(activity);

            final String regId = GCMRegistrar.getRegistrationId(activity);

            if (regId.equals("")) {
                GCMRegistrar.register(activity, SENDER_ID);
            } else {
                Log.v(TAG, "Already registered");
            }
        } catch (Exception e) {

        }
    }

    public static void unregister(Activity activity) {
        GCMRegistrar.unregister(activity);
    }
}
