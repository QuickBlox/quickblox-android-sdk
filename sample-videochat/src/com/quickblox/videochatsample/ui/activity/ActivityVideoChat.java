package com.quickblox.videochatsample.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.model.DataHolder;
import com.quickblox.videochatsample.ui.view.OwnSurfaceView;
import com.quickblox.videochatsample.ui.view.OpponentSurfaceView;

import org.jivesoftware.smack.XMPPException;

public class ActivityVideoChat extends Activity {

    private OwnSurfaceView myView;

    private OpponentSurfaceView opponentView;

    private ProgressBar opponentImageLoadingPb;
    private VideoChatConfig videoChatConfig;
    private Button switchCameraButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_chat_layout);
        initViews();
    }

    private void initViews() {

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

        myView = (OwnSurfaceView) findViewById(R.id.cameraView);
        myView.setCameraDataListener(new OwnSurfaceView.CameraDataListener() {
            @Override
            public void onCameraDataReceive(byte[] data) {
                if (videoChatConfig != null && videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
                    return;
                }
                QBVideoChatController.getInstance().sendVideo(data);
            }
        });

        opponentImageLoadingPb = (ProgressBar) findViewById(R.id.opponentImageLoading);

        try {
            QBVideoChatController.getInstance().setQBVideoChatListener(DataHolder.getInstance().getCurrentQbUser(), qbVideoChatListener);
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
    public void onDestroy() {
        QBVideoChatController.getInstance().finishVideoChat(videoChatConfig);
        super.onDestroy();
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
            opponentImageLoadingPb.setVisibility(progress ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onVideoChatStateChange(CallState callState, VideoChatConfig chat) {
            switch (callState) {
                case ON_CALL_START:
                    Toast.makeText(getBaseContext(), "ON_CALL_START", Toast.LENGTH_SHORT).show();
                    break;
                case ON_CANCELED_CALL:
                    Toast.makeText(getBaseContext(), "ON_CANCELED_CALL", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ON_CALL_END:
                    Toast.makeText(getBaseContext(), "ON_CALL_END", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ACCEPT:
                    Toast.makeText(getBaseContext(), "ACCEPT", Toast.LENGTH_SHORT).show();
                    break;
                case ON_ACCEPT_BY_USER:
                    Toast.makeText(getBaseContext(), "ON_ACCEPT_BY_USER", Toast.LENGTH_SHORT).show();
                    break;
                case ON_CONNECTED:
                    Toast.makeText(getBaseContext(), "ON_CONNECTED", Toast.LENGTH_SHORT).show();
                    break;
                case ON_START_CONNECTING:
                    Toast.makeText(getBaseContext(), "ON_START_CONNECTING", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
}
