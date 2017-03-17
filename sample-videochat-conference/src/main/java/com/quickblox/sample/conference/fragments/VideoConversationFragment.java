package com.quickblox.sample.conference.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.view.QBConferenceSurfaceView;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.adapters.OpponentsAdapter;
import com.quickblox.sample.conference.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionStateCallback;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.CameraVideoCapturer;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;


/**
 * QuickBlox team
 */
public class VideoConversationFragment extends BaseConversationFragment implements Serializable, QBRTCClientVideoTracksCallbacks<ConferenceSession>,
        QBRTCSessionStateCallback<ConferenceSession>, QBRTCSessionEventsCallback, OpponentsFromCallAdapter.OnAdapterEventListener {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;
    private static final int RECYCLE_VIEW_PADDING = 2;
    private static final long UPDATING_USERS_DELAY = 2000;
    private static final long FULL_SCREEN_CLICK_DELAY = 1000;
    private static final int REQUEST_CODE_ATTACHMENT = 100;

    private String TAG = VideoConversationFragment.class.getSimpleName();

    private ToggleButton cameraToggle;
    private View view;
    private LinearLayout actionVideoButtonsLayout;
    private QBConferenceSurfaceView remoteFullScreenVideoView;
    private QBConferenceSurfaceView localVideoView;
    private CameraState cameraState = CameraState.DISABLED_FROM_USER;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private TextView connectionStatusLocal;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isRemoteShown;

    private int amountOpponents;
    private int userIDFullScreen;
    private List<QBUser> allOpponents;
    private boolean connectionEstablished;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;
    private boolean isLocalVideoFullScreen;
    private QBUser localUser;
    private GridLayoutManager gridLayoutManager;
    private SpanSizeLookupImpl spanSizeLookup;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }

    @Override
    protected void configureOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey_transparent_50));
        allOpponentsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        ringingTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
    }

    @Override
    protected void configureActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar();
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
    int getFragmentLayout() {
        return R.layout.fragment_video_conversation;
    }

    @Override
    protected void initFields() {
        super.initFields();
        localUser = new QBUser(currentSession.getCurrentUserID());
        localViewOnClickListener = new LocalViewOnClickListener();
        amountOpponents = opponents.size();
        allOpponents = Collections.synchronizedList(new ArrayList<QBUser>(opponents.size()));
        allOpponents.addAll(opponents);

        isPeerToPeerCall = false;
    }

    public void setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(currentUser.getFullName());
    }

    private void updateActionBar(int amountOpponents) {
        actionBar.setSubtitle(getString(R.string.opponents, String.valueOf(amountOpponents)));
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
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        cameraToggle.setEnabled(inability);
        // inactivate toggle buttons
        cameraToggle.setActivated(inability);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if (!allCallbacksInit) {
            conversationFragmentCallbackListener.addTCClientConnectionCallback(this);
            initVideoTrackSListener();
            allCallbacksInit = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);
    }

    class GridManager extends GridLayoutManager {

        public GridManager(Context context, int spanCount) {
            super(context, spanCount);
        }

        @Override
        public void onItemsAdded(RecyclerView recyclerView, int positionStart, int itemCount) {
            super.onItemsAdded(recyclerView, positionStart, itemCount);
            Log.d("TEMPOS", "onItemsAdded positionStart= " + positionStart);
        }

        @Override
        public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
            super.onItemsRemoved(recyclerView, positionStart, itemCount);
            Log.d("TEMPOS", "onItemsRemoved positionStart= " + positionStart);
            Log.d("TEMPOS", "onItemsRemoved itemCount= " + itemCount);
//            if(itemCount > 0){
//                Log.d("TEMPOS", "onItemsRemoved opponentsAdapter.getItem(0)= " + opponentsAdapter.getItem(0));
//                OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(opponentsAdapter.getItem(0));
//                itemHolder.itemView.requestLayout();
//            }
        }

        @Override
        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount,
                                   Object payload) {
            super.onItemsUpdated(recyclerView, positionStart, itemCount, payload);
            Log.d("TEMPOS", "onItemsUpdated positionStart= " + positionStart);
        }

        @Override
        public void onItemsChanged(RecyclerView recyclerView) {
           super.onItemsChanged(recyclerView);
            Log.d("TEMPOS", "onItemsChanged");
        }

        @Override
        public void onLayoutCompleted(RecyclerView.State state) {
            super.onLayoutCompleted(state);
        }
    }

    class SpanSizeLookupImpl extends GridManager.SpanSizeLookup {


        @Override
        public int getSpanSize(int position) {
            int size = opponentsAdapter.getItemCount();
            if(size % 4 == 0) {
                return 3;
            }

            if(size % 4 == 1) {
//                          last position
                if (position == opponentsAdapter.getItemCount() - 1) {
                    Log.d("TEMPOS", "return 12 opponentsAdapter position= " + position);

                    return 12;
                } else {
                    return 3;
                }
            }

            if(size % 4 == 2) {

                if(position == opponentsAdapter.getItemCount() - 1 || position == opponentsAdapter.getItemCount() - 2) {
                    return 6;
                } else {
                    return 3;
                }
            }

            if(size % 4 == 3) {
                if (position == opponentsAdapter.getItemCount() - 1 || position == opponentsAdapter.getItemCount() - 2 || position == opponentsAdapter.getItemCount() - 3) {
                    return 4;
                } else {
                    return 3;
                }
            }

//                    never gonna happen
            return 4;
        }
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        Log.i(TAG, "initViews");
        opponentViewHolders = new SparseArray<>(opponents.size());
        isRemoteShown = false;
        isCurrentCameraFront = true;
        localVideoView = (QBConferenceSurfaceView) view.findViewById(R.id.local_video_view);
        initCorrectSizeForLocalView();
        localVideoView.setZOrderMediaOverlay(true);

        remoteFullScreenVideoView = (QBConferenceSurfaceView) view.findViewById(R.id.remote_video_view);
        remoteFullScreenVideoView.setOnClickListener(localViewOnClickListener);

        if (!isPeerToPeerCall) {
            recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);

            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
            recyclerView.setHasFixedSize(true);
            final int columnsCount = defineColumnsCount();
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(getActivity(), HORIZONTAL, false);
            gridLayoutManager = new GridManager(getActivity(), 12);
            gridLayoutManager.setReverseLayout(true);
            spanSizeLookup = new SpanSizeLookupImpl();
            spanSizeLookup.setSpanIndexCacheEnabled(false);
            gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
            recyclerView.setLayoutManager(gridLayoutManager);

//          for correct removing item in adapter
            recyclerView.setItemAnimator(null);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setGrid(columnsCount);
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
        connectionStatusLocal = (TextView) view.findViewById(R.id.connectionStatusLocal);

        cameraToggle = (ToggleButton) view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        actionButtonsEnabled(false);
        restoreSession();
    }

    private void restoreSession() {
        Log.d(TAG, "restoreSession ");
        if (currentSession.getState() != BaseSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
            return;
        }
        onCallStarted();
        Map<Integer, QBRTCVideoTrack> videoTrackMap = getVideoTrackMap();
        if (!videoTrackMap.isEmpty()) {
            for (final Iterator<Map.Entry<Integer, QBRTCVideoTrack>> entryIterator
                 = videoTrackMap.entrySet().iterator(); entryIterator.hasNext();){
                final Map.Entry<Integer, QBRTCVideoTrack> entry = entryIterator.next();
                Log.d(TAG, "check ability to restoreSession for user:"+entry.getKey());
                //if connection with peer wasn't closed do restore it otherwise remove from collection
                if (currentSession.getPeerChannel(entry.getKey()).getState()!=
                        QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED){
                    Log.d(TAG, "execute restoreSession for user:"+entry.getKey());
                    mainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                            onConnectedToUser(currentSession, entry.getKey());
                            onRemoteVideoTrackReceive(currentSession, entry.getValue(), entry.getKey());
                        }
                        }, LOCAL_TRACk_INITIALIZE_DELAY);
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
//        int gridWidth = view.getMeasuredWidth();
//        Log.i(TAG, "onGlobalLayout : gridWidth=" + gridWidth + " columnsCount= " + columnsCount);
//        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
//        int cellSizeWidth = defineSize(gridWidth, columnsCount, itemMargin);
//        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSizeWidth);
        ArrayList<QBUser> qbUsers = new ArrayList<>();
        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, qbUsers,
                (int) getResources().getDimension(R.dimen.item_width),
                (int) getResources().getDimension(R.dimen.item_height));
        opponentsAdapter.setAdapterListener(VideoConversationFragment.this);
        recyclerView.setAdapter(opponentsAdapter);
    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }

    private int defineSize(int measuredWidth, int columnsCount, float padding) {
        return measuredWidth / columnsCount - (int) (padding * 2) - RECYCLE_VIEW_PADDING;
    }

    private int defineColumnsCount() {
//        return opponents.size() - 1;
        return opponents.size();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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
        Log.d(TAG, "onStop");
        if (connectionEstablished) {
            allCallbacksInit = false;
        } else {
            Log.d(TAG, "We are in dialing process yet!");
        }
    }

    private void removeVideoTrackRenderers() {
        Log.d(TAG, "removeVideoTrackRenderers");
        Log.d(TAG, "remove opponents video Tracks");
        Map<Integer, QBRTCVideoTrack> videoTrackMap = getVideoTrackMap();
        for (QBRTCVideoTrack videoTrack : videoTrackMap.values()) {
            if (videoTrack.getRenderer() != null) {
                Log.d(TAG, "remove opponent video Tracks");
                videoTrack.removeRenderer(videoTrack.getRenderer());
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        releaseViewHolders();
        removeConnectionStateListeners();
        removeVideoTrackSListener();
        removeVideoTrackRenderers();
        releaseViews();

//        senderHandler.cancel(true);
//        senderHandler = null;
    }

    private void releaseViewHolders() {
        opponentViewHolders.clear();
    }

    private void removeConnectionStateListeners(){
        conversationFragmentCallbackListener.removeRTCClientConnectionCallback(this);
    }

    private void releaseViews() {
        if (localVideoView != null){
            localVideoView.release();
        }
        if (remoteFullScreenVideoView != null) {
            remoteFullScreenVideoView.release();
        }
        remoteFullScreenVideoView = null;
        if (!isPeerToPeerCall){
           releseOpponentsViews();
        }
    }

    @Override
    public void onCallStopped() {
        super.onCallStopped();
        Log.i(TAG, "onCallStopped");
    }

    protected void initButtonsListener() {
        super.initButtonsListener();

        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (cameraState != CameraState.DISABLED_FROM_USER) {
                    toggleCamera(isChecked);
                }
            }
        });
    }

    private void switchCamera(final MenuItem item) {
        if (cameraState == CameraState.DISABLED_FROM_USER) {
            return;
        }
        cameraToggle.setEnabled(false);
        conversationFragmentCallbackListener.onSwitchCamera(new CameraVideoCapturer.CameraSwitchHandler() {
            @Override
            public void onCameraSwitchDone(boolean b) {
                Log.d(TAG, "camera switched, bool = " + b);
                isCurrentCameraFront = b;
                updateSwitchCameraIcon(item);
                toggleCameraInternal();
            }

            @Override
            public void onCameraSwitchError(String s) {
                Log.d(TAG, "camera switch error " + s);
                Toaster.shortToast(getString(R.string.camera_swicth_failed) + s);
                cameraToggle.setEnabled(true);
            }
        });
    }

    private void updateSwitchCameraIcon(final MenuItem item) {
        if (isCurrentCameraFront) {
            Log.d(TAG, "CameraFront now!");
            item.setIcon(R.drawable.ic_camera_front);
        } else {
            Log.d(TAG, "CameraRear now!");
            item.setIcon(R.drawable.ic_camera_rear);
        }
    }

    private void toggleCameraInternal() {
        Log.d(TAG, "Camera was switched!");
        updateVideoView(isLocalVideoFullScreen ? remoteFullScreenVideoView : localVideoView, isCurrentCameraFront);
        toggleCamera(true);
    }

    private void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            conversationFragmentCallbackListener.onSetVideoEnabled(isNeedEnableCam);
        }
        if (connectionEstablished && !cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////
    @Override
    public void onLocalVideoTrackReceive(ConferenceSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        isLocalVideoFullScreen = true;
        cameraState = CameraState.NONE;
        actionButtonsEnabled(true);

        if (remoteFullScreenVideoView != null) {
            fillVideoView(remoteFullScreenVideoView, localVideoTrack, false);
        }
    }

    private void fitScaleAdapter(){
        int itemCount = opponentsAdapter.getItemCount();

        if(itemCount <= 4) {
            if (localVideoTrack != null) {
                localVideoView.setVisibility(View.GONE);
                fillVideoView(remoteFullScreenVideoView, localVideoTrack, false);
                remoteFullScreenVideoView.setVisibility(View.VISIBLE);
                setRecyclerViewVisibleStateWrapContent();
            }
        } else if(itemCount <= 8) {
            if (localVideoTrack != null) {
                remoteFullScreenVideoView.setVisibility(View.GONE);
                localVideoView.setVisibility(View.VISIBLE);
                fillVideoView(localVideoView, localVideoTrack, false);
                setRecyclerViewVisibleStateBelow();
            }
        } else if (itemCount > 8) {
            Log.d(TAG, "replace LocalView to adapter");
        }
    }

    private void fitScaleAdapterTemp(){
        int itemCount = opponentsAdapter.getItemCount();

        if(itemCount <= 1) {
            if (localVideoTrack != null) {
                localVideoView.setVisibility(View.GONE);
                fillVideoView(remoteFullScreenVideoView, localVideoTrack, false);
                remoteFullScreenVideoView.setVisibility(View.VISIBLE);
            }
        } else if(itemCount <= 2) {
            if (localVideoTrack != null) {
                remoteFullScreenVideoView.setVisibility(View.GONE);
                localVideoView.setVisibility(View.VISIBLE);
                fillVideoView(localVideoView, localVideoTrack, false);
            }
        }
//        else if (itemCount > 2) {
//            Log.d(TAG, "replace LocalView to adapter");
//            localVideoView.setVisibility(View.GONE);
//            opponentsAdapter.add(localUser);
//            setRemoteViewOnPost(localUser.getId(), localVideoTrack);
//        }
        setRecyclerViewVisibleStateWrapContent();
    }

    private void setRemoteViewOnPost(final int userID, final QBRTCVideoTrack videoTrack) {
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRemoteViewMultiCall(userID, videoTrack);
            }
        }, LOCAL_TRACk_INITIALIZE_DELAY);
    }

    private void removeLocalViewToAdapter() {

        Log.d(TAG, "removeLocalViewToAdapter");
        if(opponentsAdapter.getOpponents().contains(localUser)){

            if(opponentsAdapter.getOpponents().size() == 3) {
                opponentsAdapter.removeOpponent(localUser);
                Log.d(TAG, "removeLocalViewToAdapter  opponentsAdapter.getOpponents().contains(localUser)");
                OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(localUser.getId());
                if (itemHolder != null) {
                    Log.d(TAG, "removeLocalViewToAdapter  opponentsAdapter.removeItem");
//                    opponentsAdapter.removeItem(itemHolder.getAdapterPosition());

                } else {
                    Log.d(TAG, "removeLocalViewToAdapter  itemHolder != null");
                }
            }
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(ConferenceSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

        setOpponentToAdapter(userID);

//        fitScaleAdapterTemp();
        fitScaleAdapter();
//        scheduler(); // for test

        isLocalVideoFullScreen = false;

            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRemoteViewMultiCall(userID, videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);

    }

    /////////////////////////////////////////    end    ////////////////////////////////////////////

    private void setOpponentToAdapter(Integer userID) {
            QBUser qbUser = getUserById(userID);
            if(qbUser !=null){
                opponentsAdapter.add(qbUser);
            } else {
                opponentsAdapter.add(new QBUser(userID));
            }
    }

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);

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
        for (QBUser qbUser : opponentsAdapter.getOpponents()) {
            if (qbUser.getId() == userIDFullScreen) {
                opponentsAdapter.replaceUsers(position, qbUser);
                break;
            }
        }
    }

    private void updateViewHolders(int position) {
        View childView = recyclerView.getChildAt(position);
        OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
        opponentViewHolders.put(position, childViewHolder);
    }

    @SuppressWarnings("ConstantConditions")
    private void swapUsersFullscreenToPreview(int userId) {
//      get opponentVideoTrack - opponent's video track from recyclerView
        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);

