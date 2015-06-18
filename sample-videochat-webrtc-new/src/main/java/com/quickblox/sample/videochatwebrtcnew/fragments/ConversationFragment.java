package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.ListUsersActivity;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment implements Serializable {

    private String TAG = ConversationFragment.class.getSimpleName();
    private ArrayList<Integer> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;

    private TextView opponentNumber;
    private TextView connectionStatus;
    private ImageView opponentAvatar;
    private ToggleButton cameraToggle;
    private ToggleButton switchCameraToggle;
    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private ImageView imgMyCameraOff;
    private TextView incUserName;
    private View view;
    private Map<String, String> userInfo;
    private View opponentItemView;
    private HorizontalScrollView camerasOpponentsList;
//    public static LinearLayout opponentsFromCall;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private LinearLayout actionVideoButtonsLayout;
    private View actionBar;
    private String callerName;
    private LinearLayout noVideoImageContainer;
    private boolean isMessageProcessed;
    private MediaPlayer ringtone;
    private View localVideoView;
    private View remoteVideoView;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private CameraState cameraState = CameraState.NONE;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        this.inflater = inflater;
        this.container = container;
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        ((CallActivity) getActivity()).initActionBarWithTimer();

        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList(ApplicationSingleton.OPPONENTS);
            qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);
            startReason = getArguments().getInt(CallActivity.START_CONVERSATION_REASON);
            sessionID = getArguments().getString(CallActivity.SESSION_ID);
            callerName = getArguments().getString(CallActivity.CALLER_NAME);

            Log.d(TAG, "CALLER_NAME: " + callerName);

        }

        initViews(view);
        initButtonsListener();

//        createOpponentsList(opponents);
        setUpUiByCallType(qbConferenceType);

        return view;

    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.getValue()) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);

            localVideoView.setVisibility(View.INVISIBLE);
            remoteVideoView.setVisibility(View.INVISIBLE);

            imgMyCameraOff.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {


        cameraToggle.setEnabled(enability);
        switchCameraToggle.setEnabled(enability);
        imgMyCameraOff.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);


        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        switchCameraToggle.setActivated(enability);
        imgMyCameraOff.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);
    }


    @Override
    public void onStart() {

        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        QBRTCSession session = ((CallActivity) getActivity()).getCurrentSession();
        if (!isMessageProcessed) {
            if (startReason == StartConversetionReason.INCOME_CALL_FOR_ACCEPTION.ordinal()) {
                session.acceptCall(session.getUserInfo());
            } else {
                session.startCall(session.getUserInfo());
                startOutBeep();
            }
            isMessageProcessed = true;
        }
    }

    private void startOutBeep() {
        ringtone = MediaPlayer.create(getActivity(), R.raw.beep);
        ringtone.setLooping(true);
        ringtone.start();

    }

    public void stopOutBeep() {

        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ringtone.release();
            ringtone = null;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
    }

    private void initViews(View view) {


        localVideoView = view.findViewById(R.id.localVideoVidew);
        remoteVideoView = view.findViewById(R.id.remoteVideoView);

//        opponentsFromCall = (LinearLayout) view.findViewById(R.id.opponentsFromCall);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switchCameraToggle);
        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.dynamicToggleVideoCall);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        incUserName = (TextView) view.findViewById(R.id.incUserName);
        incUserName.setText(callerName);
        incUserName.setBackgroundResource(ListUsersActivity.selectBackgrounForOpponent((
                DataHolder.getUserIndexByFullName(callerName)) + 1));

        noVideoImageContainer = (LinearLayout) view.findViewById(R.id.noVideoImageContainer);
        imgMyCameraOff = (ImageView) view.findViewById(R.id.imgMyCameraOff);

        actionButtonsEnabled(false);
    }
    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.getValue()) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if(cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopOutBeep();
        getActivity().unregisterReceiver(audioStreamReceiver);
    }

    private void initButtonsListener() {

        switchCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    ((CallActivity) getActivity()).getCurrentSession().switchCapturePosition(new Runnable() {
                        @Override
                        public void run() {
//                            Toast.makeText(getActivity(), "Cam was switched", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.d(TAG, "Camera was switched!");
                }
            }
        });


        cameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    if (cameraState != CameraState.DISABLED_FROM_USER) {
                        toggleCamera(false);
                        cameraState = CameraState.DISABLED_FROM_USER;
                    } else {
                        toggleCamera(true);
                        cameraState = CameraState.ENABLED_FROM_USER;
                    }
