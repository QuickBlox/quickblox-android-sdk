package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.sample.groupchatwebrtc.utils.CameraUtils;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.sample.groupchatwebrtc.view.RTCGLVideoView;
import com.quickblox.sample.groupchatwebrtc.view.RTCGLVideoView.RendererConfig;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;


/**
 * QuickBlox team
 */
public class ConversationFragment extends Fragment implements Serializable, QBRTCClientVideoTracksCallbacks,
        QBRTCSessionConnectionCallbacks, CallActivity.QBRTCSessionUserCallback, OpponentsFromCallAdapter.OnAdapterEventListener {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;

    private String TAG = ConversationFragment.class.getSimpleName();
    private ArrayList<QBUser> opponents;
    private int qbConferenceType;
    private int startReason;

    private ToggleButton cameraToggle;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private View myCameraOff;
    private View view;
    private boolean isVideoEnabled = false;
    private boolean isVideoCall = false;
    private boolean isAudioEnabled = true;
    private LinearLayout actionVideoButtonsLayout;
    private boolean isMessageProcessed;
    private RTCGLVideoView localVideoView;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private CameraState cameraState = CameraState.NONE;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private Handler mainHandler;
    private OnCallEventsController callEvents;
    private ConversationFragmentCallbackListener conversationFragmentCallbackListener;
    private boolean isIncomingCall;
    private QBRTCSession currentSession;
    private TextView connectionStatusLocal;
    private TextView backgroundTextView;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private ActionBar actionBar;
    private boolean isRemoteShown;
    private boolean isStarted;
    private boolean headsetPlugged;

    private Chronometer timerABWithTimer;
    private int amountOpponents;
    private boolean isUserRemoved;
    private int oldqbUserID;
    private boolean clicked;
    private QBRTCVideoTrack videoTrackFullScreen;
    RTCGLVideoView remoteVideoViewFromPreview;
    QBRTCVideoTrack lastclickedVideoTrackPreviewScreen;

    public static ConversationFragment newInstance(boolean isIncomingCall) {
        ConversationFragment fragment = new ConversationFragment();

        Bundle args = new Bundle();
        args.putBoolean(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        initFields();
        initActionBarInner();
        initViews(view);
        initButtonsListener();
        setUpUiByCallType(qbConferenceType);

        mainHandler = new FragmentLifeCycleHandler();
        return view;
    }

    private void initFields() {
        QbUsersDbManager dbManager = QbUsersDbManager.getInstance(getActivity().getApplicationContext());
        WebRtcSessionManager sessionManager = WebRtcSessionManager.getInstance(getActivity());

        localViewOnClickListener = new LocalViewOnClickListener();

        if (getArguments() != null) {
            isIncomingCall = getArguments().getBoolean(Consts.EXTRA_IS_INCOMING_CALL);
        }

        currentSession = sessionManager.getCurrentSession();
        opponents = dbManager.getUsersByIds(currentSession.getOpponents());

        if (isIncomingCall) {
            opponents.add(dbManager.getUserById(currentSession.getCallerID()));
            opponents.remove(QBChatService.getInstance().getUser());
        }

        amountOpponents = opponents.size();

        String callerName = dbManager.getUserNameById(currentSession.getCallerID());
        qbConferenceType = currentSession.getConferenceType().ordinal();

        isPeerToPeerCall = opponents.size() == 1;
        isVideoCall = (qbConferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.ordinal());

        Log.d(TAG, "opponents: " + opponents.toString());
        Log.d(TAG, "currentSession " + currentSession.toString());
    }

    public void initActionBarInner() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_call);
        timerABWithTimer = (Chronometer) getActivity().findViewById(R.id.timer_chronometer);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        actionBar = ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    public void setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true);
        QBUser user = QBChatService.getInstance().getUser();
        actionBar.setTitle(user.getFullName());
        if (isPeerToPeerCall) {
            actionBar.setSubtitle(getString(R.string.opponent, opponents.get(0).getFullName()));
        } else {
            actionBar.setSubtitle(getString(R.string.opponents, amountOpponents));
        }
        backgroundTextView.setVisibility(View.INVISIBLE);
        actionButtonsEnabled(true);
    }

    private void initVideoTrackSListener() {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(this);
        }
    }

    private void removeVideoTrackSListener() {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
    }

    private void setUpUiByCallType(int qbConferenceType) {
        if (!isVideoCall) {
            cameraToggle.setVisibility(View.GONE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {

        cameraToggle.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            callEvents = (OnCallEventsController) activity;
            conversationFragmentCallbackListener = (ConversationFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCallEventsController and ConversationFragmentCallbackListener");
        }
    }

    @Override
    public void onStart() {

        initVideoTrackSListener();
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        if (!isMessageProcessed) {
            if (isIncomingCall) {
                currentSession.acceptCall(currentSession.getUserInfo());
            } else {
                currentSession.startCall(currentSession.getUserInfo());
            }
            isMessageProcessed = true;
        }
        conversationFragmentCallbackListener.addTCClientConnectionCallback(this);
        conversationFragmentCallbackListener.addRTCSessionUserCallback(this);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
    }

    private void initViews(View view) {

        opponentViewHolders = new SparseArray<>(opponents.size());

        if (!isPeerToPeerCall) {
            recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);

            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
            recyclerView.setHasFixedSize(true);
            final int columnsCount = defineColumnsCount();
            final int rowsCount = defineRowCount();
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(getActivity(), HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);

//          for correct removing item in adapter
            recyclerView.setItemAnimator(null);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setGrid(columnsCount, rowsCount);
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
        connectionStatusLocal = (TextView) view.findViewById(R.id.connectionStatusLocal);

        backgroundTextView = (TextView) view.findViewById(R.id.backgroundText);

        String[] opponentName = new String[opponents.size()];
        for (int i = 0; i < opponents.size(); i++) {
            opponentName[i] = opponents.get(i).getFullName();
        }

        backgroundTextView.setText(getString(R.string.outgoing_audio_video_call_title, TextUtils.join(", ", opponentName)));

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);

        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);

        actionButtonsEnabled(false);
    }

    private void setGrid(int columnsCount, int rowsCount) {
        int gridWidth = recyclerView.getMeasuredWidth();
        Log.i(TAG, "onGlobalLayout : gridWidth=" + gridWidth + " recyclerView.getMeasuredHeight()= " + recyclerView.getMeasuredHeight());
        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
        int cellSize = defineMinSize(gridWidth, recyclerView.getMeasuredHeight(),
                columnsCount, rowsCount, itemMargin);
        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSize);

        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, opponents, (int) getResources().getDimension(R.dimen.item_width),
                (int) getResources().getDimension(R.dimen.item_height), gridWidth, columnsCount, (int) itemMargin,
                isVideoCall);
        opponentsAdapter.setAdapterListener(ConversationFragment.this);
        recyclerView.setAdapter(opponentsAdapter);
    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }

    private int defineMinSize(int measuredWidth, int measuredHeight, int columnsCount, int rowsCount, float padding) {
        int cellWidth = measuredWidth / columnsCount - (int) (padding * 2);
        int cellHeight = measuredHeight / rowsCount - (int) (padding * 2);
        return Math.min(cellWidth, cellHeight);
    }

    private int defineRowCount() {
        int result = DEFAULT_ROWS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount < 3) {
            result = opponentsCount;
        }
        return result;

    }

    private int defineColumnsCount() {
        int result = DEFAULT_COLS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount == 1 || opponentsCount == 2) {
            result = 1;
        } else if (opponentsCount == 4) {
            result = 2;
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && isVideoCall) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && isVideoCall) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeVideoTrackSListener();
        getActivity().unregisterReceiver(audioStreamReceiver);
        conversationFragmentCallbackListener.removeRTCClientConnectionCallback(this);
        conversationFragmentCallbackListener.removeRTCSessionUserCallback(this);
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
    }

    private void initButtonsListener() {
        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                toggleCamera(isChecked);
            }
        });

        micToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentSession != null) {
                    if (isAudioEnabled) {
                        Log.d(TAG, "Mic is off!");
                        conversationFragmentCallbackListener.onSetAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        Log.d(TAG, "Mic is on!");
                        conversationFragmentCallbackListener.onSetAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

                conversationFragmentCallbackListener.onHangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });
    }

    private void switchCamera(MenuItem item) {
        if (currentSession == null) {
            return;
        }
        final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
        if (mediaStreamManager == null) {
            return;
        }
        boolean cameraSwitched = mediaStreamManager.switchCameraInput(new Runnable() {
            @Override
            public void run() {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        toggleCameraInternal(mediaStreamManager);
                    }
                });
            }
        });

        if (CameraUtils.isCameraFront(currentSession.getMediaStreamManager().getCurrentCameraId())) {
            Log.d(TAG, "CameraFront now!");
            item.setIcon(R.drawable.ic_camera_front);
        } else {
            Log.d(TAG, "CameraRear now!");
            item.setIcon(R.drawable.ic_camera_rear);
        }
    }

    private void toggleCameraInternal(QBMediaStreamManager mediaStreamManager) {
        int currentCameraId = mediaStreamManager.getCurrentCameraId();
        Log.d(TAG, "Camera was switched!");
        RendererConfig config = setRTCCameraMirrorConfig(CameraUtils.isCameraFront(currentCameraId));
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleCameraOnUiThread(true);
            }
        }, TOGGLE_CAMERA_DELAY);
    }

    private void toggleCameraOnUiThread(final boolean toggle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleCamera(toggle);
            }
        });
    }

    private void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            conversationFragmentCallbackListener.onSetVideoEnabled(isNeedEnableCam);
        }
    }

    private void setOpponentsVisibility(int visibility) {
        for (OpponentsFromCallAdapter.ViewHolder RTCView : getAllOpponentsView()) {
            RTCView.getOpponentView().setVisibility(visibility);
        }
    }

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////

    private RTCGLVideoView.RendererConfig setRTCCameraMirrorConfig(boolean mirror) {
        RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
        config.mirror = mirror;
        return config;
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");

        localVideoTrack = videoTrack;
        if (localVideoView != null) {
            localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, setRTCCameraMirrorConfig(true));
            fillVideoView(localVideoView, videoTrack, false);
        }

        if (isPeerToPeerCall) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (localVideoView != null) {
                        return;
                    }
                    Log.i(TAG, "onLocalVideoTrackReceive init localView");
                    localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();

                    localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, setRTCCameraMirrorConfig(true));
                    localVideoView.setOnClickListener(localViewOnClickListener);

                    if (localVideoTrack != null) {
                        fillVideoView(localVideoView, localVideoTrack, false);
                    }
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);
        }
        //in other case localVideoView hasn't been inflated yet. Will set track while OnBindLastViewHolder
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

        if (isPeerToPeerCall) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDuringCallActionBar();
                    Log.d("onRemoteVideoTrackRe", "localVideoView==null?" + (localVideoView == null));
                    Log.d("onRemoteVideoTrackRe", "videoTrack==null?" + (videoTrack == null));
                    if (localVideoView == null) {
                        localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();
                    }
                    setLocalVideoView(videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);

        } else {
            final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
            if (itemHolder == null) {
                return;
            }
            final RTCGLVideoView remoteVideoView = itemHolder.getOpponentView();

            getVideoTrackMap().put(userID, videoTrack);


            if (remoteVideoView != null) {
                Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");
                if (isRemoteShown) {
                    Log.d(TAG, "USer onRemoteVideoTrackReceive = " + userID);
                    remoteVideoViewFromPreview = remoteVideoView;
                    fillVideoView(false, remoteVideoView, videoTrack);
                }

                if (!isRemoteShown) {
                    isRemoteShown = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            opponents.remove(itemHolder.getAdapterPosition());
                            opponentsAdapter.notifyItemRemoved(itemHolder.getAdapterPosition());
                            opponentsAdapter.notifyItemRangeChanged(itemHolder.getAdapterPosition(), opponents.size());
                            setDuringCallActionBar();
                            recyclerView.setVisibility(View.VISIBLE);
                            setOpponentsVisibility(View.VISIBLE);
                        }
                    });
                    setLocalVideoView(videoTrack);

                }
            }
        }
    }
    /////////////////////////////////////////    end    ////////////////////////////////////////////

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);
        if (!isVideoCall) {
            return;
        }
        if (isPeerToPeerCall) {
            Log.i(TAG, " isPeerToPeerCall");
            localVideoView = holder.getOpponentView();

        } else {
            //on group call we postpone initialization of localVideoView due to set it on Gui renderer.
            // Refer to RTCGlVIew
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

//                    if(clicked) {
//
//                        QBRTCVideoTrack videoTrack = getVideoTrackMap().get(opponentsAdapter.getItem(position));
//                        fillVideoView(holder.getOpponentView(), videoTrack);
//                        Log.i(TAG, " fillVideoView after click");
//                    }
                    if (localVideoView != null) {
                        return;
                    }
                    setOpponentsVisibility(View.GONE);
                    Log.i(TAG, "OnBindLastViewHolder init localView");
                    localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();
                    localVideoView.setOnClickListener(localViewOnClickListener);
                    if (localVideoTrack != null) {
                        fillVideoView(localVideoView, localVideoTrack, !isPeerToPeerCall);
                    }
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);
        }
    }

    @Override
    public void onItemClick(int position) {
        int userId = opponentsAdapter.getItem(position);
        if (!getVideoTrackMap().containsKey(userId)) {
            return;
        }


//ToDo Something wrong here
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userId);
        RTCGLVideoView remoteVideoView = itemHolder.getOpponentView();
        Log.d(TAG, "remoteVideoView = remoteVideoViewFromPreview? " + (remoteVideoView == remoteVideoViewFromPreview));
        Log.d(TAG, "USer onItemClick= " + userId);


