package com.quickblox.sample.videochat.conference.java;

import android.app.Application;
import android.text.TextUtils;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.conference.ConferenceConfig;
import com.quickblox.sample.videochat.conference.java.managers.ActivityLifecycle;
import com.quickblox.sample.videochat.conference.java.managers.BackgroundListener;
import com.quickblox.sample.videochat.conference.java.managers.ChatHelper;
import com.quickblox.sample.videochat.conference.java.managers.DialogsManager;
import com.quickblox.sample.videochat.conference.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBDialogsHolder;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBDialogsHolderImpl;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBUsersHolder;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBUsersHolderImpl;

public class App extends Application {

    // chat settings
    public static final int CHAT_PORT = 5223;
    public static final int SOCKET_TIMEOUT = 300;
    public static final boolean KEEP_ALIVE = true;
    public static final boolean USE_TLS = true;
    public static final boolean AUTO_JOIN = false;
    public static final boolean AUTO_MARK_DELIVERED = true;
    public static final boolean RECONNECTION_ALLOWED = true;
    public static final boolean ALLOW_LISTEN_NETWORK = true;

    // chat settings range
    private static final int MAX_PORT_VALUE = 65535;
    private static final int MIN_PORT_VALUE = 1000;
    private static final int MIN_SOCKET_TIMEOUT = 300;
    private static final int MAX_SOCKET_TIMEOUT = 60000;

    // app credentials
    private static final String APPLICATION_ID = "";
    private static final String AUTH_KEY = "";
    private static final String AUTH_SECRET = "";
    private static final String ACCOUNT_KEY = "";
    private static final String SERVER_URL = "";

    public static final String USER_DEFAULT_PASSWORD = "quickblox";

    private SharedPrefsHelper sharedPrefsHelper;
    private QBUsersHolder qbUsersHolder;
    private QBDialogsHolder qbDialogsHolder;
    private ChatHelper chatHelper;
    private DialogsManager dialogsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ActivityLifecycle.init(this);
        checkAppCredentials();
        checkChatSettings();
        initCredentials();
        initConferenceConfig();
        initSharedPreferences();
        initUsersHolder();
        initDialogsHolder();
        initChatHelper();
        initDialogsManager();

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new BackgroundListener());
    }

    private void initSharedPreferences() {
        sharedPrefsHelper = new SharedPrefsHelper(getApplicationContext());
    }

    public SharedPrefsHelper getSharedPrefsHelper() {
        return sharedPrefsHelper;
    }

    private void initUsersHolder() {
        qbUsersHolder = new QBUsersHolderImpl();
    }

    public QBUsersHolder getQBUsersHolder() {
        return qbUsersHolder;
    }

    private void initDialogsHolder() {
        qbDialogsHolder = new QBDialogsHolderImpl();
    }

    public QBDialogsHolder getQBDialogsHolder() {
        return qbDialogsHolder;
    }

    private void initChatHelper() {
        chatHelper = new ChatHelper(getApplicationContext());
    }

    public ChatHelper getChatHelper() {
        return chatHelper;
    }

    private void initDialogsManager() {
        dialogsManager = new DialogsManager(getApplicationContext());
    }

    public DialogsManager getDialogsManager() {
        return dialogsManager;
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

        // uncomment and put your Api and Chat servers endpoints if you want to point the sample
        // against your own server.
        //
        // QBSettings.getInstance().setEndpoints("https://your.api.endpoint.com", "your.chat.endpoint.com", ServiceZone.PRODUCTION);
        // QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
    }

    private void initConferenceConfig() {
        if (!TextUtils.isEmpty(SERVER_URL)) {
            ConferenceConfig.setUrl(SERVER_URL);
        } else {
            throw new AssertionError(getString(R.string.error_server_url_null));
        }
    }
}