package com.quickblox.sample.videochat.conference.java.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.view.QBConferenceSurfaceView;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.videochat.conference.java.managers.WebRtcSessionManager;
import com.quickblox.sample.videochat.conference.java.services.CallService;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBUsersHolder;
import com.quickblox.sample.videochat.conference.java.views.NoChildClickableRecyclerView;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ConversationFragment extends BaseToolBarFragment implements CallService.CurrentCallStateCallback, QBRTCSessionStateCallback<ConferenceSession>,
        QBRTCClientVideoTracksCallbacks<ConferenceSession>, OpponentsFromCallAdapter.OnAdapterEventListener, CallService.OnlineParticipantsChangeListener {

    private static final String TAG = ConversationFragment.class.getSimpleName();

    private static final long CONTROL_LAYOUT_DISPLAYING_TIME = 5000;
    private static final long LOCAL_TRACK_INITIALIZE_DELAY = 500;
    private static final int REQUEST_ADD_OCCUPANTS = 175;
    private static final int SPAN_COUNT = 12;

    private WebRtcSessionManager sessionManager;
    private ConferenceSession currentSession;
    private QBUsersHolder qbUsersHolder;
    private ArrayList<Integer> opponentsIds;
    private Set<Integer> usersToDestroy;
    private boolean allCallbacksInit;
    private NoChildClickableRecyclerView recyclerView;
    private QBConferenceSurfaceView fullScreenVideoView;
    private boolean isRemoteShown;
    private OpponentsFromCallAdapter opponentsAdapter;
    private boolean isNeedCleanUp;
    private TextView conversationPlaceholder;
    private ToggleButton micToggle;
    private ToggleButton cameraToggle;
    private ToggleButton endCallToggle;
    private ToggleButton screenSharingToggle;
    private ToggleButton swapCamToggle;
    private RelativeLayout conferenceToolbar;
    private ImageButton ibGoToChat;
    private ImageButton ibManageGroup;
    private TextView tvMembersCount;
    private ImageView ivStreamLabel;
    private View controlLayout;
    private ControlsDispatcher controlsDispatcher;
    private boolean controlLayoutVisible;
    private CameraState cameraState = CameraState.DISABLED_FROM_USER;
    private ConversationFragmentCallback conversationFragmentCallback;
    protected QBUser currentUser;
    private Integer fullScreenUserID;
    private String roomTitle;
    private SharedPrefsHelper sharedPrefsHelper;
    private boolean asListenerRole;
    private Map<Integer, Boolean> onlineParticipants = new HashMap<>();
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            conversationFragmentCallback = (ConversationFragmentCallback) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString()
                    + " must implement ConversationFragmentCallbackListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onStart");
            return;
        }

        if (!allCallbacksInit) {
            conversationFragmentCallback.addClientConnectionCallback(this);
            initTrackListeners();
            allCallbacksInit = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() != null && getActivity().getApplicationContext() != null) {
            qbUsersHolder = ((App) getActivity().getApplicationContext()).getQBUsersHolder();
            sharedPrefsHelper = ((App) getActivity().getApplicationContext()).getSharedPrefsHelper();
        }
        conversationFragmentCallback.addCurrentCallStateCallback(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (this.getArguments() != null) {
            opponentsIds = this.getArguments().getIntegerArrayList(Consts.EXTRA_DIALOG_OCCUPANTS);
        }
        asListenerRole = conversationFragmentCallback.isListenerRole();
        roomTitle = conversationFragmentCallback.getRoomTitle();
        sessionManager = WebRtcSessionManager.getInstance();
        currentSession = sessionManager.getCurrentSession();
        if (currentSession == null) {
            Log.d(TAG, "currentSession = null onCreateView");
            return view;
        }
        initFields();
        initViews(view);
        initButtonsListener();
        controlsDispatcher = new ControlsDispatcher();
        controlsDispatcher.wakeupControls();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isNeedCleanUp = true;
        cleanAdapterIfNeed();
        controlsDispatcher.wakeupControls();
        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (!conversationFragmentCallback.isListenerRole()) {
            boolean isCamEnabled = sharedPrefsHelper.get(Consts.PREF_CAM_ENABLED, false);
            if (cameraState != CameraState.DISABLED_FROM_USER) {
                toggleCamera(isCamEnabled);
            }
        }
    }

    @Override
    public void onPause() {
        isNeedCleanUp = false;
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && !conversationFragmentCallback.isListenerRole() && !conversationFragmentCallback.isScreenSharingState()) {
            toggleCamera(false);
        }
        releaseViews();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        conversationFragmentCallback.removeCurrentCallStateCallback(this);
        super.onDestroy();
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_conference;
    }

    private void initFields() {
        currentUser = sharedPrefsHelper.getQbUser();
        sessionManager = WebRtcSessionManager.getInstance();
        currentSession = sessionManager.getCurrentSession();
        opponentViewHolders = new SparseArray<>(opponentsIds.size());
        isRemoteShown = false;
        usersToDestroy = new HashSet<>();

        Log.v(TAG, "initOpponentsList() opponentsIds= " + opponentsIds);
        Log.d(TAG, "currentSession " + currentSession.toString());
    }

    private void initViews(View view) {
        Log.i(TAG, "initViews");
        conversationPlaceholder = view.findViewById(R.id.conversation_placeholder);
        micToggle = view.findViewById(R.id.tb_switch_mic);
        cameraToggle = view.findViewById(R.id.tb_switch_cam);
        cameraToggle.setVisibility(View.VISIBLE);
        endCallToggle = view.findViewById(R.id.tb_end_call);
        screenSharingToggle = view.findViewById(R.id.tb_screen_share);
        swapCamToggle = view.findViewById(R.id.tb_swap_cam);
        conferenceToolbar = view.findViewById(R.id.rl_conference_toolbar);
        ibGoToChat = view.findViewById(R.id.ib_chat);
        ibManageGroup = view.findViewById(R.id.ib_manage_group);
        tvMembersCount = view.findViewById(R.id.tv_members_count);
        ivStreamLabel = view.findViewById(R.id.iv_stream_label);

        TextView toolbarTitle = view.findViewById(R.id.tv_conference_title);
        toolbarTitle.setText(roomTitle);
        toolbarTitle.setSelected(true);

        if (isStream()) {
            toolbarTitle.setVisibility(View.INVISIBLE);
            ibManageGroup.setVisibility(View.INVISIBLE);
            ivStreamLabel.setVisibility(View.VISIBLE);

            if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED.equals(currentSession.getState())) {
                ivStreamLabel.setImageResource(R.drawable.live_streaming);
            } else {
                ivStreamLabel.setImageResource(R.drawable.offline_streaming);
            }

            if (conversationFragmentCallback.isListenerRole()) {
                tvMembersCount.setVisibility(View.GONE);
            } else {
                tvMembersCount.setVisibility(View.VISIBLE);
                updateToolbar();
            }
        }
        controlLayout = view.findViewById(R.id.element_set_call_buttons);

        boolean isCamEnabled = sharedPrefsHelper.get(Consts.PREF_CAM_ENABLED, false);
        boolean isMicEnabled = sharedPrefsHelper.get(Consts.PREF_MIC_ENABLED, true);
        boolean isScreenSharingChecked = sharedPrefsHelper.get(Consts.PREF_SCREEN_SHARING_TOGGLE_CHECKED, true);
        boolean isSwapCamChecked = sharedPrefsHelper.get(Consts.PREF_SWAP_CAM_TOGGLE_CHECKED, true);

        cameraToggle.setChecked(isCamEnabled);
        micToggle.setChecked(isMicEnabled);
        screenSharingToggle.setChecked(isScreenSharingChecked);
        swapCamToggle.setChecked(isSwapCamChecked);

        fullScreenVideoView = view.findViewById(R.id.full_screen_video);

        recyclerView = view.findViewById(R.id.grid_opponents);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
        recyclerView.setHasFixedSize(false);

        GridLayoutManager gridLayoutManager = new GridManager(getActivity(), SPAN_COUNT);
        gridLayoutManager.setReverseLayout(false);
        SpanSizeLookupImpl spanSizeLookup = new SpanSizeLookupImpl();
        spanSizeLookup.setSpanIndexCacheEnabled(false);
        gridLayoutManager.setSpanSizeLookup(spanSizeLookup);

        recyclerView.setLayoutManager(gridLayoutManager);

        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, new ArrayList<>(), recyclerView.getHeight());
        //for correct removing item in adapter
        recyclerView.setItemAnimator(null);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setGrid(recyclerView.getHeight());
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (currentSession.getState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED) {
                    startJoinConference();
                } else {
                    Set<Integer> publishers = currentSession.getActivePublishers();

                    QBRTCVideoTrack localVideoTrack = conversationFragmentCallback.getVideoTrackMap().get(currentUser.getId());
                    if (localVideoTrack != null) {
                        boolean isCamEnabled = sharedPrefsHelper.get(Consts.PREF_CAM_ENABLED, false);
                        localVideoTrack.setEnabled(isCamEnabled);
                        onConnectedToUser(currentSession, currentUser.getId());
                    }

                    for (Integer publisher : publishers) {
                        onConnectedToUser(currentSession, publisher);
                    }
                    actionButtonsEnabled(true);
                }
            }
        });

        if (conversationFragmentCallback.isListenerRole()) {
            actionButtonsEnabled(true);
        } else {
            actionButtonsEnabled(false);
        }
        setActionButtonsVisibility();
    }

    private void updateToolbar() {
        if (onlineParticipants != null && getContext() != null) {
            String membersTitle;
            Integer onlineUsers = onlineParticipants.size() - 1;
            if (onlineUsers == -1) {
                onlineUsers = 0;
            }
            if (onlineUsers != 1) {
                membersTitle = getString(R.string.online_participants_label, onlineUsers.toString());
            } else {
                membersTitle = getString(R.string.online_participants_label_single, "1");
            }

            tvMembersCount.setText(membersTitle);
        }
    }

    private void startJoinConference() {
        conversationFragmentCallback.onStartJoinConference();
    }

    private void switchCamera() {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return;
        }
        cameraToggle.setEnabled(false);
        conversationFragmentCallback.onSwitchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                Log.d(TAG, "Camera swapped to :" + b);
                toggleCameraInternal();
            }

            @Override
            public void onCameraSwitchError(String s) {
                Log.d(TAG, "camera swap error " + s);
                cameraToggle.setEnabled(true);
            }
        });
    }

    private void toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!");
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(currentUser.getId());
        QBConferenceSurfaceView localVideoView = itemHolder.getSurfaceView();
        updateVideoView(localVideoView);
        toggleCamera(true);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            conversationFragmentCallback.onSetVideoEnabled(isNeedEnableCam);
        }
        if (!cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    private void startScreenSharing() {
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.onStartScreenSharing();
        }
    }

    private void setProgressBarForOpponentGone(int userId) {
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }
        holder.getProgressBar().setVisibility(View.GONE);
    }

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);
    }

    @Override
    public void onToggleButtonItemClick(Integer userID, boolean isAudioEnabled) {
        Log.d(TAG, "onToggleButtonItemClick userId= " + userID);
        adjustOpponentAudio(userID, isAudioEnabled);

    }

    @Override
    public void onOpponentViewItemClick(Integer userID) {
        if (controlLayoutVisible && !isStream()) {
            fullScreenUserID = userID;
            if (getViewHolderForOpponent(fullScreenUserID).getSurfaceView().getVisibility() == View.VISIBLE) {
                swapUserToFullscreen(fullScreenUserID);
            }
        }
        controlsDispatcher.wakeupControls();
    }

    private void adjustOpponentAudio(int userID, boolean isAudioEnabled) {
        currentSession.getMediaStreamManager().getAudioTrack(userID).setEnabled(isAudioEnabled);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_OCCUPANTS) {
                Log.d(TAG, "onActivityResult REQUEST_ADD_OCCUPANTS");
            }
        }
    }

    private void initTrackListeners() {
        initVideoTracksListener();
    }

    private void removeTrackListeners() {
        removeVideoTracksListener();
    }

    private void removeConnectionStateListeners() {
        conversationFragmentCallback.removeClientConnectionCallback(this);
    }

    private void setActionButtonsVisibility() {
        if (asListenerRole) {
            setActionButtonsInvisible();
        }
    }

    private void setActionButtonsInvisible() {
        micToggle.setVisibility(View.INVISIBLE);
        cameraToggle.setVisibility(View.INVISIBLE);
        screenSharingToggle.setVisibility(View.INVISIBLE);
        swapCamToggle.setVisibility(View.INVISIBLE);
    }

    private void setGrid(int itemHeight) {
        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, new ArrayList<>(), itemHeight);
        opponentsAdapter.setAdapterListener(this);
        recyclerView.setAdapter(opponentsAdapter);
    }

    private void releaseViewHolders() {
        if (opponentViewHolders != null) {
            opponentViewHolders.clear();
        }
    }

    private void removeVideoTrackRenderers() {
        Map<Integer, QBRTCVideoTrack> videoTrackMap = conversationFragmentCallback.getVideoTrackMap();
        for (QBRTCVideoTrack videoTrack : videoTrackMap.values()) {
            if (videoTrack.getRenderer() != null) {
                Log.d(TAG, "remove opponent video Tracks");
                videoTrack.removeRenderer(videoTrack.getRenderer());
            }
        }
    }

    private void releaseViews() {
        if (fullScreenVideoView != null) {
            fullScreenVideoView.release();
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int childCount = 0;
        if (layoutManager != null) {
            childCount = layoutManager.getChildCount();
        }
        Log.d(TAG, " releaseOpponentsViews for  " + childCount + " views");
        for (int i = 0; i < childCount; i++) {
            View childView = layoutManager.getChildAt(i);
            Log.d(TAG, " release View for  " + i + ", " + childView);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = null;
            if (childView != null) {
                childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
                QBConferenceSurfaceView view = childViewHolder.getSurfaceView();
                view.release();
            }
        }
    }

    private void initButtonsListener() {
        micToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (controlLayoutVisible) {
                    conversationFragmentCallback.onSetAudioEnabled(isChecked);
                    sharedPrefsHelper.save(Consts.PREF_MIC_ENABLED, isChecked);
                } else {
                    controlsDispatcher.wakeupControls();
                }
            }
        });

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (controlLayoutVisible) {
                    if (cameraState != CameraState.DISABLED_FROM_USER) {
                        toggleCamera(isChecked);
                        sharedPrefsHelper.save(Consts.PREF_CAM_ENABLED, isChecked);
                        swapCamToggle.setEnabled(isChecked);
                    }
                }
                controlsDispatcher.wakeupControls();
            }
        });

        endCallToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlLayoutVisible) {
                    conversationFragmentCallback.onLeaveCurrentSession();
                    actionButtonsEnabled(false);
                    removeVideoTrackRenderers();
                    releaseViews();
                    releaseViewHolders();
                    removeConnectionStateListeners();
                    removeTrackListeners();

                    Log.d(TAG, "Call is stopped");
                }
                controlsDispatcher.wakeupControls();
            }
        });

        screenSharingToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                QBRTCVideoTrack videoTrack = conversationFragmentCallback.getVideoTrackMap().get(currentUser.getId());
                if (videoTrack == null) {
                    screenSharingToggle.setChecked(!isChecked);
                    return;
                }
                if (controlLayoutVisible) {
                    toggleCamera(true);
                    startScreenSharing();
                }
                controlsDispatcher.wakeupControls();
            }
        });

        swapCamToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean isCamEnabled = sharedPrefsHelper.get(Consts.PREF_CAM_ENABLED, false);
                if (!isCamEnabled) {
                    swapCamToggle.setChecked(!isChecked);
                    return;
                }
                if (controlLayoutVisible) {
                    switchCamera();
                    sharedPrefsHelper.save(Consts.PREF_SWAP_CAM_TOGGLE_CHECKED, isChecked);
                }
                controlsDispatcher.wakeupControls();
            }
        });

        ibGoToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlLayoutVisible) {
                    conversationFragmentCallback.onReturnToChat();
                }
                controlsDispatcher.wakeupControls();

            }
        });

        ibManageGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlLayoutVisible) {
                    conversationFragmentCallback.onManageGroup();
                }
                controlsDispatcher.wakeupControls();
            }
        });

        fullScreenVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controlLayoutVisible) {
                    dismissFullScreen();
                }
                controlsDispatcher.wakeupControls();
            }
        });

        conversationPlaceholder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controlsDispatcher.wakeupControls();
            }
        });

        recyclerView.setOnNoChildClickListener(new NoChildClickableRecyclerView.OnNoChildClickListener() {
            @Override
            public void onNoChildClick() {
                controlsDispatcher.wakeupControls();
            }
        });
    }

    private void swapUserToFullscreen(Integer userId) {
        QBRTCVideoTrack videoTrack = conversationFragmentCallback.getVideoTrackMap().get(userId);
        if (videoTrack != null) {
            opponentsAdapter.clearOpponents(new ArrayList<>());
            opponentsAdapter.notifyDataSetChanged();
            fullScreenVideoView.setVisibility(View.VISIBLE);
            fillVideoView(fullScreenVideoView, videoTrack, true);

            for (Map.Entry<Integer, QBRTCVideoTrack> entry : conversationFragmentCallback.getVideoTrackMap().entrySet()) {
                QBRTCVideoTrack track = entry.getValue();
                Integer userID = entry.getKey();
                if (!userID.equals(userId)) {
                    track.removeRenderer(track.getRenderer());
                }
            }
            Log.d(TAG, "Fullscreen Enabled");
        }
    }

    private void dismissFullScreen() {
        if (fullScreenVideoView.getVisibility() != View.GONE) {
            fullScreenVideoView.setVisibility(View.GONE);

            for (Map.Entry<Integer, QBRTCVideoTrack> entry : conversationFragmentCallback.getVideoTrackMap().entrySet()) {
                QBRTCVideoTrack track = entry.getValue();
                Integer userID = entry.getKey();
                track.removeRenderer(track.getRenderer());
                if (getViewHolderForOpponent(userID) != null) {
                    fillVideoView(getViewHolderForOpponent(userID).getSurfaceView(), track, true);
                }
            }
            Log.d(TAG, "Fullscreen Disabled");
        }
    }

    private void fadeOutButtons() {
        controlLayoutVisible = false;
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new DecelerateInterpolator());
        fadeOut.setDuration(1800);

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (controlLayout != null) {
                        controlLayout.setAnimation(fadeOut);
                        controlLayout.startAnimation(fadeOut);
                    }

                    if (conferenceToolbar != null) {
                        conferenceToolbar.setAnimation(fadeOut);
                        conferenceToolbar.startAnimation(fadeOut);
                    }
                }
            });
        }

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                actionButtonsEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                controlLayout.setVisibility(View.INVISIBLE);
                conferenceToolbar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void fadeInButtons() {
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(250);

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (controlLayout != null) {
                        controlLayout.setAnimation(fadeIn);
                        controlLayout.startAnimation(fadeIn);
                    }

                    if (conferenceToolbar != null) {
                        conferenceToolbar.setAnimation(fadeIn);
                        conferenceToolbar.startAnimation(fadeIn);
                    }
                }
            });
        }
        controlLayoutVisible = true;

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                actionButtonsEnabled(true);
                controlLayout.setVisibility(View.VISIBLE);
                conferenceToolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void actionButtonsEnabled(boolean enabled) {
        micToggle.setEnabled(enabled);
        micToggle.setActivated(enabled);
        cameraToggle.setEnabled(enabled);
        cameraToggle.setActivated(enabled);
        endCallToggle.setEnabled(enabled);
        endCallToggle.setActivated(enabled);
        screenSharingToggle.setEnabled(enabled);
        screenSharingToggle.setActivated(enabled);
        swapCamToggle.setEnabled(enabled);
        swapCamToggle.setActivated(enabled);
        ibManageGroup.setEnabled(enabled);
        ibGoToChat.setEnabled(enabled);
        ibGoToChat.setActivated(enabled);
    }

    private boolean isStream() {
        return !conversationFragmentCallback.getRoomID().equals(conversationFragmentCallback.getDialogID());
    }

    @Override
    public void onCallStarted() {
        actionButtonsEnabled(true);
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            Log.d(TAG, "View Holder == null");
            holder = findHolder(userID);
            if (holder != null) {
                Log.d(TAG, "View Holder found");
                opponentViewHolders.put(userID, holder);
            }
        }
        return holder;
    }

    private void cleanUpAdapter(int userId) {
        Log.d(TAG, "cleanUpAdapter of userId= " + userId);
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userId);
        if (itemHolder != null) {
            if (itemHolder.getAdapterPosition() != -1) {
                Log.d(TAG, "opponentsAdapter.removeItem");
                opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
                opponentViewHolders.remove(userId);
            }
        }
        recyclerView.requestLayout();
    }

    private void cleanAdapterIfNeed() {
        if (!usersToDestroy.isEmpty()) {
            Iterator<Integer> iterator = usersToDestroy.iterator();
            while (iterator.hasNext()) {
                cleanUpAdapter(iterator.next());
                iterator.remove();
            }
        }
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        Log.d(TAG, "findHolder for " + userID);
        int childCount = recyclerView.getChildCount();
        Log.d(TAG, "findHolder for childCount= " + childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            Log.d(TAG, "childViewHolder.getUserId= " + childViewHolder.getUserId());
            if (userID.equals(childViewHolder.getUserId())) {
                Log.d(TAG, "return childViewHolder");
                return childViewHolder;
            }
        }
        return null;
    }

    private boolean checkIfUserInAdapter(int userId) {
        if (opponentsAdapter != null) {
            for (QBUser user : opponentsAdapter.getOpponents()) {
                if (user.getId() == userId) {
                    Log.d(TAG, "User already in adapter");
                    return true;
                }
            }
        }
        Log.d(TAG, "User is still NOT in adapter");
        return false;
    }

    private void loadUserById(int userID) {
        Log.d(TAG, "Loading User by ID");
        QBUsers.getUser(userID).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "User successfully loaded");
                qbUsersHolder.putUser(qbUser);
                if (!TextUtils.isEmpty(qbUser.getFullName())) {
                    opponentsAdapter.updateUserFullName(qbUser);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Load User Error" + e.getMessage());
            }
        });
    }

    private void setOpponentToAdapter(Integer userID) {
        if (!isRemoteShown) {
            isRemoteShown = true;
        }

        QBUser qbUser = qbUsersHolder.getUserById(userID);
        if (qbUser == null) {
            qbUser = new QBUser(userID);
            qbUser.setFullName(getString(R.string.load_user));
            loadUserById(userID);
        }
        opponentsAdapter.add(qbUser);
    }

    //////////////////////////////////////////
    //    Session State Callback
    //////////////////////////////////////////

    @Override
    public void onConnectedToUser(ConferenceSession qbrtcSession, final Integer userId) {
        if (conversationFragmentCallback.isListenerRole()) {
            ivStreamLabel.setImageResource(R.drawable.live_streaming);
        }

        if (conversationFragmentCallback.isScreenSharingState()) {
            return;
        }

        if (checkIfUserInAdapter(userId)) {
            Log.d(TAG, "onConnectedToUser user already in, userId= " + userId);
            return;
        }

        conversationPlaceholder.setVisibility(View.INVISIBLE);
        setOpponentToAdapter(userId);

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRemoteViewMultiCall(userId);
                setProgressBarForOpponentGone(userId);
            }
        }, LOCAL_TRACK_INITIALIZE_DELAY);
    }

    @Override
    public void onConnectionClosedForUser(ConferenceSession qbrtcSession, Integer userID) {
        Log.d(TAG, "onConnectionClosedForUser userId= " + userID);
        if (currentSession.isDestroyed()) {
            Log.d(TAG, "onConnectionClosedForUser isDestroyed userId= " + userID);
            return;
        }
        if (userID.equals(fullScreenUserID)) {
            dismissFullScreen();
        }
        if (isNeedCleanUp) {
            cleanUpAdapter(userID);
        } else {
            usersToDestroy.add(userID);
        }
        if (isStream()) {
            ivStreamLabel.setImageResource(R.drawable.offline_streaming);
            conversationPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDisconnectedFromUser(ConferenceSession qbrtcSession, Integer userID) {

    }

    @Override
    public void onStateChanged(ConferenceSession session, BaseSession.QBRTCSessionState state) {
        if (isStream() && !conversationFragmentCallback.isListenerRole()) {
            if (BaseSession.QBRTCSessionState.QB_RTC_SESSION_CONNECTED.equals(state)) {
                ivStreamLabel.setImageResource(R.drawable.live_streaming);
            } else {
                ivStreamLabel.setImageResource(R.drawable.offline_streaming);
            }
        }
    }

    //////////////////////////////////////////
    //    Video Tracks Callbacks
    //////////////////////////////////////////

    @Override
    public void onLocalVideoTrackReceive(ConferenceSession session, QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive");
        cameraState = CameraState.NONE;
        actionButtonsEnabled(true);

        boolean isNeedEnableLocalVideo = sharedPrefsHelper.get(Consts.PREF_CAM_ENABLED, false);
        if (conversationFragmentCallback.isScreenSharingState()) {
            return;
        }
        conversationFragmentCallback.onSetVideoEnabled(isNeedEnableLocalVideo);
        onConnectedToUser(session, currentUser.getId());
    }

    @Override
    public void onRemoteVideoTrackReceive(ConferenceSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
    }

    private void setRemoteViewMultiCall(int userID) {
        if (currentSession.isDestroyed()) {
            Log.d(TAG, "setRemoteViewMultiCall currentSession.isDestroyed RETURN");
            return;
        }
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView");

        final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
        if (itemHolder == null) {
            Log.d(TAG, "itemHolder == null - true");
            return;
        }
        final QBConferenceSurfaceView remoteVideoView = itemHolder.getSurfaceView();

        if (remoteVideoView != null) {
            remoteVideoView.setZOrderMediaOverlay(true);
            updateVideoView(remoteVideoView);
            Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");

            QBRTCVideoTrack remoteVideoTrack = conversationFragmentCallback.getVideoTrackMap().get(userID);
            if (remoteVideoTrack != null) {
                fillVideoView(remoteVideoView, remoteVideoTrack, true);
            }
        }
    }

    private void updateVideoView(SurfaceViewRenderer surfaceViewRenderer) {
        Log.i(TAG, "updateVideoView");
        surfaceViewRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        surfaceViewRenderer.setMirror(false);
        surfaceViewRenderer.requestLayout();
    }

    private void fillVideoView(QBConferenceSurfaceView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.removeRenderer(videoTrack.getRenderer());
        videoTrack.addRenderer(videoView);
        cameraState = CameraState.NONE;
        updateVideoView(videoView);
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void initVideoTracksListener() {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(this);
        }
    }

    private void removeVideoTracksListener() {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
    }

    @Override
    public void onParticipantsCountChanged(Map<Integer, Boolean> onlineParticipants) {
        this.onlineParticipants = onlineParticipants;
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateToolbar();
                }
            });
        }
    }

    private class GridManager extends GridLayoutManager {

        GridManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
            super.onItemsAdded(recyclerView, positionStart, itemCount);
            Log.d("GridManager", "onItemsAdded positionStart= " + positionStart);
        }

        @Override
        public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
            super.onItemsRemoved(recyclerView, positionStart, itemCount);
            Log.d("GridManager", "onItemsRemoved positionStart= " + positionStart);
            updateAdaptersItems();
        }

        private void updateAdaptersItems() {
            if (opponentsAdapter.getItemCount() > 0) {
                OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(opponentsAdapter.getItem(0));
                if (itemHolder != null) {
                    itemHolder.itemView.requestLayout();
                }
            }
        }

        @Override
        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
            super.onItemsUpdated(recyclerView, positionStart, itemCount, payload);
            Log.d("GridManager", "onItemsUpdated positionStart= " + positionStart);
        }

        @Override
        public void onItemsChanged(RecyclerView recyclerView) {
            super.onItemsChanged(recyclerView);
            Log.d("GridManager", "onItemsChanged");
        }

        @Override
        public void onLayoutCompleted(RecyclerView.State state) {
            super.onLayoutCompleted(state);
            Log.d("GridManager", "onLayoutCompleted");
        }
    }

    private class SpanSizeLookupImpl extends GridManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            int itemCount = opponentsAdapter.getItemCount();
            int spanSize = 0;

            if (itemCount >= 1 && itemCount <= 2) {
                spanSize = 12; // Whole size
            }
            if (itemCount >= 3 && itemCount <= 4) {
                spanSize = 6; // 2 rows
            }
            if (itemCount >= 5 && itemCount <= 12) {
                spanSize = 4; // 3 rows
            }
            if (itemCount >= 13) {
                spanSize = 3; // 4 rows
            }

            return spanSize;
        }
    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }

    private class ControlsDispatcher {
        private Timer timer = new Timer();

        void wakeupControls() {
            if (!controlLayoutVisible) {
                fadeInButtons();
            }
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    fadeOutButtons();
                }
            }, CONTROL_LAYOUT_DISPLAYING_TIME);
        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }
}