//        for(Map.Entry<Integer, QBRTCVideoTrack> entry: getVideoTrackMap().entrySet()) {
//            System.out.println(entry.getKey());
//            QBRTCVideoTrack userVideoTrack2 = entry.getValue();
//            userVideoTrack2.removeRenderer(userVideoTrack2.getRenderer());
//        }

        if (oldqbUserID != 0) {
            userId = oldqbUserID;
        }

        for (Map.Entry<Integer, QBRTCVideoTrack> entry : getVideoTrackMap().entrySet()) {
            if (entry.getValue().equals(videoTrackFullScreen)) {
                oldqbUserID = entry.getKey();
                Log.d(TAG, "USer onItemClickentry.getValue()= " + oldqbUserID);
            }
        }
        QBRTCVideoTrack userVideoTrack = getVideoTrackMap().get(userId);


//        if (userId == oldqbUserID) {
//            if (lastclickedVideoTrackPreviewScreen != null) {
//                userVideoTrack = lastclickedVideoTrackPreviewScreen;
//            }
//        }
//        lastclickedVideoTrackPreviewScreen = videoTrackFullScreen;

        userVideoTrack.removeRenderer(userVideoTrack.getRenderer());
//        remoteVideoViewFromPreview.release();
//        localVideoView.release();
        videoTrackFullScreen.removeRenderer(videoTrackFullScreen.getRenderer());

        opponentsAdapter.notifyItemChanged(position);

        fillVideoView(false, remoteVideoView, videoTrackFullScreen);
        Log.d(TAG, "remoteVideoView enabled");

        fillVideoView(true, localVideoView, userVideoTrack);
        Log.d(TAG, "fullscreen enabled");

        fillVideoView(localVideoView, localVideoTrack, false);
        RTCGLVideoView.RendererConfig config = setRTCCameraMirrorConfig(true);
        config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
        Log.d(TAG, "small screen enabled");
