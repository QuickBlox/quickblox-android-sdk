package com.quickblox.sample.videochatwebrtcnew.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.SessionManager;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.OpponentsActivity;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;

import org.jivesoftware.smack.SmackException;
import org.webrtc.VideoCapturerAndroid;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 08.07.15.
 */
public class IncomeCallListenerService extends Service implements QBRTCClientSessionCallbacks, QBRTCClientConnectionCallbacks {

    private static final String TAG = IncomeCallListenerService.class.getSimpleName();
    private QBChatService chatService;
    private String login;
    private String password;
    private PendingIntent pendingIntent;
    private int startServiceVariant;
    private BroadcastReceiver connectionStateReceiver;
    private boolean needMaintainConnectivity;
    private boolean isConnectivityExists;

    @Override
    public void onCreate() {
        super.onCreate();
        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);
        initConnectionManagerListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        if (!QBChatService.isInitialized()) {
            QBChatService.init(getApplicationContext());
        }

        chatService = QBChatService.getInstance();

        if (intent != null && intent.getExtras()!= null) {
            pendingIntent = intent.getParcelableExtra(Consts.PARAM_PINTENT);
            parseIntentExtras(intent);
            if (TextUtils.isEmpty(login) && TextUtils.isEmpty(password)){
                getUserDataFromPreferences();
            }
        }

