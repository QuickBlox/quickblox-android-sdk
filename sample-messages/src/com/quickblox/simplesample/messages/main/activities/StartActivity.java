package com.quickblox.simplesample.messages.main.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.messages.model.QBDevice;
import com.quickblox.module.messages.model.QBPlatform;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.simplesample.messages.R;

import static com.quickblox.simplesample.messages.main.definitions.Consts.APP_ID;
import static com.quickblox.simplesample.messages.main.definitions.Consts.AUTH_KEY;
import static com.quickblox.simplesample.messages.main.definitions.Consts.AUTH_SECRET;


public class StartActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        String deviceUDID = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        QBUser qbUser = new QBUser();
        qbUser.setLogin("test");
        qbUser.setPassword("test");
        QBDevice qbDevice = new QBDevice();
        qbDevice.setId(deviceUDID);
        qbDevice.setPlatform(QBPlatform.ANDROID);

        // Create session with additional parameters
        QBAuth.authorizeApp(qbUser, qbDevice, new QBCallback() {
            @Override
            public void onComplete(Result result) {
                startMessageActivity();
            }

            @Override
            public void onComplete(Result result, Object o) {
            }
        });

    }

    private void startMessageActivity() {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
        finish();
    }
}