//
//        oldqbUserID = userId;
    }

    private void setLocalVideoView(QBRTCVideoTrack videoTrack) {
        RTCGLVideoView.RendererConfig config = setRTCCameraMirrorConfig(true);
        config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
        config = setRTCCameraMirrorConfig(false);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.MAIN, config);
        fillVideoView(true, localVideoView, videoTrack);
    }

    private void startTimer() {
        if (!isStarted) {
            timerABWithTimer.setVisibility(View.VISIBLE);
            timerABWithTimer.setBase(SystemClock.elapsedRealtime());
            timerABWithTimer.start();
            isStarted = true;
        }
    }

    private void stopTimer() {
        if (timerABWithTimer != null) {
            timerABWithTimer.stop();
            isStarted = false;
        }
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.append(userID, holder);
            }
        }
        return holder;
    }

    private List<OpponentsFromCallAdapter.ViewHolder> getAllOpponentsView() {
        int childCount = recyclerView.getChildCount();
        List<OpponentsFromCallAdapter.ViewHolder> holders = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            holders.add(childViewHolder);
        }
        return holders;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        for (OpponentsFromCallAdapter.ViewHolder childViewHolder : getAllOpponentsView()) {
            Log.d(TAG, "getViewForOpponent holder user id is : " + childViewHolder.getUserId());
            if (userID.equals(childViewHolder.getUserId())) {
                return childViewHolder;
            }
        }
        return null;
    }

    private void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ?
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.MAIN) :
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.SECOND)));
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(boolean localView, RTCGLVideoView videoView, QBRTCVideoTrack videoTrack) {
        if (localView) {
            videoTrackFullScreen = videoTrack;
        }
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        if (isPeerToPeerCall) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStatusLocal.setText(status);
                }
            });
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.setStatus(status);
            }
        });
    }

    private void setProgressBarForOpponentGone(int userId) {
        if (isPeerToPeerCall) {
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.getProgressBar().setVisibility(View.GONE);
            }
        });
    }

    private void setBackgroundOpponentView(Integer userId) {
        OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }
        holder.getOpponentView().setBackgroundColor(Color.parseColor("#ffffff"));
    }


    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        setStatusForOpponent(userId, getString(R.string.connected));
        setProgressBarForOpponentGone(userId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startTimer();
            }
        });
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.closed));
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }
    //////////////////////////////////   end     //////////////////////////////////////////


    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////

    @Override
    public void onSessionClosed() {
        stopTimer();
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setProgressBarForOpponentGone(userId);
        setStatusForOpponent(userId, getString(R.string.noAnswer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.hungUp));
    }
    //////////////////////////////////   end     //////////////////////////////////////////


    public void enableDynamicToggle(boolean plugged) {
        headsetPlugged = plugged;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.audio_switch:
                Log.d("Conversation", "audio_switch");
                callEvents.onSwitchAudio();
                if (!headsetPlugged) {
                    if (!item.isChecked()) {
                        item.setChecked(!item.isChecked());
                        item.setIcon(R.drawable.ic_speaker_phone);
                    } else {
                        item.setChecked(!item.isChecked());
                        item.setIcon(R.drawable.ic_phonelink_ring);
                    }
                }
                return true;
            case R.id.camera_switch:
                Log.d("Conversation", "camera_switch");
                switchCamera(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }
        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class FragmentLifeCycleHandler extends Handler {

        @Override
        public void dispatchMessage(Message msg) {
            if (isAdded() && getActivity() != null) {
                super.dispatchMessage(msg);
            } else {
                Log.d(TAG, "Fragment under destroying");
            }
        }
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }

    class LocalViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setFullScreenOnOff();
        }

        private void setFullScreenOnOff() {
            if (actionBar.isShowing()) {
                hideToolBarAndButtons();
            } else {
                showToolBarAndButtons();
            }
        }

        private void hideToolBarAndButtons() {
            actionBar.hide();
            localVideoView.releaseLocalRendererCallback();
            actionVideoButtonsLayout.setVisibility(View.GONE);

            if (!isPeerToPeerCall) {
                shiftBottomListOpponents();
            }
        }

        private void showToolBarAndButtons() {
            actionBar.show();
            fillVideoView(localVideoView, localVideoTrack, false);
            RendererConfig config = setRTCCameraMirrorConfig(CameraUtils.isCameraFront(currentSession.getMediaStreamManager().getCurrentCameraId()));
            config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
            localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
            actionVideoButtonsLayout.setVisibility(View.VISIBLE);

            if (!isPeerToPeerCall) {
                shiftMarginListOpponents();
            }
        }

        private void shiftBottomListOpponents() {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            params.setMargins(0, 0, 0, 0);

            recyclerView.setLayoutParams(params);
        }

        private void shiftMarginListOpponents() {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_common));

            recyclerView.setLayoutParams(params);
        }

    }
}


