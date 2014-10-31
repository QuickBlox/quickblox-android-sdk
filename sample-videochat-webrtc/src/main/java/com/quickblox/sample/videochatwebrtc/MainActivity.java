package com.quickblox.sample.videochatwebrtc;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.exception.QBVideoException;
import com.quickblox.videochat.webrtc.listener.QBVideoChatWebRTCSignalingListener;
import com.quickblox.videochat.webrtc.signaling.QBSignalingChannel;
import com.quickblox.videochat.webrtc.QBVideoChat;
import com.quickblox.videochat.webrtc.QBVideoChannel;
import com.quickblox.videochat.webrtc.Consts;
import com.quickblox.videochat.webrtc.model.CallConfig;
import com.quickblox.videochat.webrtc.model.ConnectionConfig;
import com.quickblox.videochat.webrtc.view.QBVideoStreamView;

import org.jivesoftware.smack.SmackException;
import org.webrtc.SessionDescription;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener, QBVideoChatWebRTCSignalingListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Toast logToast;

    private QBVideoStreamView videoStreamView;
    private QBVideoChat videoChat;
    private QBVideoChannel videoChannel;

    private QBUser opponent;
    private TextView currentUserTextView;

    private SessionDescription sdp;
    private String sessionId;
    private Consts.MEDIA_STREAM callType;
    private CallConfig callConfig;

    private boolean cameraEnabled = true;

    private Timer callTimer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initViews();

        enableCallView(true);

        initSignaling();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoChat != null) {
            videoChat.onActivityPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (videoChat != null) {
            videoChat.onActivityResume();
        }
    }

    @Override
    protected void onDestroy() {
        disconnectAndExit();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged");
        if (videoChat != null) {
            videoChat.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.call: {
                call();
                break;
            }
            case R.id.accept: {
                accept();
                break;
            }
            case R.id.reject: {
                reject();
            }
            case R.id.stop: {
                stopCall();
                break;
            }
            case R.id.muteMicrophone: {
                muteMicrophone();
                break;
            }
            case R.id.turnCamera: {
                enableCamera();
                break;
            }
            case R.id.orientation: {
                changeOrientation();
            }
        }
    }


    private void initViews() {
        videoStreamView = (QBVideoStreamView) findViewById(R.id.videoView);

        findViewById(R.id.call).setOnClickListener(this);
        findViewById(R.id.accept).setOnClickListener(this);
        findViewById(R.id.reject).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.muteMicrophone).setOnClickListener(this);
        findViewById(R.id.turnCamera).setOnClickListener(this);
        findViewById(R.id.orientation).setOnClickListener(this);

        // show current User
        //
        currentUserTextView = (TextView) findViewById(R.id.currentUserTextView);
        VideoChatApplication app = (VideoChatApplication)getApplication();
        currentUserTextView.setText(app.getCurrentUser().getId() == VideoChatApplication.FIRST_USER_ID ? "Logged in as the User1": "Logged in as the User2");
    }

    private void enableView(int viewId, boolean enable) {
        findViewById(viewId).setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void enableCallView(boolean enable) {
        enableView(R.id.call, enable);
        enableView(R.id.stop, enable);
    }

    private void enableAcceptView(boolean enable) {
        enableView(R.id.accept, enable);
        enableView(R.id.reject, enable);
    }

    private void initSignaling() {
        QBChatService.getInstance().getVideoChatWebRTCSignalingManager().addSignalingManagerListener(
            new QBVideoChatSignalingManagerListener() {
                @Override
                public void signalingCreated(QBSignaling signaling, boolean createdLocally) {
                    if (!createdLocally) {
                        videoChannel = new QBVideoChannel((QBWebRTCSignaling)signaling);
                        videoChannel.addSignalingListener(MainActivity.this);
                    }
                }
            });
    }


    private void call() {

        // get opponent
        //
        VideoChatApplication app = (VideoChatApplication)getApplication();
        opponent = new QBUser();
        opponent.setId((app.getCurrentUser().getId() == VideoChatApplication.FIRST_USER_ID ? VideoChatApplication.SECOND_USER_ID : VideoChatApplication.FIRST_USER_ID));

        // call
        //
        callTimer = new Timer();
        callTimer.schedule(new CancelCallTimerTask(), 30 * 1000);

        createSenderChannel();
        initVideoChat();

        if (videoChat != null) {
            videoChat.call(opponent, getCallType(), 3000);
        } else {
            logAndToast("Stop current chat before call");
        }
    }

    private void stopCall() {
        cancelCallTimer();

        if (videoChat != null) {
            videoChat.stopCall();
            videoChat = null;
        }
        if (videoChannel != null) {
            videoChannel.close();
            videoChannel = null;
        }

        sessionId = null;
    }

    private void muteMicrophone() {
        if (videoChat != null) {
            videoChat.muteMicrophone(!videoChat.isMicrophoneMute());

            String status = videoChat.isMicrophoneMute() ? "off" : "on";
            ((Button) findViewById(R.id.muteMicrophone)).setText("Mute " + status);
        }
    }

    private void reject() {
        if (callConfig != null) {
            ConnectionConfig connectionConfig = new ConnectionConfig(callConfig.getFromUser(),
                    callConfig.getConnectionSession());
            videoChannel.sendReject(connectionConfig);
        }
        enableAcceptView(false);

        sessionId = null;
    }

    private void enableCamera() {
        if (videoChat != null) {
            if (cameraEnabled) {
                videoChat.disableCamera();
                cameraEnabled = false;
            } else {
                videoChat.enableCamera();
                cameraEnabled = true;
            }

            int resource = cameraEnabled ? R.string.camera_off : R.string.camera_on;
            ((Button) findViewById(R.id.turnCamera)).setText(getString(resource));
        }
    }

    private void accept() {
        initVideoChat();

        logAndToast("callType=" + callConfig.getCallStreamType());

        videoChat.accept(callConfig);

        enableAcceptView(false);
    }

    private void createSenderChannel() {
        QBWebRTCSignaling signaling = QBChatService.getInstance().getVideoChatWebRTCSignalingManager().createSignaling(
                opponent.getId(), null);

        videoChannel = new QBVideoChannel(signaling);
        videoChannel.addSignalingListener(MainActivity.this);
    }

    private void initVideoChat() {
        // Create video chat
        //
        try{
            videoChat = new QBVideoChat(MainActivity.this, videoChannel, videoStreamView);
        } catch (QBVideoException exception){
            exception.printStackTrace();
        }


        // set Capture callback
        //
        videoChat.setMediaCaptureCallback(new QBVideoChat.MediaCaptureCallback() {
            @Override
            public void onCaptureFail(Consts.MEDIA_STREAM mediaStream, String problem) {
                logAndToast(problem);
            }

            @Override
            public void onCaptureSuccess(Consts.MEDIA_STREAM mediaStream) {

            }
        });
    }

    private Consts.MEDIA_STREAM getCallType() {
        int selectedId = ((RadioGroup) findViewById(R.id.callMode)).getCheckedRadioButtonId();
        return (R.id.audio_call == selectedId) ? Consts.MEDIA_STREAM.AUDIO : Consts.MEDIA_STREAM.VIDEO;
    }

    public void changeOrientation() {
        setRequestedOrientation(getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    // Disconnect from remote resources, disposeConnection of local resources, and exit.
    private void disconnectAndExit() {
        if (videoChat != null && !QBVideoChat.VIDEO_CHAT_STATE.CLOSED.equals(videoChat.getState())) {
            videoChat.disposeConnection();
        }
        if (videoChannel != null) {
            videoChannel.close();
        }


        // Logout from Chat
        //
        try {
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        QBChatService.getInstance().destroy();
    }

    private void closeConnection() {
        if (videoChat != null) {
            videoChat.disposeConnection();
            videoChat = null;
        }
        if (videoChannel != null) {
            videoChannel.close();
            videoChannel = null;
        }
    }

    private void cancelCallTimer() {
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }
    }


    // Log |msg| and Toast about it.
    private void logAndToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, msg);

                if (logToast != null) {
                    logToast.cancel();
                }
                logToast = Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT);
                logToast.show();
            }
        });
    }



    @Override
    public void onCall(final ConnectionConfig connectionConfig) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (sessionId != null){
                    return;
                }
                opponent = connectionConfig.getFromUser();

                logAndToast("call from user " + opponent.getFullName());

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(4000);

                // enable 'Accept' button
                enableAcceptView(true);

                // save incoming call parameters
                //
                sessionId = connectionConfig.getConnectionSession();
                sdp = ((CallConfig) connectionConfig).getSessionDescription();
                callType = ((CallConfig) connectionConfig).getCallStreamType();
                callConfig = (CallConfig) connectionConfig;
            }
        });
    }

    @Override
    public void onIceCandidate(ConnectionConfig connectionConfig) {

    }

    @Override
    public void onAccepted(final ConnectionConfig connectionConfig) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelCallTimer();
                logAndToast("Call accepted");
            }
        });
    }

    @Override
    public void onParametersChanged(final ConnectionConfig connectionConfig) {

    }

    @Override
    public void onStop(final ConnectionConfig connectionConfig) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                enableAcceptView(false);
                closeConnection();

                logAndToast("Participant closed connection");
            }
        });
    }

    @Override
    public void onRejected(final ConnectionConfig connectionConfig) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cancelCallTimer();
                closeConnection();

                logAndToast("onRejected");
            }
        });
    }

    @Override
    public void onClosed(String msg) {
        logAndToast("onClosed");
    }

    @Override
    public void onError(QBSignalingChannel.PacketType packetType, QBChatException e) {
        logAndToast("onError: " + e.getLocalizedMessage());
    }



    class CancelCallTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopCall();
                }
            });
        }
    }
}