        if(!QBChatService.getInstance().isLoggedIn()){
            createSession(login, password);
        } else {
            startActionsOnSuccessLogin(login, password);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification createNotification() {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, ListUsersActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.logo_qb)
                .setContentIntent(contentIntent)
                .setTicker(getResources().getString(R.string.service_launched))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.logged_in_as) + " " +
                        DataHolder.getUserNameByLogin(login));

        Notification notification = notificationBuilder.build();

        return notification;
    }

    private void initQBRTCClient() {
        Log.d(TAG, "initQBRTCClient()");
        try {
            QBChatService.getInstance().startAutoSendPresence(60);
        } catch (SmackException.NotLoggedInException e) {
            e.printStackTrace();
        }

        // Add signalling manager
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListener() {
            @Override
            public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
                if (!createdLocally) {
                    QBRTCClient.getInstance().addSignaling((QBWebRTCSignaling) qbSignaling);
                }
            }
        });

        QBRTCClient.getInstance().setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(final String s) {
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        });

        QBRTCConfig.setAnswerTimeInterval(Consts.ANSWER_TIME_INTERVAL);

        // Add activity as callback to RTCClient
        QBRTCClient.getInstance().addSessionCallbacksListener(this);
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);

        // Start mange QBRTCSessions according to VideoCall parser's callbacks
        QBRTCClient.getInstance().prepareToProcessCalls(getApplicationContext());
    }

    private void parseIntentExtras(Intent intent) {
        login = intent.getStringExtra(Consts.USER_LOGIN);
        password = intent.getStringExtra(Consts.USER_PASSWORD);
        startServiceVariant = intent.getIntExtra(Consts.START_SERVICE_VARIANT, Consts.AUTOSTART);
    }

    protected void getUserDataFromPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedPreferences.getString(Consts.USER_LOGIN, null);
        password = sharedPreferences.getString(Consts.USER_PASSWORD, null);
    }

    private void createSession(final String login, final String password) {
        if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
            final QBUser user = new QBUser(login, password);
            QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
                @Override
                public void onSuccess(QBSession session, Bundle bundle) {
                    Log.d(TAG, "onSuccess create session with params");
                    user.setId(session.getUserId());

                    if (chatService.isLoggedIn()) {
                        Log.d(TAG, "chatService.isLoggedIn()");
                        startActionsOnSuccessLogin(login, password);
                    } else {
                        Log.d(TAG, "!chatService.isLoggedIn()");
                        chatService.login(user, new QBEntityCallbackImpl<QBUser>() {

                            @Override
                            public void onSuccess(QBUser result, Bundle params) {
                                Log.d(TAG, "onSuccess login to chat with params");
                                startActionsOnSuccessLogin(login, password);
                            }

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "onSuccess login to chat");
                                startActionsOnSuccessLogin(login, password);
                            }

                            @Override
                            public void onError(List errors) {
                                sendResultToActivity(false);
                                Toast.makeText(IncomeCallListenerService.this, "Error when login", Toast.LENGTH_SHORT).show();
                                for (Object error : errors) {
                                    Log.d(TAG, error.toString());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.d(TAG, "onSuccess create session");
                }

                @Override
                public void onError(List<String> errors) {
                    for (String s : errors) {
                        Log.d(TAG, s);
                    }
                    sendResultToActivity(false);
                    Toast.makeText(IncomeCallListenerService.this, "Error when login, check test users login and password", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            sendResultToActivity(false);
            stopForeground(true);
            stopService(new Intent(getApplicationContext(), IncomeCallListenerService.class));
        }
    }

    private void startActionsOnSuccessLogin(String login, String password) {
        initQBRTCClient();
        sendResultToActivity(true);
        startOpponentsActivity();
        startForeground(Consts.NOTIFICATION_FORAGROUND, createNotification());
        saveUserDataToPreferences(login, password);
        needMaintainConnectivity = true;
    }

    private void saveUserDataToPreferences(String login, String password){
        Log.d(TAG, "saveUserDataToPreferences()");
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(Consts.USER_LOGIN, login);
        ed.putString(Consts.USER_PASSWORD, password);
        ed.commit();
    }

    private void startOpponentsActivity(){
        Log.d(TAG, "startOpponentsActivity()");
        if (startServiceVariant != Consts.AUTOSTART) {
            Intent intent = new Intent(IncomeCallListenerService.this, OpponentsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void sendResultToActivity (boolean isSuccess){
        Log.d(TAG, "sendResultToActivity()");
        if (startServiceVariant == Consts.LOGIN) {
            try {
                Intent intent = new Intent().putExtra(Consts.LOGIN_RESULT, isSuccess);
                pendingIntent.send(IncomeCallListenerService.this, Consts.LOGIN_RESULT_CODE, intent);
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
//        QBRTCClient.getInstance().close(true);
        try {
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        QBRTCClient.getInstance().removeSessionsCallbacksListener(this);
        QBRTCClient.getInstance().removeConnectionCallbacksListener(this);
        SessionManager.setCurrentSession(null);

        if (connectionStateReceiver != null){
            unregisterReceiver(connectionStateReceiver);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }


    private void initConnectionManagerListener() {
        connectionStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Connection state was changed");
                boolean isConnected = processConnectivityState(intent);
                updateStateIfNeed(isConnected);
            }

            private boolean processConnectivityState(Intent intent) {
                int connectivityType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                // Check does connectivity equal mobile or wifi types
                boolean connectivityState = false;
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null){
                    if (connectivityType == ConnectivityManager.TYPE_MOBILE
                            || connectivityType == ConnectivityManager.TYPE_WIFI
                            || networkInfo.getTypeName().equals("WIFI")
                            || networkInfo.getTypeName().equals("MOBILE")) {
                        //should check null because in air plan mode it will be null
                        if (networkInfo.isConnected()) {
                            // Check does connectivity EXISTS for connectivity type wifi or mobile internet
                            // Pay attention on "!" symbol  in line below
                            connectivityState = true;
                        }
                    }
                }
                return connectivityState;
            }

            private void updateStateIfNeed(boolean connectionState) {
                if (isConnectivityExists != connectionState) {
                    processCurrentConnectionState(connectionState);
                }
            }

            private void processCurrentConnectionState(boolean isConnected) {
                if (!isConnected) {
                    Log.d(TAG, "Connection is turned off");
                } else {
                    if (needMaintainConnectivity) {
                        Log.d(TAG, "Connection is turned on");
                        if (!QBChatService.isInitialized()) {
                            QBChatService.init(getApplicationContext());
                        }
                        chatService = QBChatService.getInstance();
                        if (!QBChatService.getInstance().isLoggedIn()) {
                            SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                            String login = sharedPreferences.getString(Consts.USER_LOGIN, null);
                            String password = sharedPreferences.getString(Consts.USER_PASSWORD, null);
                            reloginToChat(login, password);
                        }
                    }
                }
            }

        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(connectionStateReceiver, intentFilter);
    }

    private void reloginToChat(String login, String password) {
        final QBUser user = new QBUser(login, password);
        QBAuth.createSession(login, password, new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession session, Bundle bundle) {
                Log.d(TAG, "onSuccess create session with params");
                user.setId(session.getUserId());
                chatService.login(user, new QBEntityCallbackImpl<QBUser>() {

                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "onSuccess login to chat with params");
                    }

                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "onSuccess login to chat");
                    }

                    @Override
                    public void onError(List errors) {
                        Toast.makeText(IncomeCallListenerService.this, "Error when login", Toast.LENGTH_SHORT).show();
                        for (Object error : errors) {
                            Log.d(TAG, error.toString());
                        }
                    }
                });
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
                Log.d(TAG, "onSuccess create session");
            }

            @Override
            public void onError(List<String> errors) {
                for (String s : errors) {
                    Log.d(TAG, s);
                }
            }
        });
    }

    private void sendBroadcastMessages(int callbackAction, Integer usedID,
                                       Map<String, String> userInfo, QBRTCException exception){
        Intent intent = new Intent();
        intent.setAction(Consts.CALL_RESULT);
        intent.putExtra(Consts.CALL_ACTION_VALUE, callbackAction);
        intent.putExtra(Consts.USER_ID, usedID);
        intent.putExtra(Consts.USER_INFO_EXTRAS, (Serializable) userInfo);
        intent.putExtra(Consts.QB_EXCEPTION_EXTRAS, exception);
        sendBroadcast(intent);
    }


    //========== Implement methods ==========//

    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        if (SessionManager.getCurrentSession() == null){
            SessionManager.setCurrentSession(qbrtcSession);
            CallActivity.start(this,
                    qbrtcSession.getConferenceType(),
                    qbrtcSession.getOpponents(),
                    qbrtcSession.getUserInfo(),
                    Consts.CALL_DIRECTION_TYPE.INCOMING);
        } else if (SessionManager.getCurrentSession() != null && !qbrtcSession.equals(SessionManager.getCurrentSession())){
                qbrtcSession.rejectCall(new HashMap<String, String>());
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.USER_NOT_ANSWER, integer, null, null);
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
        sendBroadcastMessages(Consts.CALL_REJECT_BY_USER, integer, map, null);
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {
        if (qbrtcSession.equals(SessionManager.getCurrentSession())) {
            sendBroadcastMessages(Consts.RECEIVE_HANG_UP_FROM_USER, integer, null, null);
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        if (qbrtcSession.equals(SessionManager.getCurrentSession())) {
            sendBroadcastMessages(Consts.SESSION_CLOSED, null, null, null);
        }
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        sendBroadcastMessages(Consts.SESSION_START_CLOSE, null, null, null);
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.START_CONNECT_TO_USER, integer, null, null);
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.CONNECTED_TO_USER, integer, null, null);
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.CONNECTION_CLOSED_FOR_USER, integer, null, null);
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.DISCONNECTED_FROM_USER, integer, null, null);
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.DISCONNECTED_TIMEOUT_FROM_USER, integer, null, null);
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        sendBroadcastMessages(Consts.CONNECTION_FAILED_WITH_USER, integer, null, null);
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {
        sendBroadcastMessages(Consts.ERROR, null, null, null);
    }
}
