package com.quickblox.sample.groupchatwebrtc.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.util.ChatPingAlarmManager;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smackx.ping.PingFailedListener;

/**
 * Created by tereha on 13.05.16.
 */
public class LoginToChatAndCallListenerService extends Service{
    private static final String TAG = LoginToChatAndCallListenerService.class.getSimpleName();
    private QBChatService chatService;
    private QBRTCClient rtcClient;
    private PendingIntent pendingIntent;
    private Integer userId;
    private String login;
    private String password;
    private int currentCommand;

    public static void start(Context context, QBUser qbUser, PendingIntent pendingIntent){
        Intent intent = new Intent(context, LoginToChatAndCallListenerService.class);

        if (qbUser != null){
            intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGIN);
            intent.putExtra(Consts.EXTRA_USER_ID, qbUser.getId());
            intent.putExtra(Consts.EXTRA_USER_LOGIN, qbUser.getLogin());
            intent.putExtra(Consts.EXTRA_USER_PASSWORD, Consts.DEFAULT_USER_PASSWORD);
        } else {
            intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGOUT);
        }

        intent.putExtra(Consts.EXTRA_PENDING_INTENT, pendingIntent);
        context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createChatService();

        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        parseIntentExtras(intent);

        startSuitableActions();

        return START_REDELIVER_INTENT;
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            currentCommand = intent.getIntExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_NOT_FOUND);

            pendingIntent = intent.getParcelableExtra(Consts.EXTRA_PENDING_INTENT);

            userId = intent.getIntExtra(Consts.EXTRA_USER_ID, 0);
            login = intent.getStringExtra(Consts.EXTRA_USER_LOGIN);
            password = intent.getStringExtra(Consts.EXTRA_USER_PASSWORD);
        }
    }

    private void startSuitableActions() {
        if (currentCommand == Consts.COMMAND_LOGIN) {
            startLoginToChat();
        } else if (currentCommand == Consts.COMMAND_LOGOUT){
            logout();
        }
    }

    private void createChatService() {
        if (chatService == null){
            QBChatService.setDebugEnabled(true);
            QBChatService.setDefaultAutoSendPresenceInterval(60);
            chatService = QBChatService.getInstance();
        }
    }

    private void startLoginToChat() {
        if(!chatService.isLoggedIn()){
            loginToChat(userId, login, password);
        } else {
            startActionsOnSuccessLogin();
        }
    }

    private void loginToChat(Integer id, final String login, final String password) {
        QBUser qbUser = new QBUser(login, password);
        qbUser.setId(id);

        chatService.login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "login onSuccess");
                startActionsOnSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "login onError " + e.getMessage());
                sendResultToActivity(false, e.getMessage() != null
                        ? e.getMessage()
                        : "Login error");
            }
        });
    }

    private void startActionsOnSuccessLogin() {
        initPingListener();
        initQBRTCClient();
        sendResultToActivity(true, null);
    }

    private void initPingListener() {
        ChatPingAlarmManager.onCreate(this);
        ChatPingAlarmManager.getInstanceFor().addPingListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                showToast("Ping chat server failed");
            }
        });
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(getApplicationContext());
        // Add signalling manager
        chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    rtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        // Configure
        QBRTCConfig.setDebugEnabled(true);

        // Add service as callback to RTCClient
        rtcClient.addSessionCallbacksListener(new CurrentSessionStateCallback(this));
    }

    private void sendResultToActivity(boolean isSuccess, String errorMessage) {
        if (pendingIntent != null) {
            Log.d(TAG, "sendResultToActivity()");
            try {
                Intent intent = new Intent();
                intent.putExtra(Consts.EXTRA_LOGIN_RESULT, isSuccess);
                intent.putExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE, errorMessage);

                pendingIntent.send(LoginToChatAndCallListenerService.this, Consts.EXTRA_LOGIN_RESULT_CODE, intent);
            } catch (PendingIntent.CanceledException e) {
                String errorMessageSendingResult = e.getMessage();
                Log.d(TAG, errorMessageSendingResult != null
                        ? errorMessageSendingResult
                        : "Error sending result to activity");
            }
        }
    }

    private void showToast(final String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void logout(){
        destroyRtcClientAndChat();
    }

    private void destroyRtcClientAndChat() {
        rtcClient.destroy();
        ChatPingAlarmManager.onDestroy();
        chatService.destroy();
        stopSelf();
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

    class CurrentSessionStateCallback extends WebRtcSessionManager{
        private final Context context;

        public CurrentSessionStateCallback (Context context){
            this.context = context;
        }

        @Override
        public void onReceiveNewSession(QBRTCSession session) {
            super.onReceiveNewSession(session);
            Log.d(TAG, "onReceiveNewSession LoginToChatAndCallListenerService");
            if (getCurrentSession().equals(session)){
                CallActivity.start(context, true);
            }
        }
    }
}
