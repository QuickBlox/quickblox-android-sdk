package com.quickblox.videochatsample.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.core.QBVideoChatController;
import com.quickblox.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.videochat.model.objects.CallState;
import com.quickblox.videochat.model.objects.CallType;
import com.quickblox.videochat.model.objects.VideoChatConfig;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.VideoChatApplication;
import com.quickblox.videochatsample.model.listener.OnCallDialogListener;
import com.quickblox.videochatsample.model.utils.DialogHelper;
import com.quickblox.videochatsample.ui.view.OwnSurfaceView;
import com.quickblox.videochatsample.ui.view.OpponentSurfaceView;

import org.jivesoftware.smack.XMPPException;

public class ActivityVideoChat extends Activity {

    private OwnSurfaceView myView;
    private OpponentSurfaceView opponentView;

    private ProgressBar progressBar;
    private Button switchCameraButton;
    private Button startStopVideoCallBtn;
    private TextView txtName;

    private VideoChatConfig videoChatConfig;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_chat_layout);

        initViews();
    }

    private void initViews() {
        final VideoChatApplication app = (VideoChatApplication)getApplication();

        // VideoChat settings
        videoChatConfig = getIntent().getParcelableExtra(VideoChatConfig.class.getCanonicalName());


        // Setup UI
        //
        opponentView = (OpponentSurfaceView) findViewById(R.id.opponentView);

        switchCameraButton = (Button)findViewById(R.id.switch_camera_button);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myView.switchCamera();
            }
        });

        myView = (OwnSurfaceView) findViewById(R.id.ownCameraView);
        myView.setCameraDataListener(new OwnSurfaceView.CameraDataListener() {
            @Override
            public void onCameraDataReceive(byte[] data) {
                if (videoChatConfig != null && videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
                    return;
                }
                QBVideoChatController.getInstance().sendVideo(data);
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.opponentImageLoading);

        txtName = (TextView) findViewById(R.id.txtName);
        txtName.setText("You logged in as the " +
                (app.getCurrentUser().getId() == VideoChatApplication.FIRST_USER_ID ? "1st user" : "2nd user"));

        startStopVideoCallBtn = (Button) findViewById(R.id.startStopCallBtn);
        startStopVideoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Button btn = (Button)v;
                // Call
                if(btn.getText().equals("Call user")){
                    progressBar.setVisibility(View.VISIBLE);

                    // Call user
                    //
                    QBUser opponentUser = new QBUser();
                    opponentUser.setId((app.getCurrentUser().getId() == VideoChatApplication.FIRST_USER_ID ? VideoChatApplication.SECOND_USER_ID : VideoChatApplication.FIRST_USER_ID));
                    videoChatConfig = QBVideoChatController.getInstance().callFriend(opponentUser, CallType.VIDEO_AUDIO, null);

                // Stop call
                }else{
                    startStopVideoCallBtn.setText("Call user");
                    progressBar.setVisibility(View.INVISIBLE);

                    QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);

                    opponentView.clear();
                }
            }
        });

        // Set video chat listener
        //
        try {
            QBVideoChatController.getInstance().setQBVideoChatListener(app.getCurrentUser(), qbVideoChatListener);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myView.reuseCamera();
    }

    @Override
    protected void onPause() {
        myView.closeCamera();
        super.onPause();
    }

    @Override
    protected void onStop() {
        QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);
        super.onStop();
    }

    OnQBVideoChatListener qbVideoChatListener = new OnQBVideoChatListener() {

        @Override
        public void onCameraDataReceive(byte[] videoData) {
            //
        }

        @Override
        public void onMicrophoneDataReceive(byte[] audioData) {
            QBVideoChatController.getInstance().sendAudio(audioData);
        }

        @Override
        public void onOpponentVideoDataReceive(final byte[] videoData) {
            opponentView.render(videoData);
        }

        @Override
        public void onOpponentAudioDataReceive(byte[] audioData) {
            QBVideoChatController.getInstance().playAudio(audioData);
        }

        @Override
        public void onProgress(boolean progress) {
//            progressBar.setVisibility(progress ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onVideoChatStateChange(CallState callState, VideoChatConfig receivedVideoChatConfig) {
            videoChatConfig = receivedVideoChatConfig;

            switch (callState) {
                case ON_CALL_START:
                    Toast.makeText(getBaseContext(), "ON_CALL_START", Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case ON_CANCELED_CALL:
                    Toast.makeText(getBaseContext(), "ON_CANCELED_CALL", Toast.LENGTH_SHORT).show();

                    videoChatConfig = null;
                    if (alertDialog != null && alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    autoCancelHandler.removeCallbacks(autoCancelTask);

                    break;
                case ON_CALL_END:
                    Toast.makeText(getBaseContext(), "ON_CALL_END", Toast.LENGTH_SHORT).show();

                    // clear opponent view
                    opponentView.clear();
                    startStopVideoCallBtn.setText("Call user");
                    break;
                case ACCEPT:
                    Toast.makeText(getBaseContext(), "ACCEPT", Toast.LENGTH_SHORT).show();

                    showIncomingCallDialog();
                    break;
                case ON_ACCEPT_BY_USER:
                    Toast.makeText(getBaseContext(), "ON_ACCEPT_BY_USER", Toast.LENGTH_SHORT).show();

                    QBVideoChatController.getInstance().onAcceptFriendCall(videoChatConfig, null);
                    break;
                case ON_REJECTED_BY_USER:
                    Toast.makeText(getBaseContext(), "ON_REJECTED_BY_USER", Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                case ON_CONNECTED:
                    Toast.makeText(getBaseContext(), "ON_CONNECTED", Toast.LENGTH_SHORT).show();

                    progressBar.setVisibility(View.INVISIBLE);

                    startStopVideoCallBtn.setText("Hung up");
                    break;
                case ON_START_CONNECTING:
                    Toast.makeText(getBaseContext(), "ON_START_CONNECTING", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private Handler autoCancelHandler = new Handler(Looper.getMainLooper());
    private Runnable autoCancelTask = new Runnable() {
        @Override
        public void run() {
            if (alertDialog != null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }
        }
    };

    private void showIncomingCallDialog() {
        alertDialog = DialogHelper.showCallDialog(this, new OnCallDialogListener() {
            @Override
            public void onAcceptCallClick() {
                progressBar.setVisibility(View.VISIBLE);

                QBVideoChatController.getInstance().acceptCallByFriend(videoChatConfig, null);

                autoCancelHandler.removeCallbacks(autoCancelTask);
            }

            @Override
            public void onRejectCallClick() {
                QBVideoChatController.getInstance().rejectCall(videoChatConfig);

                autoCancelHandler.removeCallbacks(autoCancelTask);
            }
        });

        autoCancelHandler.postDelayed(autoCancelTask, 30000);
    }
}
