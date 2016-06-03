package com.quickblox.sample.groupchatwebrtc.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.groupchatwebrtc.utils.CameraUtils;
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
public class VideoConversationFragment extends BaseConversationFragment implements Serializable, QBRTCClientVideoTracksCallbacks,
        QBRTCSessionConnectionCallbacks, CallActivity.QBRTCSessionUserCallback, OpponentsFromCallAdapter.OnAdapterEventListener {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;

    private String TAG = VideoConversationFragment.class.getSimpleName();

    private ToggleButton cameraToggle;
    private View view;
    private boolean isVideoCall = false;
    private LinearLayout actionVideoButtonsLayout;
    private RTCGLVideoView localVideoView;
    private CameraState cameraState = CameraState.NONE;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private List<OpponentsFromCallAdapter.ViewHolder> viewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private TextView connectionStatusLocal;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isRemoteShown;
    private boolean headsetPlugged;

    private int amountOpponents;
    private int userIDFullScreen;
    private ArrayList<QBUser> allOponents;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    protected void configureOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setBackgroundColor(getResources().getColor(R.color.grey_transparent_50));
        allOpponentsTextView.setTextColor(getResources().getColor(R.color.white));
        ringingTextView.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    protected void configureActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void configureToolbar() {
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.black_transparent_50));
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));
        toolbar.setSubtitleTextColor(getResources().getColor(R.color.white));
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_video_conversation;
    }

    @Override
    protected void initFields() {
        super.initFields();
        localViewOnClickListener = new LocalViewOnClickListener();
        amountOpponents = opponents.size();
        allOponents = new ArrayList<>(opponents.size());
        allOponents.addAll(opponents);

        timerChronometer = (Chronometer) getActivity().findViewById(R.id.timer_chronometer_action_bar);

        String callerName = dbManager.getUserNameById(currentSession.getCallerID());

        isPeerToPeerCall = opponents.size() == 1;
        isVideoCall = (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType()));
    }

    public void setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(currentUser.getFullName());
        if (isPeerToPeerCall) {
            actionBar.setSubtitle(getString(R.string.opponent, opponents.get(0).getFullName()));
        } else {
            actionBar.setSubtitle(getString(R.string.opponents, amountOpponents));
        }

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

    @Override
    protected void actionButtonsEnabled(boolean enability) {
        super.actionButtonsEnabled(enability);
        cameraToggle.setEnabled(enability);
//Todo add switch camera
        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
    }

    @Override
    public void onStart() {
        super.onStart();

        initVideoTrackSListener();

        conversationFragmentCallbackListener.addTCClientConnectionCallback(this);
        conversationFragmentCallbackListener.addRTCSessionUserCallback(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);

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

        cameraToggle = (ToggleButton) view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

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
        opponentsAdapter.setAdapterListener(VideoConversationFragment.this);
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
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        removeVideoTrackSListener();
        conversationFragmentCallbackListener.removeRTCClientConnectionCallback(this);
        conversationFragmentCallbackListener.removeRTCSessionUserCallback(this);
    }

    protected void initButtonsListener() {
        super.initButtonsListener();

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                toggleCamera(isChecked);
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
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

        if (isPeerToPeerCall) {
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDuringCallActionBar();
                    if (localVideoView == null) {
                        localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();
                    }
                    setLocalVideoView(userID, videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);

        } else {
            setRemoteViewMultiCall(userID, videoTrack);
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
                    if (viewHolders != null)
                        Log.d(TAG, "USer childViewHolder.getUserId OnBindLastViewHolder= " + viewHolders.get(position).getUserId());
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
        Log.d(TAG, "USer onItemClick= " + userId);
        if (!getVideoTrackMap().containsKey(userId) ||
                currentSession.getPeerChannel(userId).getState().ordinal() == QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal()) {
            return;
        }

        replaceUsersInAdapter(position);

        updateViewHolders(position);

        swapUsersFullscreenToPreview(userId);
    }

    private void replaceUsersInAdapter(int position) {
        for (QBUser qbUser : allOponents) {
            if (qbUser.getId() == userIDFullScreen) {
                opponentsAdapter.replaceUsers(position, qbUser);
                break;
            }
        }
    }

    private void updateViewHolders(int position) {
        View childView = recyclerView.getChildAt(position);
        OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
        viewHolders.set(position, childViewHolder);
    }

    @SuppressWarnings("ConstantConditions")
    private void swapUsersFullscreenToPreview(int userId) {
        QBRTCVideoTrack userVideoTrackPreview = videoTrackMap.get(userId);
        QBRTCVideoTrack videoTrackFullScreen = getVideoTrackMap().get(userIDFullScreen);
        userVideoTrackPreview.removeRenderer(userVideoTrackPreview.getRenderer());

        videoTrackFullScreen.removeRenderer(videoTrackFullScreen.getRenderer());

        RTCGLVideoView remoteVideoView = findHolder(userId).getOpponentView();

        fillVideoView(0, remoteVideoView, videoTrackFullScreen);
        Log.d(TAG, "remoteVideoView enabled");

        fillVideoView(userId, localVideoView, userVideoTrackPreview);
        Log.d(TAG, "fullscreen enabled");
    }

    private void setLocalVideoView(int userId, QBRTCVideoTrack videoTrack) {
        RTCGLVideoView.RendererConfig config = setRTCCameraMirrorConfig(true);
        config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
        config = setRTCCameraMirrorConfig(false);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.MAIN, config);
        fillVideoView(userId, localVideoView, videoTrack);
    }

    private void setRemoteViewMultiCall(int userID, QBRTCVideoTrack videoTrack) {
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
                fillVideoView(0, remoteVideoView, videoTrack);
            }

            if (!isRemoteShown) {
                isRemoteShown = true;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
                        setDuringCallActionBar();
                        recyclerView.setVisibility(View.VISIBLE);
                        setOpponentsVisibility(View.VISIBLE);
                    }
                });
                setLocalVideoView(userID, videoTrack);
            }
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
//        if (recyclerView == null) {
//            return null;
//        }

        if (viewHolders != null) {
            return viewHolders;
        }
        int childCount = recyclerView.getChildCount();
        viewHolders = new ArrayList<>();
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            viewHolders.add(childViewHolder);
        }
        return viewHolders;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        if (viewHolders == null) {
            return null;
        }
        for (OpponentsFromCallAdapter.ViewHolder childViewHolder : viewHolders) {
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

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, RTCGLVideoView videoView, QBRTCVideoTrack videoTrack) {
        if (userId != 0) {
            userIDFullScreen = userId;
        }
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        if (isPeerToPeerCall) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectionStatusLocal.setText(status);
                }
            });
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }
        runOnUiThread(new Runnable() {
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

    private void setBackgroundOpponentView(final Integer userId) {
        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }
        Log.d(TAG, "setBackgroundOpponentView userIDFullScreen= " + userIDFullScreen);
        Log.d(TAG, "setBackgroundOpponentView userId= " + userId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (userId != userIDFullScreen) {
                    holder.getOpponentView().setBackgroundColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_connected));
        setProgressBarForOpponentGone(userId);
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_closed));
        if (!isPeerToPeerCall) {
            setBackgroundOpponentView(userId);
        }
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }
    //////////////////////////////////   end     //////////////////////////////////////////


    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////

    @Override
    public void onSessionClosed() {
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
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_hang_up));
        if (!isPeerToPeerCall) {
            if (userId == userIDFullScreen) {
                setAnotherUserToFullScreen();
            }
        }
    }
    //////////////////////////////////   end     //////////////////////////////////////////

    @SuppressWarnings("ConstantConditions")
    private void setAnotherUserToFullScreen() {
        if (opponentsAdapter.getOpponents().size() == 0) {
            return;
        }
        final int userId = opponentsAdapter.getItem(0);
        QBRTCVideoTrack userVideoTrackPreview = videoTrackMap.get(userId);
        if (userVideoTrackPreview == null) {
            return;
        }
        userVideoTrackPreview.removeRenderer(userVideoTrackPreview.getRenderer());
        fillVideoView(userId, localVideoView, userVideoTrackPreview);
        Log.d(TAG, "fullscreen enabled");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(userId);
                opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
            }
        });
    }

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
                conversationFragmentCallbackListener.onSwitchAudio();
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

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
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