//      get mainVideoTrack - opponent's video track from full screen
        QBRTCVideoTrack mainVideoTrack = getVideoTrackMap().get(userIDFullScreen);

        QBConferenceSurfaceView remoteVideoView = findHolder(userId).getOpponentView();

        if (mainVideoTrack != null) {
            fillVideoView(0, remoteVideoView, mainVideoTrack);
            Log.d(TAG, "_remoteVideoView enabled");
        }
        if (opponentVideoTrack != null) {
            fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
            Log.d(TAG, "fullscreen enabled");
        }
    }


    private void setRemoteViewMultiCall(int userID, QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView");
        if(!isRemoteShown){
            isRemoteShown = true;
            setRecyclerViewVisibleStateWrapContent();
            setDuringCallActionBar();
        }
        updateActionBar(opponentsAdapter.getItemCount());
        final OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
        if (itemHolder == null) {
            Log.d(TAG, "itemHolder == null - true");
            return;
        }
        final QBConferenceSurfaceView remoteVideoView = itemHolder.getOpponentView();

        if (remoteVideoView != null) {
            remoteVideoView.setZOrderMediaOverlay(true);
            updateVideoView(remoteVideoView, false);
            Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");
            fillVideoView(userID, remoteVideoView, videoTrack, true);
        }
    }

    private QBUser getUserById(int userID) {
        for (QBUser qbUser : allOpponents) {
            if(qbUser.getId().equals(userID)){
                return qbUser;
            }
        }
        return null;
    }

    private void setRecyclerViewVisibleStateWrapContent() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
