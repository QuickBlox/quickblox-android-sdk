package com.quickblox.sample.groupchatwebrtc.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Map;

/**
 * Created by tereha on 13.05.16.
 */
public class LoginToChatAndCallListenerService extends Service implements QBRTCClientSessionCallbacks{
    private static final String TAG = LoginToChatAndCallListenerService.class.getSimpleName();
    private QBChatService chatService;
    private String login;
    private String password;
    private PendingIntent pendingIntent;
    private int startServiceVariant;
//    private BroadcastReceiver wifiStateReceiver;
    private boolean needMaintainConnectivity;
    private QBRTCClient rtcClient;
    private Integer userId;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (!QBChatService.isInitialized()) {
            QBChatService.init(getApplicationContext());
        }

        chatService = QBChatService.getInstance();

        if (intent != null && intent.getExtras()!= null) {
            parseIntentExtras(intent);
        }

        initQbChatServiceIfNeed();

        if(!QBChatService.getInstance().isLoggedIn()){
            loginToChat(userId, login, password);
        } else {
            startActionsOnSuccessLogin();
        }

        return START_REDELIVER_INTENT;
    }

    private void initQbChatServiceIfNeed() {
        if (chatService == null){
            QBChatService.setDebugEnabled(true);
            //QBChatService.setDefaultAutoSendPresenceInterval(60); set this parameter after updating SDK
            chatService = QBChatService.getInstance();
        }
    }

    private void initQBRTCClient() {
        rtcClient = QBRTCClient.getInstance(this);
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
        //
//        QBRTCConfig.setMaxOpponentsCount(6);
//        QBRTCConfig.setDisconnectTime(30);
//        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setDebugEnabled(true);


        // Add activity as callback to RTCClient
        rtcClient.addSessionCallbacksListener(this);
    }

    private void parseIntentExtras(Intent intent) {
        pendingIntent = intent.getParcelableExtra(Consts.EXTRA_PENDING_INTENT);

        userId = intent.getIntExtra(Consts.EXTRA_USER_ID, 0);
        login = intent.getStringExtra(Consts.EXTRA_USER_LOGIN);
        password = intent.getStringExtra(Consts.EXTRA_USER_PASSWORD);
    }

    private void loginToChat(Integer id, final String login, final String password) {
        Exception exception = null;
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
        initQBRTCClient();
        sendResultToActivity(true, null);
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
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        rtcClient.destroy();
        try {
            chatService.logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

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


    ////////////////////////////  === Call listening methods ===  ////////////////////////////

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onReceiveNewSession");
        QBRTCSession currentSession = WebRtcSessionManager.getCurrentSession();
        if (currentSession == null) {
            WebRtcSessionManager.setCurrentSession(qbrtcSession);

            CallActivity.start(getApplicationContext(), true);
        } else {
            qbrtcSession.rejectCall(null);
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNotAnswer");

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallRejectByUser");

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onCallAcceptByUser");

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        Log.d(TAG, "onReceiveHangUpFromUser");

    }

    @Override
    public void onUserNoActions(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(TAG, "onUserNoActions");

    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionClosed");
        QBRTCSession currentSession = WebRtcSessionManager.getCurrentSession();
        if (currentSession != null && currentSession.getSessionID().equals(qbrtcSession.getSessionID())){
            WebRtcSessionManager.setCurrentSession(null);
        }
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d(TAG, "onSessionStartClose");

    }
}
