package com.quickblox.sample.videochat.java.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.activities.CallActivity;
import com.quickblox.sample.videochat.java.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.videochat.java.services.CallService;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCSurfaceView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSink;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.quickblox.videochat.webrtc.QBRTCTypes.QBRTCReconnectionState.QB_RTC_RECONNECTION_STATE_RECONNECTING;

/**
 * QuickBlox team
 */
public class VideoConversationFragment extends BaseConversationFragment implements Serializable,
        QBRTCClientVideoTracksCallbacks<QBRTCSession>, QBRTCSessionStateCallback<QBRTCSession>,
        QBRTCSessionEventsCallback, OpponentsFromCallAdapter.OnAdapterEventListener {
    private final String TAG = VideoConversationFragment.class.getSimpleName();

    public static final String CAMERA_ENABLED = "is_camera_enabled";
    public static final String IS_CURRENT_CAMERA_FRONT = "is_camera_front";
    private static final long LOCAL_TRACK_INITIALIZE_DELAY = 800;
    private static final int RECYCLE_VIEW_PADDING = 2;
    private static final long UPDATING_USERS_DELAY = 2000;
    private static final long FULL_SCREEN_CLICK_DELAY = 1000;

    private ToggleButton cameraToggle;
    private View parentView;
    private LinearLayout actionVideoButtonsLayout;
    private TextView connectionStatusLocal;
    private RecyclerView recyclerView;
    private QBRTCSurfaceView localVideoView;
    private FrameLayout flLocalVideoView;
    private LinearLayout reconnectionProgress;
    private QBRTCSurfaceView remoteFullScreenVideoView;

    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private OpponentsFromCallAdapter opponentsAdapter;
    private List<QBUser> allOpponents;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private Menu optionsMenu;
    private int userIdFullScreen;
    private boolean connectionEstablished;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;
    private boolean isLocalVideoFullScreen;

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_video_conversation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = super.onCreateView(inflater, container, savedInstanceState);
        return parentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if (!allCallbacksInit) {
            addListeners();
            allCallbacksInit = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        toggleCamera(cameraToggle.isChecked());
    }

    @Override
    public void onPause() {
        toggleCamera(false);

        if (connectionEstablished) {
            allCallbacksInit = false;
        } else {
            Log.d(TAG, "We are in dialing process yet!");
        }

        releaseViewHolders();
        removeListeners();
        releaseViews();

        SharedPrefsHelper.getInstance().save(IS_CURRENT_CAMERA_FRONT, isCurrentCameraFront);
        super.onPause();
    }

    @Override
    protected void configureOutgoingScreen() {
        Context context = getActivity();
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.grey_transparent_50));
        allOpponentsTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
        ringingTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    protected void configureActionBar() {
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void configureToolbar() {
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.black_transparent_50));
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getActivity(), R.color.white));
    }

    @Override
    protected void initFields() {
        super.initFields();
        localViewOnClickListener = new LocalViewOnClickListener();
        allOpponents = Collections.synchronizedList(new ArrayList<>(opponents.size()));
        allOpponents.addAll(opponents);

        timerCallText = getActivity().findViewById(R.id.timer_call);

        isPeerToPeerCall = opponents.size() == 1;
    }

    public void setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(false);
        QBUser userFullScreen;
        if (isPeerToPeerCall) {
            userFullScreen = opponents.get(0);
        } else {
            userFullScreen = dbManager.getUserById(userIdFullScreen);
        }
        if (userFullScreen == null) {
            return;
        }
        String name;
        if (TextUtils.isEmpty(userFullScreen.getFullName())) {
            name = userFullScreen.getLogin();
        } else {
            name = userFullScreen.getFullName();
        }
        connectionStatusLocal.setText(name);
        actionButtonsEnabled(true);
    }

    private void addListeners() {
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.addSessionStateListener(this);
            conversationFragmentCallback.addSessionEventsListener(this);
            conversationFragmentCallback.addVideoTrackListener(this);
        }
    }

    private void removeListeners() {
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.removeSessionStateListener(this);
            conversationFragmentCallback.removeSessionEventsListener(this);
            conversationFragmentCallback.removeVideoTrackListener(this);
        }
    }

    @Override
    protected void actionButtonsEnabled(boolean enabled) {
        super.actionButtonsEnabled(enabled);
        cameraToggle.setEnabled(enabled);
        cameraToggle.setActivated(enabled);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        Log.i(TAG, "initViews");
        opponentViewHolders = new SparseArray<>(opponents.size());

        localVideoView = (QBRTCSurfaceView) view.findViewById(R.id.local_video_view);
        flLocalVideoView = (FrameLayout) view.findViewById(R.id.fl_local_video_view);
        reconnectionProgress = view.findViewById(R.id.reconnection_progress);

        initCorrectSizeForLocalView();
        localVideoView.setZOrderMediaOverlay(true);

        remoteFullScreenVideoView = (QBRTCSurfaceView) view.findViewById(R.id.remote_video_view);
        remoteFullScreenVideoView.setOnClickListener(localViewOnClickListener);

        if (!isPeerToPeerCall) {
            recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);

            recyclerView.addItemDecoration(new DividerItemDecoration(requireActivity(), R.dimen.grid_item_divider));
            recyclerView.setHasFixedSize(true);
            final int columnsCount = defineColumnsCount();
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);

            recyclerView.setItemAnimator(null);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setGrid(columnsCount);
                    recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        connectionStatusLocal = (TextView) view.findViewById(R.id.username_full_view);

        cameraToggle = (ToggleButton) view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);
        cameraToggle.setChecked(SharedPrefsHelper.getInstance().get(CAMERA_ENABLED, true));
        toggleCamera(cameraToggle.isChecked());
        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        isCurrentCameraFront = SharedPrefsHelper.getInstance().get(IS_CURRENT_CAMERA_FRONT, true);

        if (conversationFragmentCallback.isCameraFront() != isCurrentCameraFront) {
            switchCamera(null);
        }

        actionButtonsEnabled(false);
        restoreSession();
    }

    private void restoreSession() {
        Log.d(TAG, "restoreSession ");
        if (conversationFragmentCallback != null && !conversationFragmentCallback.isCallState()) {
            return;
        }
        onCallStarted();
        Map<Integer, QBRTCVideoTrack> videoTrackMap = conversationFragmentCallback.getVideoTrackMap();
        if (!videoTrackMap.isEmpty()) {
            final Iterator<Map.Entry<Integer, QBRTCVideoTrack>> entryIterator = videoTrackMap.entrySet().iterator();
            while (entryIterator.hasNext()) {
                final Map.Entry<Integer, QBRTCVideoTrack> entry = entryIterator.next();
                Integer userID = entry.getKey();
                QBRTCVideoTrack videoTrack = entry.getValue();
                Log.d(TAG, "Checking Ability to Restore Session for User:" + userID);

                if (userID.equals(currentUser.getId())) {
                    Log.d(TAG, "Execute Restore Session for User: " + userID);
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onLocalVideoTrackReceive(null, videoTrack);
                        }
                    }, LOCAL_TRACK_INITIALIZE_DELAY);
                } else if (conversationFragmentCallback.getPeerChannel(userID) != QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED) {
                    Log.d(TAG, "Execute Restore Session for User: " + userID);
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedToUser(null, userID);
                            onRemoteVideoTrackReceive(null, videoTrack, userID);
                        }
                    }, LOCAL_TRACK_INITIALIZE_DELAY);
                } else {
                    entryIterator.remove();
                }
            }
        }
    }

    private void initCorrectSizeForLocalView() {
        ViewGroup.LayoutParams params = localVideoView.getLayoutParams();
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();

        int screenWidthPx = displaymetrics.widthPixels;
        Log.d(TAG, "screenWidthPx " + screenWidthPx);
        params.width = (int) (screenWidthPx * 0.3);
        params.height = (params.width / 2) * 3;
        localVideoView.setLayoutParams(params);
    }

    private void setGrid(int columnsCount) {
        int gridWidth = parentView.getMeasuredWidth();
        Log.i(TAG, "onGlobalLayout : gridWidth= " + gridWidth + "; columnsCount= " + columnsCount);
        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
        int cellSizeWidth = defineSize(gridWidth, columnsCount, itemMargin);
        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSizeWidth);
        opponents.remove(0);
        opponentsAdapter = new OpponentsFromCallAdapter(getContext(), this, opponents, cellSizeWidth,
                (int) getResources().getDimension(R.dimen.item_height));
        opponentsAdapter.setAdapterListener(this);
        recyclerView.setAdapter(opponentsAdapter);
    }

    private int defineSize(int measuredWidth, int columnsCount, float padding) {
        return measuredWidth / columnsCount - (int) (padding * 2) - RECYCLE_VIEW_PADDING;
    }

    private int defineColumnsCount() {
        return opponents.size() - 1;
    }

    private void releaseViewHolders() {
        opponentViewHolders.clear();
    }

    private void releaseViews() {
        if (conversationFragmentCallback.getCurrentSessionState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_CLOSED) {
            Map<Integer, QBRTCVideoTrack> videoTrackMap = ((CallActivity) requireActivity()).getVideoTrackMap();
            for (QBRTCVideoTrack item : videoTrackMap.values()) {
                VideoSink renderer = item.getRenderer();
                item.removeRenderer(renderer);
            }
        }
        if (localVideoView != null) {
            localVideoView.release();
        }
        if (remoteFullScreenVideoView != null) {
            remoteFullScreenVideoView.release();
        }
        remoteFullScreenVideoView = null;
        if (!isPeerToPeerCall) {
            releaseOpponentsViews();
        }
    }

    @Override
    public void onCallStopped() {
        super.onCallStopped();
        CallService.stop(getActivity());
        Log.i(TAG, "onCallStopped");
    }

    protected void initButtonsListener() {
        super.initButtonsListener();

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefsHelper.getInstance().save(CAMERA_ENABLED, isChecked);
                toggleCamera(isChecked);
            }
        });
    }

    private void switchCamera(final MenuItem item) {
        cameraToggle.setEnabled(false);
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.onSwitchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
                @Override
                public void onCameraSwitchDone(boolean b) {
                    isCurrentCameraFront = b;
                    SharedPrefsHelper.getInstance().save(IS_CURRENT_CAMERA_FRONT, b);
                    if (item != null) {
                        updateSwitchCameraIcon(item);
                    } else {
                        optionsMenu.findItem(R.id.camera_switch).setIcon(R.drawable.ic_camera_rear);
                    }
                    toggleCameraInternal();
                }

                @Override
                public void onCameraSwitchError(String s) {
                    Log.d(TAG, "Camera Switch Error " + s);
                    cameraToggle.setEnabled(true);
                }
            });
        }
    }

    private void updateSwitchCameraIcon(final MenuItem item) {
        if (conversationFragmentCallback.isCameraFront()) {
            Log.d(TAG, "CameraFront now!");
            item.setIcon(R.drawable.ic_camera_front);
        } else {
            Log.d(TAG, "CameraRear now!");
            item.setIcon(R.drawable.ic_camera_rear);
        }
    }

    private void toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!");
        if (remoteFullScreenVideoView == null) {
            return;
        }
        QBRTCSurfaceView surfaceViewRenderer = isLocalVideoFullScreen ? remoteFullScreenVideoView : localVideoView;

        updateVideoView(surfaceViewRenderer);
        toggleCamera(true);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        if (conversationFragmentCallback != null && conversationFragmentCallback.isMediaStreamManagerExist()) {
            conversationFragmentCallback.onSetVideoEnabled(isNeedEnableCam);
        }
        if (connectionEstablished && !cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        isLocalVideoFullScreen = true;

        if (localVideoTrack != null) {
            fillVideoView(localVideoView, localVideoTrack, false);
        }
        isLocalVideoFullScreen = false;
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        if (userID == null) {
            return;
        }
        if (isPeerToPeerCall) {
            setDuringCallActionBar();
            if (remoteFullScreenVideoView != null) {
                fillVideoView(userID,remoteFullScreenVideoView, videoTrack);
                updateVideoView(remoteFullScreenVideoView);
            }
        } else {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRemoteViewMultiCall(userID, videoTrack);
                }
            }, LOCAL_TRACK_INITIALIZE_DELAY);
        }
    }

    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);
    }

    @Override
    public void onItemClick(int position) {
        int userId = opponentsAdapter.getItem(position);
        Log.d(TAG, "USer onItemClick= " + userId);

        QBRTCTypes.QBRTCConnectionState connectionState = conversationFragmentCallback.getPeerChannel(userId);
        HashMap<Integer, QBRTCVideoTrack> videoTrackMap = conversationFragmentCallback.getVideoTrackMap();
        OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);

        boolean isNotExistVideoTrack = videoTrackMap != null && !videoTrackMap.containsKey(userId);
        boolean isConnectionStateClosed = connectionState != null &&
                connectionState.ordinal() == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal();

        if (isNotExistVideoTrack || isConnectionStateClosed || holder == null) {
            return;
        }

        replaceUsersInAdapter(position);
        updateViewHolders(position);
        swapUsersFullscreenToPreview(holder, userId);
    }

    private void replaceUsersInAdapter(int position) {
        for (QBUser user : allOpponents) {
            if (user.getId() == userIdFullScreen) {
                opponentsAdapter.replaceUsers(position, user);
                break;
            }
        }
    }

    private void updateViewHolders(int position) {
        View childView = recyclerView.getChildAt(position);
        OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
        opponentViewHolders.put(position, childViewHolder);
    }

    private void swapUsersFullscreenToPreview(OpponentsFromCallAdapter.ViewHolder holder, int userId) {
        QBRTCVideoTrack opponentVideoTrack = conversationFragmentCallback.getVideoTrackMap().get(userId);
        QBRTCVideoTrack mainVideoTrack = conversationFragmentCallback.getVideoTrackMap().get(userIdFullScreen);

        QBRTCSurfaceView remoteVideoView = holder.getOpponentView();

        if (opponentVideoTrack != null) {
            fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
            QBUser userFullScreen = dbManager.getUserById(userIdFullScreen);
            connectionStatusLocal.setText(userFullScreen.getFullName());
        }

        if (mainVideoTrack != null) {
            fillVideoView( remoteVideoView, mainVideoTrack,true);
        } else {
            holder.getOpponentView().setBackgroundColor(Color.BLACK);
            remoteFullScreenVideoView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void setRemoteViewMultiCall(int userId, QBRTCVideoTrack videoTrack) {
        final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userId);

        if (itemHolder == null || userIdFullScreen == userId) {
            if (remoteFullScreenVideoView != null) {
                fillVideoView(userId, remoteFullScreenVideoView, videoTrack);
                updateVideoView(remoteFullScreenVideoView);
                QBRTCTypes.QBRTCReconnectionState state = conversationFragmentCallback.getState(userId);
                if(state == QB_RTC_RECONNECTION_STATE_RECONNECTING){
                    reconnectionProgress.setVisibility(View.VISIBLE);
                }
            }
            setDuringCallActionBar();
        } else {
            final QBRTCSurfaceView remoteVideoView = itemHolder.getOpponentView();
            remoteVideoView.setZOrderMediaOverlay(true);
            updateVideoView(remoteVideoView);
            fillVideoView(remoteVideoView, videoTrack, true);
            setRecyclerViewVisibleState();
            QBRTCTypes.QBRTCReconnectionState state = conversationFragmentCallback.getState(userId);
            if(state == QB_RTC_RECONNECTION_STATE_RECONNECTING){
                itemHolder.showProgressBar();
                itemHolder.setStatus(getString(R.string.reconnecting_status));
            }
        }
    }

    private void setRecyclerViewVisibleState() {
        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
        params.height = (int) getResources().getDimension(R.dimen.item_height);
        recyclerView.setLayoutParams(params);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            Log.d(TAG, "holder not found in cache");
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.append(userID, holder);
            }
        }
        return holder;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        Log.d(TAG, "findHolder for " + userID);
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            if (userID.equals(childViewHolder.getUserId())) {
                return childViewHolder;
            }
        }
        return null;
    }

    private void releaseOpponentsViews() {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        Log.d(TAG, " releaseOpponentsViews for  " + childCount + " views");
        for (int i = 0; i < childCount; i++) {
            View childView = layoutManager.getChildAt(i);
            Log.d(TAG, " release View for  " + i + ", " + childView);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            childViewHolder.getOpponentView().release();
        }
    }

    private void fillVideoView(QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack,
                               boolean remoteRenderer) {
        videoTrack.removeRenderer(videoTrack.getRenderer());
        if (videoView != null) {
            videoTrack.addRenderer(videoView);
        }
        if (!remoteRenderer) {
            updateVideoView(videoView);
        }
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void updateVideoView(SurfaceViewRenderer videoView) {
        RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;
        Log.i(TAG, "updateVideoView - scalingType = " + scalingType);
        if (videoView != null) {
            videoView.setScalingType(scalingType);
            videoView.setMirror(false);
            videoView.requestLayout();
        }
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, QBRTCSurfaceView videoView, QBRTCVideoTrack videoTrack) {
            userIdFullScreen = userId;
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        if (isPeerToPeerCall) {
            return;
        }

        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }

        holder.setStatus(status);
    }

    private void updateNameForOpponent(int userId, String newUserName) {
        if (isPeerToPeerCall) {
            actionBar.setSubtitle(getString(R.string.opponent, newUserName));
        } else {
            OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
            if (holder == null) {
                Log.d(TAG, "holder == null");
                return;
            }

            Log.d(TAG, "holder != null");
            holder.setUserName(newUserName);
        }
    }

    private void setProgressBarForOpponentGone(int userId) {
        if (isPeerToPeerCall) {
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }

        holder.hideProgressBar();
    }

    private void setBackgroundOpponentView(final Integer userId) {
        if (userId != userIdFullScreen) {
            final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
            if (holder == null) {
                return;
            }
            holder.getOpponentView().setBackgroundColor(Color.BLACK);
        } else {
            remoteFullScreenVideoView.setBackgroundColor(Color.BLACK);
        }
    }

    @Override
    public void onStateChanged(QBRTCSession qbrtcSession, BaseSession.QBRTCSessionState qbrtcSessionState) {
        // empty
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        connectionEstablished = true;
        setStatusForOpponent(userId, getString(R.string.text_status_connected));
        setProgressBarForOpponentGone(userId);
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_closed));
        if (!isPeerToPeerCall) {
            Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= " + userId);
            setBackgroundOpponentView(userId);
            setProgressBarForOpponentGone(userId);
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        if (getContext() != null) {
            setStatusForOpponent(integer, getContext().getString(R.string.text_status_disconnected));
        }
    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
        setProgressBarForOpponentGone(userId);
        setStatusForOpponent(userId, getString(R.string.text_status_no_answer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.text_status_rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
        setStatusForOpponent(userId, getString(R.string.text_status_hang_up));
        Log.d(TAG, "onReceiveHangUpFromUser userId= " + userId);
        if (!isPeerToPeerCall) {
            if (userId == userIdFullScreen) {
                Log.d(TAG, "setAnotherUserToFullScreen call userId= " + userId);
                setAnotherUserToFullScreen();
            }
        }
    }

    @Override
    public void onChangeReconnectionState(QBRTCSession qbrtcSession, Integer userID, QBRTCTypes.QBRTCReconnectionState qbrtcReconnectionState) {
         OpponentsFromCallAdapter.ViewHolder holder = null;
        if(!isPeerToPeerCall){
            holder = getViewHolderForOpponent(userID);
        }
        switch (qbrtcReconnectionState) {
            case QB_RTC_RECONNECTION_STATE_RECONNECTING:
                if (userIdFullScreen == userID) {
                    reconnectionProgress.setVisibility(View.VISIBLE);
                    return;
                }
                if (holder == null) {
                    return;
                }
                holder.showProgressBar();
                holder.setStatus(getString(R.string.reconnecting_status));
                break;
            case QB_RTC_RECONNECTION_STATE_RECONNECTED:
            case QB_RTC_RECONNECTION_STATE_FAILED:
                if (userIdFullScreen == userID) {
                    reconnectionProgress.setVisibility(View.GONE);
                    return;
                }
                if (holder == null) {
                    return;
                }
                holder.hideProgressBar();
                break;
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {
        // empty
    }

    private void setAnotherUserToFullScreen() {
        if (opponentsAdapter.getOpponents().isEmpty()) {
            return;
        }

        for (int index = 0; index <= opponents.size() - 1; index++) {
            QBRTCVideoTrack videoTrack = conversationFragmentCallback.getVideoTrack(opponents.get(index).getId());
            if (videoTrack != null) {
                QBUser userFullScreen = dbManager.getUserById(userIdFullScreen);

                OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(opponents.get(index).getId());
                if (itemHolder != null) {
                    itemHolder.setUserId(userIdFullScreen);
                    itemHolder.setUserName(userFullScreen.getFullName());
                    itemHolder.setStatus(getString(R.string.text_status_closed));
                    itemHolder.getOpponentView().release();
                }

                if (remoteFullScreenVideoView != null) {
                    fillVideoView(opponents.get(index).getId(), remoteFullScreenVideoView, videoTrack);
                    connectionStatusLocal.setText(opponents.get(index).getFullName());
                    Log.d(TAG, "fullscreen enabled");
                }
                return;
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
        updateSwitchCameraIcon(menu.findItem(R.id.camera_switch));
        optionsMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera_switch:
                Log.d("Conversation", "camera_switch");
                switchCamera(item);
                return true;
            case R.id.screen_share:
                startScreenSharing();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startScreenSharing() {
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.onStartScreenSharing();
        }
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
        updateAllOpponentsList(newUsers);
        Log.d(TAG, "updateOpponentsList(), newUsers = " + newUsers);
        runUpdateUsersNames(newUsers);
    }

    private void updateAllOpponentsList(ArrayList<QBUser> newUsers) {
        for (int i = 0; i < allOpponents.size(); i++) {
            for (QBUser updatedUser : newUsers) {
                if (updatedUser.equals(allOpponents.get(i))) {
                    allOpponents.set(i, updatedUser);
                }
            }
        }
    }

    @Override
    public void onCallTimeUpdate(String time) {
        timerCallText.setText(time);
    }

    private void runUpdateUsersNames(final ArrayList<QBUser> newUsers) {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (QBUser user : newUsers) {
                    Log.d(TAG, "runUpdateUsersNames. foreach, user = " + user.getFullName());
                    updateNameForOpponent(user.getId(), user.getFullName());
                }
            }
        }, UPDATING_USERS_DELAY);
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {
        private final int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }
    }

    class LocalViewOnClickListener implements View.OnClickListener {
        private long lastFullScreenClickTime = 0L;

        @Override
        public void onClick(View v) {
            if ((SystemClock.uptimeMillis() - lastFullScreenClickTime) < FULL_SCREEN_CLICK_DELAY) {
                return;
            }
            lastFullScreenClickTime = SystemClock.uptimeMillis();

            if (connectionEstablished) {
                setFullScreenOnOff();
            }
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
            flLocalVideoView.setVisibility(View.INVISIBLE);
            actionVideoButtonsLayout.setVisibility(View.GONE);
            if (!isPeerToPeerCall) {
                shiftBottomListOpponents();
            }
        }

        private void showToolBarAndButtons() {
            actionBar.show();
            flLocalVideoView.setVisibility(View.VISIBLE);
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