//        params.height = (int) getResources().getDimension(R.dimen.item_height);
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        recyclerView.setLayoutParams(params);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setRecyclerViewVisibleStateBelow() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
//        params.height = (int) getResources().getDimension(R.dimen.item_height);
//        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.addRule(RelativeLayout.BELOW, localVideoView.getId());
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
        Log.d(TAG, "findHolder for "+userID);
        int childCount = recyclerView.getChildCount();
        Log.d(TAG, " AMBRA findHolder for childCount= " + childCount);
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            Log.d("AMBRA", "childViewHolder.getUserId= " + childViewHolder.getUserId());
            if (userID.equals(childViewHolder.getUserId())) {
                Log.d("AMBRA", "return childViewHolder");
                return childViewHolder;
            }
        }
        return null;
    };

    private void releseOpponentsViews(){
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int childCount = layoutManager.getChildCount();
        Log.d(TAG, " releseOpponentsViews for  "+childCount + " views");
        for (int i = 0; i < childCount; i++) {
            View childView = layoutManager.getChildAt(i);
            Log.d(TAG, " relese View for  " + i +", "+childView);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
            childViewHolder.getOpponentView().release();
        }
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, QBConferenceSurfaceView videoView, QBRTCVideoTrack videoTrack,
                               boolean remoteRenderer) {
        videoTrack.removeRenderer(videoTrack.getRenderer());
        videoTrack.addRenderer(new VideoRenderer(videoView));
        if (userId != 0) {
            getVideoTrackMap().put(userId, videoTrack);
        }
        if (!remoteRenderer) {
            updateVideoView(videoView, isCurrentCameraFront);
        }
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(QBConferenceSurfaceView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
       fillVideoView(0, videoView, videoTrack, remoteRenderer);
    }

    /**
     * @param userId set userId if it from fullscreen videoTrack
     */
    private void fillVideoView(int userId, QBConferenceSurfaceView videoView, QBRTCVideoTrack videoTrack) {
        if (userId != 0) {
            userIDFullScreen = userId;
        }
        fillVideoView(userId, videoView, videoTrack, true);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror) {
        updateVideoView(surfaceViewRenderer, mirror, RendererCommon.ScalingType.SCALE_ASPECT_FILL);
    }

    protected void updateVideoView(SurfaceViewRenderer surfaceViewRenderer, boolean mirror, RendererCommon.ScalingType scalingType) {
        Log.i(TAG, "updateVideoView mirror:" + mirror + ", scalingType = " + scalingType);
        surfaceViewRenderer.setScalingType(scalingType);
        surfaceViewRenderer.setMirror(mirror);
        surfaceViewRenderer.requestLayout();
    }

    private void setStatusForOpponent(int userId, final String status) {
        if (isPeerToPeerCall) {
            connectionStatusLocal.setText(status);
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
                Log.d("UPDATE_USERS", "holder == null");
                return;
            }

            Log.d("UPDATE_USERS", "holder != null");
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

        holder.getProgressBar().setVisibility(View.GONE);

    }

    private void setBackgroundOpponentView(final Integer userId) {
        final OpponentsFromCallAdapter.ViewHolder holder = findHolder(userId);
        if (holder == null) {
            return;
        }

        if (userId != userIDFullScreen) {
            holder.getOpponentView().setBackgroundColor(Color.parseColor("#000000"));
        }
    }

    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onConnectedToUser(ConferenceSession qbrtcSession, final Integer userId) {
        connectionEstablished = true;
        setStatusForOpponent(userId, getString(R.string.text_status_connected));
        setProgressBarForOpponentGone(userId);
    }

    @Override
    public void onConnectionClosedForUser(ConferenceSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_closed));
        if (!isPeerToPeerCall) {
            Log.d(TAG, "onConnectionClosedForUser videoTrackMap.remove(userId)= " + userId);

            OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(userId);
            if (itemHolder != null){
                if(itemHolder.getAdapterPosition() != -1) {
                    Log.d(TAG, "onConnectionClosedForUser  opponentsAdapter.removeItem");
                    opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
                    opponentViewHolders.remove(userId);
                }
            }
//            opponentsAdapter.removeOpponent(getUserById(userId));

            getVideoTrackMap().remove(userId);
            updateActionBar(opponentsAdapter.getItemCount());

//            setBackgroundOpponentView(userId);
//            removeLocalViewToAdapter();
//            fitScaleAdapterTemp();
            fitScaleAdapter();
        }
    }

    @Override
    public void onDisconnectedFromUser(ConferenceSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected));
    }

    //////////////////////////////////   end     //////////////////////////////////////////


    /////////////////// Callbacks from CallActivity.QBRTCSessionUserCallback //////////////////////
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
            if (userId == userIDFullScreen) {
                Log.d(TAG, "setAnotherUserToFullScreen call userId= " + userId);
                setAnotherUserToFullScreen();
            }
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession session) {

    }

    //////////////////////////////////   end     //////////////////////////////////////////

    private void setAnotherUserToFullScreen() {
        if (opponentsAdapter.getOpponents().isEmpty()) {
            return;
        }
        int userId = opponentsAdapter.getItem(0);
//      get opponentVideoTrack - opponent's video track from recyclerView
        QBRTCVideoTrack opponentVideoTrack = getVideoTrackMap().get(userId);
        if (opponentVideoTrack == null) {
            Log.d(TAG, "setAnotherUserToFullScreen opponentVideoTrack == null");
            return;
        }

        fillVideoView(userId, remoteFullScreenVideoView, opponentVideoTrack);
        Log.d(TAG, "fullscreen enabled");

        OpponentsFromCallAdapter.ViewHolder itemHolder = findHolder(userId);
        if (itemHolder != null) {
            opponentsAdapter.removeOpponent(getUserById(userId));
            itemHolder.getOpponentView().release();
            Log.d(TAG, "onConnectionClosedForUser opponentsAdapter.removeItem= " + userId);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.conversation_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.camera_switch:
                Log.d("Conversation", "camera_switch");
                switchCamera(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
//        updateAllOpponentsList(newUsers);
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

    private void runUpdateUsersNames(final ArrayList<QBUser> newUsers) {
        //need delayed for synchronization with recycler view initialization
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

            localVideoView.setVisibility(View.INVISIBLE);

            actionVideoButtonsLayout.setVisibility(View.GONE);

            if (!isPeerToPeerCall) {
                shiftBottomListOpponents();
            }
        }

        private void showToolBarAndButtons() {
            actionBar.show();

            localVideoView.setVisibility(View.VISIBLE);

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

    int i = 0;
    int a = 10;
    boolean cancel;
    ScheduledFuture senderHandler;
    private void scheduler() {

        ScheduledExecutorService scheduler =
                Executors.newSingleThreadScheduledExecutor();
        senderHandler = scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        if(i > 10){
                            cancel = true;
                        }


                        Log.d("AMBRA", "scheduleAtFixedRate  opponentsAdapter item " + i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                             if(cancel){
                                 opponentsAdapter.removeItem(--i);
                             } else {
                                opponentsAdapter.add(new QBUser(i++));
                             }
                                fitScaleAdapter();
                            }
                        });


                        mainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                setStatusForOpponent(i, getString(R.string.text_status_connected));
                            }
                        }, LOCAL_TRACk_INITIALIZE_DELAY);

                    }
                }, 0, 2, TimeUnit.SECONDS);

    }
}