//                }

            }
        });

        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    Log.d(TAG, "Dynamic switched!");
                    ((CallActivity) getActivity()).getCurrentSession().switchAudioOutput();
                }
            }
        });

        micToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    if (isAudioEnabled) {
                        Log.d(TAG, "Mic is off!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        Log.d(TAG, "Mic is on!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopOutBeep();
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

//                ((CallActivity) getActivity()).getCurrentSession().hangUp(userInfo);
                ((CallActivity) getActivity()).hangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        // TODO temporary insertion will be removed when GLVideoView will be fixed
        DisplayMetrics displaymetrics = new DisplayMetrics();
        displaymetrics.setToDefaults();

        ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

        layoutParams.height = localVideoView.getHeight();
        layoutParams.width = localVideoView.getWidth();

        imgMyCameraOff.setLayoutParams(layoutParams);

        Log.d(TAG, "Width is: " + imgMyCameraOff.getLayoutParams().width + " height is:" + imgMyCameraOff.getLayoutParams().height);
        // TODO end

        if (((CallActivity) getActivity()).getCurrentSession() != null) {
            ((CallActivity) getActivity()).getCurrentSession().setVideoEnabled(isNeedEnableCam);
            cameraToggle.setChecked(isNeedEnableCam);

            if (isNeedEnableCam) {
                Log.d(TAG, "Camera is on!");
                switchCameraToggle.setVisibility(View.VISIBLE);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
            } else {
                Log.d(TAG, "Camera is off!");
                switchCameraToggle.setVisibility(View.INVISIBLE);
                imgMyCameraOff.setVisibility(View.VISIBLE);
            }
        }
    }


    public static enum StartConversetionReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE;
    }

    private List<QBUser> getOpponentsFromCall(ArrayList<Integer> opponents) {
        ArrayList<QBUser> opponentsList = new ArrayList<>();

        for (Integer opponentId : opponents) {
            try {
                opponentsList.add(QBUsers.getUser(opponentId));
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
        }
        return opponentsList;
    }

//    private void createOpponentsList(List<Integer> opponents) {
//        if (opponents.size() != 0) {
//            for (Integer i : opponents) {
//                addOpponentPreviewToList(i, opponentsFromCall);
//            }
//        }
//    }

    private void addOpponentPreviewToList(Integer userID, LinearLayout opponentsFromCall) {

        if (opponentsFromCall.findViewById(userID) == null) {

            View opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall, false);
            opponentItemView.setId(userID);

            opponentItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Main opponent Selected");
                }
            });

            TextView opponentNumber = (TextView) opponentItemView.findViewById(R.id.opponentNumber);
            opponentNumber.setText(String.valueOf(ListUsersActivity.getUserIndex(userID)));
            opponentNumber.setBackgroundResource(ListUsersActivity.resourceSelector
                    (ListUsersActivity.getUserIndex(userID)));

            ImageView opponentAvatar = (ImageView) opponentItemView.findViewById(R.id.opponentAvatar);
            opponentAvatar.setImageResource(R.drawable.ic_noavatar);

            opponentsFromCall.addView(opponentItemView);
        } else {
            opponentsFromCall.addView(opponentsFromCall.findViewById(userID));
        }
    }

    private String getCallerName(QBRTCSession session) {
        String s = new String();
        int i = session.getCallerID();

        allUsers.addAll(DataHolder.usersList);

        for (QBUser usr : allUsers) {
            if (usr.getId().equals(i)) {
                s = usr.getFullName();
            }
        }
        return s;
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)){
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)){
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/){
                dynamicToggleVideoCall.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                dynamicToggleVideoCall.setChecked(true);
            } else {
//                Toast.makeText(context, "Output audio stream is incorrect", Toast.LENGTH_LONG).show();
            }
            dynamicToggleVideoCall.invalidate();
        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

}


