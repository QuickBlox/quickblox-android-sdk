package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
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

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by tereha on 16.02.15.
 */
public class ConversationFragment extends Fragment implements Serializable {

    private String TAG = "ConversationFragment";
    private ArrayList<Integer> opponents;
    private int qbConferenceType;
    private int startReason;
    private String sessionID;
    //    private QBGLVideoView videoView;
    private GLSurfaceView videoView;
    //    private QBRTCSessionDescription sessionDescription;
    private static VideoRenderer.Callbacks REMOTE_RENDERER;

    //    private QBGLVideoView opponentLittleCamera;
    private TextView opponentNumber;
    private TextView connectionStatus;
    private ImageView opponentAvatar;
    //    private HorizontalScrollView camerasOpponentsList;
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
    public static LinearLayout opponentsFromCall;
    private LayoutInflater inflater;
    private ViewGroup container;
    private Bundle savedInstanceState;
    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private LinearLayout actionVideoButtonsLayout;
    //    private Chronometer timer;
    private View actionBar;
    private String callerName;
    private LinearLayout noVideoImageContainer;
    private boolean isMessageProcessed;
    private MediaPlayer ringtone;


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

            Log.d("Track", "CALLER_NAME: " + callerName);

        }

        initViews(view);
        initButtonsListener();
        VideoRendererGui.setView(videoView, new Runnable() {
            @Override
            public void run() {
            }
        });

        createOpponentsList(opponents);
        ((CallActivity) getActivity()).setCurrentVideoView(videoView);
        setUpUIByCallType(qbConferenceType);

        return view;

    }

    private void setUpUIByCallType(int qbConferenceType) {
        if (qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.getValue()) {
            cameraToggle.setVisibility(View.GONE);
            switchCameraToggle.setVisibility(View.INVISIBLE);
            videoView.setVisibility(View.INVISIBLE);
            imgMyCameraOff.setVisibility(View.INVISIBLE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {


        cameraToggle.setEnabled(enability);
        switchCameraToggle.setEnabled(enability);
        videoView.setEnabled(enability);
        imgMyCameraOff.setEnabled(enability);
        handUpVideoCall.setEnabled(enability);


        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        switchCameraToggle.setActivated(enability);
        videoView.setActivated(enability);
        imgMyCameraOff.setActivated(enability);
    }


    @Override
    public void onStart() {
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
        setRetainInstance(true);
        Log.d("Track", "onCreate() from ConversationFragment");
        super.onCreate(savedInstanceState);
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        onCreateView(inflater, container, savedInstanceState);
//    }

    private void initViews(View view) {

//        videoView = (QBGLVideoView)view.findViewById(R.id.videoView);
        videoView = (GLSurfaceView) view.findViewById(R.id.videoView);

//        camerasOpponentsList = (HorizontalScrollView)view.findViewById(R.id.camerasOpponentsList);
//        ScrollView camerasOpponentsListLand = (ScrollView)view.findViewById(R.id.camerasOpponentsListLand);

        opponentsFromCall = (LinearLayout) view.findViewById(R.id.opponentsFromCall);

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
    }


    private void initButtonsListener() {

        switchCameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CallActivity) getActivity()).getCurrentSession().switchCapturePosition();
                Log.d(TAG, "Camera switched!");
            }
        });


        cameraToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO temporary insertion will be removed when GLVideoView will be fixed
                DisplayMetrics displaymetrics = new DisplayMetrics();
                displaymetrics.setToDefaults();

                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int height = displaymetrics.heightPixels;
                int width = displaymetrics.widthPixels;

                ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

                layoutParams.height = ((height / 100) * 31);
                layoutParams.width = ((width / 100) * 33);

                imgMyCameraOff.setLayoutParams(layoutParams);

                Log.d(TAG, "Width is: " + imgMyCameraOff.getLayoutParams().width + " height is:" + imgMyCameraOff.getLayoutParams().height);
                // TODO end

                if (isVideoEnabled) {
                    Log.d("Track", "Camera is off!");
                    ((CallActivity) getActivity()).getCurrentSession().setVideoEnabled(false);
                    isVideoEnabled = false;
                    switchCameraToggle.setVisibility(View.INVISIBLE);
                    imgMyCameraOff.setVisibility(View.VISIBLE);
                } else {
                    Log.d("Track", "Camera is on!");
                    ((CallActivity) getActivity()).getCurrentSession().setVideoEnabled(true);
                    isVideoEnabled = true;
                    switchCameraToggle.setVisibility(View.VISIBLE);
                    imgMyCameraOff.setVisibility(View.INVISIBLE);
                }
            }
        });

        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Dynamic switched!");
                ((CallActivity) getActivity()).getCurrentSession().switchAudioOutput();
            }
        });

        micToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAudioEnabled) {
                    Log.d("Track", "Mic is off!");
                    ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(false);
                    isAudioEnabled = false;
                } else {
                    Log.d("Track", "Mic is on!");
                    ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(true);
                    isAudioEnabled = true;
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopOutBeep();
                actionButtonsEnabled(false);
                Log.d("Track", "Call is stopped");
                ((CallActivity) getActivity()).getCurrentSession().hangUp(userInfo);
            }
        });

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

    private void createOpponentsList(List<Integer> opponents) {
        if (opponents.size() != 0) {
            for (Integer i : opponents) {
                addOpponentPreviewToList(i, opponentsFromCall);
            }
        }
    }

    private void addOpponentPreviewToList(Integer userID, LinearLayout opponentsFromCall) {

        if (opponentsFromCall.findViewById(userID) == null) {

            View opponentItemView = inflater.inflate(R.layout.list_item_opponent_from_call, opponentsFromCall, false);
            opponentItemView.setId(userID);

            opponentItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Track", "Main opponent Selected");
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
}


