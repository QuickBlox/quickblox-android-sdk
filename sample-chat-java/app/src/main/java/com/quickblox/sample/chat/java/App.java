package com.quickblox.sample.chat.java;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.sample.chat.java.managers.BackgroundListener;
import com.quickblox.sample.chat.java.utils.ActivityLifecycle;

public class App extends Application {

    //Chat settings
    public static final String USER_DEFAULT_PASSWORD = "quickblox";
    public static final int CHAT_PORT = 5223;
    public static final int SOCKET_TIMEOUT = 300;
    public static final boolean KEEP_ALIVE = true;
    public static final boolean USE_TLS = true;
    public static final boolean AUTO_JOIN = false;
    public static final boolean AUTO_MARK_DELIVERED = true;
    public static final boolean RECONNECTION_ALLOWED = true;
    public static final boolean ALLOW_LISTEN_NETWORK = true;

    //App credentials
    private static final String APPLICATION_ID = "";
    private static final String AUTH_KEY = "";
    private static final String AUTH_SECRET = "";
    private static final String ACCOUNT_KEY = "";

    //Chat settings range
    private static final int MAX_PORT_VALUE = 65535;
    private static final int MIN_PORT_VALUE = 1000;
    private static final int MIN_SOCKET_TIMEOUT = 300;
    private static final int MAX_SOCKET_TIMEOUT = 60000;

    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        ActivityLifecycle.init(this);
        checkAppCredentials();
        checkChatSettings();
        initCredentials();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new BackgroundListener());
    }

    private void initApplication() {
        instance = this;
    }

    private void checkAppCredentials() {
        if (APPLICATION_ID.isEmpty() || AUTH_KEY.isEmpty() || AUTH_SECRET.isEmpty() || ACCOUNT_KEY.isEmpty()) {
            throw new AssertionError(getString(R.string.error_qb_credentials_empty));
        }
    }

    private void checkChatSettings() {
        if (USER_DEFAULT_PASSWORD.isEmpty() || (CHAT_PORT < MIN_PORT_VALUE || CHAT_PORT > MAX_PORT_VALUE)
                || (SOCKET_TIMEOUT < MIN_SOCKET_TIMEOUT || SOCKET_TIMEOUT > MAX_SOCKET_TIMEOUT)) {
            throw new AssertionError(getString(R.string.error_chat_credentails_empty));
        }
    }

    private void initCredentials() {
        QBSettings.getInstance().init(getApplicationContext(), APPLICATION_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);

        // Uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your_api_endpoint.com", "your_chat_endpoint", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
    }

    public static App getInstance() {
        return instance;
    }
}