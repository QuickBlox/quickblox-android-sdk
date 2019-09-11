package com.quickblox.sample.videochat.java.services;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric;
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.java.util.ChatPingAlarmManager;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.SettingsUtil;
import com.quickblox.sample.videochat.java.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;

import org.jivesoftware.smackx.ping.PingFailedListener;

import androidx.annotation.Nullable;

/**
 * QuickBlox team
 */
public class LoginService extends Service {
    private static final String TAG = LoginService.class.getSimpleName();

    private static final String EXTRA_COMMAND_TO_SERVICE = "command_for_service";
    private static final String EXTRA_QB_USER = "qb_user";
    private static final String EXTRA_PENDING_INTENT = "pending_Intent";

    private static final int COMMAND_NOT_FOUND = 0;
    private static final int COMMAND_LOGIN = 1;
    private static final int COMMAND_LOGOUT = 2;

    private QBChatService chatService;
    private QBRTCClient rtcClient;
    private PendingIntent pendingIntent;
    private int currentCommand;
    private QBUser currentUser;

    public static void start(Context context, QBUser qbUser, PendingIntent pendingIntent) {
        Intent intent = new Intent(context, LoginService.class);
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGIN);
        intent.putExtra(EXTRA_QB_USER, qbUser);
        intent.putExtra(EXTRA_PENDING_INTENT, pendingIntent);

        context.startService(intent);
    }

    public static void start(Context context, QBUser qbUser) {
        start(context, qbUser, null);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(context, LoginService.class);
        context.stopService(intent);
    }

    public static void logout(Context context) {
        Intent intent = new Intent(context, LoginService.class);
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT);
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
            currentCommand = intent.getIntExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_NOT_FOUND);
            pendingIntent = intent.getParcelableExtra(EXTRA_PENDING_INTENT);
            currentUser = (QBUser) intent.getSerializableExtra(EXTRA_QB_USER);
        }
    }

    private void startSuitableActions() {
        if (currentCommand == COMMAND_LOGIN) {
            startLoginToChat();
        } else if (currentCommand == COMMAND_LOGOUT) {
            logout();
        }
    }

    private void createChatService() {
        if (chatService == null) {
            QBTcpConfigurationBuilder configurationBuilder = new QBTcpConfigurationBuilder();
            configurationBuilder.setSocketTimeout(0);
            QBChatService.setConnectionFabric(new QBTcpChatConnectionFabric(configurationBuilder));
            QBChatService.setDebugEnabled(true);
            chatService = QBChatService.getInstance();
        }
    }

    private void startLoginToChat() {
        if (chatService.isLoggedIn()) {
            sendResultToActivity(true, null);
        } else {
            loginToChat(currentUser);
        }
    }

    private void loginToChat(QBUser qbUser) {
        chatService.login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "ChatService Login Successful");
                startActionsOnSuccessLogin();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "ChatService Login Error: " + e.getMessage());
                sendResultToActivity(false, e.getMessage() != null
                        ? e.getMessage() : "Login error");
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
                Log.d(TAG, "Ping Chat Server Failed");
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
        SettingsUtil.configRTCTimers(LoginService.this);

        // Add service as callback to RTCClient
        rtcClient.addSessionCallbacksListener(WebRtcSessionManager.getInstance(this));
        rtcClient.prepareToProcessCalls();
    }

    private void sendResultToActivity(boolean isSuccess, String errorMessage) {
        if (pendingIntent != null) {
            Log.d(TAG, "sendResultToActivity()");
            try {
                Intent intent = new Intent();
                intent.putExtra(Consts.EXTRA_LOGIN_RESULT, isSuccess);
                intent.putExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE, errorMessage);

                pendingIntent.send(LoginService.this, Consts.EXTRA_LOGIN_RESULT_CODE, intent);
                stopForeground(true);
            } catch (PendingIntent.CanceledException e) {
                String errorMessageSendingResult = e.getMessage();
                Log.d(TAG, errorMessageSendingResult != null
                        ? errorMessageSendingResult
                        : "Error sending result to activity");
            }
        }
    }

    private void logout() {
        if (rtcClient != null) {
            rtcClient.destroy();
        }
        ChatPingAlarmManager.onDestroy();
        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Logout Successful");
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Logout Error: " + e.getMessage());
                    chatService.destroy();
                }
            });
        }
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
        if (!isCallServiceRunning()) {
            logout();
        }
    }

    private boolean isCallServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (LoginService.class.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        return running;
    }
}