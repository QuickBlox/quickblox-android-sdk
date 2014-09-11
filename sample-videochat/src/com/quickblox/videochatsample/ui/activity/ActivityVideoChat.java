package com.quickblox.videochatsample.ui.activity;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.quickblox.module.videochat.core.QBVideoChatController;
import com.quickblox.module.videochat.model.listeners.OnCameraViewListener;
import com.quickblox.module.videochat.model.listeners.OnQBVideoChatListener;
import com.quickblox.module.videochat.model.objects.CallState;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat.model.objects.VideoChatConfig;
import com.quickblox.videochatsample.R;
import com.quickblox.videochatsample.model.DataHolder;
import com.quickblox.videochatsample.ui.view.MySurfaceView;
import com.quickblox.videochatsample.ui.view.OpponentSurfaceView;

import org.jivesoftware.smack.XMPPException;

import java.util.List;

public class ActivityVideoChat extends Activity {

    private MySurfaceView myView;

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

        myView = (MySurfaceView) findViewById(R.id.cameraView);
        myView.setCameraFrameProcess(true);
        // Set VideoChat listener
        myView.setQBVideoChatListener(qbVideoChatListener);

        // Set Camera init callback
        myView.setFPS(6);
        myView.setOnCameraViewListener(new OnCameraViewListener() {
            @Override
            public void onCameraSupportedPreviewSizes(List<Camera.Size> supportedPreviewSizes) {
//                cameraView.setFrameSize(supportedPreviewSizes.get(5));
                Camera.Size firstFrameSize = supportedPreviewSizes.get(0);
                Camera.Size lastFrameSize = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
                myView.setFrameSize(firstFrameSize.width > lastFrameSize.width ? lastFrameSize : firstFrameSize);
            }
        });

        opponentImageLoadingPb = (ProgressBar) findViewById(R.id.opponentImageLoading);

        // VideoChat settings
        videoChatConfig = getIntent().getParcelableExtra(VideoChatConfig.class.getCanonicalName());

        try {
            QBVideoChatController.getInstance().setQBVideoChatListener(DataHolder.getInstance().getCurrentQbUser(), qbVideoChatListener);
        } catch (XMPPException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        myView.reuseCameraView();
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
            if (videoChatConfig.getCallType() != CallType.VIDEO_AUDIO) {
                return;
            }
            QBVideoChatController.getInstance().sendVideo(videoData);
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
                    Toast.makeText(getBaseContext(), getString(R.string.call_start_txt), Toast.LENGTH_SHORT).show();
                    break;
                case ON_CANCELED_CALL:
                    Toast.makeText(getBaseContext(), getString(R.string.call_canceled_txt), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case ON_CALL_END:
                    finish();
                    break;
            }
        }
    };
}
