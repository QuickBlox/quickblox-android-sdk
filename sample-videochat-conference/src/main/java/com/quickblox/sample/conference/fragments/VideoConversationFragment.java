package com.quickblox.sample.conference.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.conference.view.QBConferenceSurfaceView;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.activities.SelectUsersActivity;
import com.quickblox.sample.conference.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.BaseSession;
import com.quickblox.videochat.webrtc.QBRTCAudioTrack;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientAudioTracksCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * QuickBlox team
 */
public class VideoConversationFragment extends BaseConversationFragment implements Serializable, QBRTCClientVideoTracksCallbacks<ConferenceSession>,
        QBRTCClientAudioTracksCallback<ConferenceSession>, QBRTCSessionStateCallback<ConferenceSession>, OpponentsFromCallAdapter.OnAdapterEventListener {

    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;
    private static final int REQUEST_ADD_OCCUPANTS = 175;

    private static final int DISPLAY_ROW_AMOUNT = 3;
    private static final int SMALL_CELLS_AMOUNT = 8;
    private static final int LARGE_CELLS_AMOUNT = 12;

    private String TAG = VideoConversationFragment.class.getSimpleName();

    private ToggleButton cameraToggle;
    private View view;
    private LinearLayout actionVideoButtonsLayout;
    private QBConferenceSurfaceView localVideoView;
    private CameraState cameraState = CameraState.DISABLED_FROM_USER;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private QBRTCVideoTrack localVideoTrack;
    private TextView connectionStatusLocal;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private LocalViewOnClickListener localViewOnClickListener;
    private boolean isRemoteShown;

    private int userIDFullScreen;
    private List<QBUser> allOpponents;
    private boolean allCallbacksInit;
    private boolean isCurrentCameraFront;
    private GridLayoutManager gridLayoutManager;
    private SpanSizeLookupImpl spanSizeLookup;
    Set<Integer> usersToDestroy;
    private boolean isNeedCleanUp;


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
        localViewOnClickListener = new LocalViewOnClickListener();
        usersToDestroy = new HashSet<>();
        allOpponents = Collections.synchronizedList(new ArrayList<QBUser>(opponents.size()));
        allOpponents.addAll(opponents);
    }

    public void setDuringCallActionBar() {
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(currentUser.getFullName());
    }

    private void updateActionBar(int amountOpponents) {
        actionBar.setSubtitle(getString(R.string.opponents, String.valueOf(amountOpponents)));
    }

    private void initVideoTracksListener() {
        if (currentSession != null) {
            currentSession.addVideoTrackCallbacksListener(this);
        }
    }

    private void initAudioTracksListener() {
        if (currentSession != null) {
            currentSession.addAudioTrackCallbacksListener(this);
        }
    }

    private void removeVideoTracksListener() {
        if (currentSession != null) {
            currentSession.removeVideoTrackCallbacksListener(this);
        }
    }

    private void removeAudioTracksListener() {
        if (currentSession != null) {
            currentSession.removeAudioTrackCallbacksListener(this);
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
            conversationFragmentCallbackListener.addClientConnectionCallback(this);
            initVideoTracksListener();
            initAudioTracksListener();
            allCallbacksInit = true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setHasOptionsMenu(true);
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
            if(opponentsAdapter.getItemCount() > 0){
                OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(opponentsAdapter.getItem(0));
                if(itemHolder != null) {
                    itemHolder.itemView.requestLayout();
                }
            }
        }

        @Override
        public void onItemsUpdated(RecyclerView recyclerView, int positionStart, int itemCount,
                                   Object payload) {
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
            if(itemCount % 4 == 0) {
                return 3;
            }

            if(itemCount % 4 == 1) {
//              check last position
                if (position == itemCount - 1) {
                    return 12;
                }
            } else if(itemCount % 4 == 2) {
                if(position == itemCount - 1 || position == itemCount - 2) {
                    return 6;
                }
            } else if(itemCount % 4 == 3) {
                if (position == itemCount - 1 || position == itemCount - 2 || position == itemCount - 3) {
                    return 4;
                }
            }

            return 3;
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
        localVideoView.setOnClickListener(localViewOnClickListener);

            recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);

            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
            recyclerView.setHasFixedSize(false);

            gridLayoutManager = new GridManager(getActivity(), 12);
            gridLayoutManager.setReverseLayout(false);
            spanSizeLookup = new SpanSizeLookupImpl();
            spanSizeLookup.setSpanIndexCacheEnabled(false);
            gridLayoutManager.setSpanSizeLookup(spanSizeLookup);
            recyclerView.setLayoutManager(gridLayoutManager);

//          for correct removing item in adapter
            recyclerView.setItemAnimator(null);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setGrid(recyclerView.getHeight());
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });

        connectionStatusLocal = (TextView) view.findViewById(R.id.connectionStatusLocal);

        cameraToggle = (ToggleButton) view.findViewById(R.id.toggle_camera);
        cameraToggle.setVisibility(View.VISIBLE);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        actionButtonsEnabled(false);
    }

    private void setGrid(int recycleViewHeight) {
        ArrayList<QBUser> qbUsers = new ArrayList<>();
        int itemHeight = recycleViewHeight / DISPLAY_ROW_AMOUNT;
        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), currentSession, qbUsers,
                (int) getResources().getDimension(R.dimen.item_width),
                itemHeight);
        opponentsAdapter.setAdapterListener(VideoConversationFragment.this);
        recyclerView.setAdapter(opponentsAdapter);
    }

    private Map<Integer, QBRTCVideoTrack> getVideoTrackMap() {
        if (videoTrackMap == null) {
            videoTrackMap = new HashMap<>();
        }
        return videoTrackMap;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            cameraToggle.setChecked(true);
            toggleCamera(true);
        }
        isNeedCleanUp = true;
        cleanAdapterIfNeed();
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER) {
            toggleCamera(false);
        }
        isNeedCleanUp = false;
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_OCCUPANTS) {
                Log.d(TAG, "onActivityResult REQUEST_ADD_OCCUPANTS");
                ArrayList<QBUser> addedOccupants = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);
                List<Integer> allOccupants = (List<Integer>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_OCCUPANTS_IDS);
                allOpponents.addAll(0, addedOccupants);
            }
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
        removeVideoTracksListener();
        removeAudioTracksListener();
        removeVideoTrackRenderers();
        releaseViews();
    }

    private void releaseViewHolders() {
        if (opponentViewHolders != null) {
            opponentViewHolders.clear();
        }
    }

    private void removeConnectionStateListeners(){
        conversationFragmentCallbackListener.removeClientConnectionCallback(this);
    }

    private void releaseViews() {
        if (localVideoView != null) {
            localVideoView.release();
        }
        localVideoView = null;

        releseOpponentsViews();
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
        updateVideoView(localVideoView, isCurrentCameraFront);
        toggleCamera(true);
    }

    private void runOnUiThread(Runnable runnable) {
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        if (currentSession != null && currentSession.getMediaStreamManager() != null) {
            conversationFragmentCallbackListener.onSetVideoEnabled(isNeedEnableCam);
        }
        if (!cameraToggle.isEnabled()) {
            cameraToggle.setEnabled(true);
        }
    }

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////
    @Override
    public void onLocalVideoTrackReceive(ConferenceSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        cameraState = CameraState.NONE;
        actionButtonsEnabled(true);

        if (localVideoView != null) {
            fillVideoView(localVideoView, localVideoTrack, false);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(ConferenceSession session, final QBRTCVideoTrack videoTrack, final Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);

        setOpponentToAdapter(userID);

            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRemoteViewMultiCall(userID, videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);

    }


    @Override
    public void onLocalAudioTrackReceive(ConferenceSession session, QBRTCAudioTrack audioTrack) {

    }

    @Override
    public void onRemoteAudioTrackReceive(ConferenceSession session, QBRTCAudioTrack audioTrack, Integer userID) {
        currentSession.getMediaStreamManager().addAudioTrack(userID, audioTrack);
    }

    /////////////////////////////////////////    end    ////////////////////////////////////////////

    private void setOpponentToAdapter(Integer userID) {
            QBUser qbUser = getUserById(userID);
            if(qbUser != null){
                opponentsAdapter.add(qbUser);
            } else {
                QBUser user = new QBUser(userID);
                user.setFullName("NoName");
                opponentsAdapter.add(user);
            }
        recyclerView.requestLayout();
    }

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(final OpponentsFromCallAdapter.ViewHolder holder, final int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);

    }

    @Override
    public void onToggleButtonItemClick(int position, boolean isAudioEnabled) {
        int userId = opponentsAdapter.getItem(position);
        Log.d(TAG, "onToggleButtonItemClick userId= " + userId);
        adjustOpponentAudio(userId, isAudioEnabled);
    }

    private void adjustOpponentAudio(int userID, boolean isAudioEnabled) {
        currentSession.getMediaStreamManager().getAudioTrack(userID).setEnabled(isAudioEnabled);
    }


    private void setRemoteViewMultiCall(int userID, QBRTCVideoTrack videoTrack) {
        if(currentSession.isDestroyed()){
            Log.d(TAG, "setRemoteViewMultiCall currentSession.isDestroyed RETURN");
            return;
        }
        Log.d(TAG, "setRemoteViewMultiCall fillVideoView");
        if(!isRemoteShown){
            isRemoteShown = true;
            setRecyclerViewVisibleState();
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

    private void setRecyclerViewVisibleState() {
        recyclerView.setVisibility(View.VISIBLE);
    }


    private OpponentsFromCallAdapter.ViewHolder getViewHolderForOpponent(Integer userID) {
        OpponentsFromCallAdapter.ViewHolder holder = opponentViewHolders.get(userID);
        if (holder == null) {
            Log.d(TAG, "holder not found in cache");
            holder = findHolder(userID);
            if (holder != null) {
                opponentViewHolders.put(userID, holder);
            }
        }
        return holder;
    }

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        Log.d(TAG, "findHolder for "+userID);
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
        if(userId == currentUser.getId()) {
            return;
        }
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }

        holder.setStatus(status);
    }

    private void setProgressBarForOpponentGone(int userId) {
        final OpponentsFromCallAdapter.ViewHolder holder = getViewHolderForOpponent(userId);
        if (holder == null) {
            return;
        }

        holder.getProgressBar().setVisibility(View.GONE);

    }


    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onStateChanged(ConferenceSession session, BaseSession.QBRTCSessionState state) {

    }

    @Override
    public void onConnectedToUser(ConferenceSession qbrtcSession, final Integer userId) {
        setStatusForOpponent(userId, getString(R.string.text_status_connected));
        setProgressBarForOpponentGone(userId);
    }

    @Override
    public void onConnectionClosedForUser(ConferenceSession qbrtcSession, Integer userId) {
        Log.d(TAG, "onConnectionClosedForUser userId= " + userId);

        if (currentSession.isDestroyed()) {
            Log.d(TAG, "onConnectionClosedForUser isDestroyed userId= " + userId);
            return;
        }

        if (isNeedCleanUp) {
            setStatusForOpponent(userId, getString(R.string.text_status_closed));
            cleanUpAdapter(userId);
        } else {
            usersToDestroy.add(userId);
        }

    }

    @Override
    public void onDisconnectedFromUser(ConferenceSession qbrtcSession, Integer integer) {
        setStatusForOpponent(integer, getString(R.string.text_status_disconnected));
    }

    //////////////////////////////////   end     //////////////////////////////////////////


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
            case R.id.add_opponent:
                Log.d("Conversation", "add_opponent");
                addOpponentToDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void cleanAdapterIfNeed() {
        if(!usersToDestroy.isEmpty()) {
            Iterator<Integer> iterator = usersToDestroy.iterator();
            while (iterator.hasNext()) {
                cleanUpAdapter(iterator.next());
                iterator.remove();
            }
        }
    }

    private void cleanUpAdapter(int userId){
        Log.d(TAG, "onConnectionClosedForUser cleanUpAdapter userId= " + userId);
        OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userId);
        if (itemHolder != null) {
            if (itemHolder.getAdapterPosition() != -1) {
                Log.d(TAG, "onConnectionClosedForUser  opponentsAdapter.removeItem");
                opponentsAdapter.removeItem(itemHolder.getAdapterPosition());
                opponentViewHolders.remove(userId);
            }
        }

        getVideoTrackMap().remove(userId);
        updateActionBar(opponentsAdapter.getItemCount());
        recyclerView.requestLayout();
    }

    private void addOpponentToDialog(){
        SelectUsersActivity.startForResult(this, REQUEST_ADD_OCCUPANTS, getChatDialog(currentSession.getDialogID()));
    }

    private QBChatDialog getChatDialog(String dialogId) {
        QBChatDialog chatDialog = new QBChatDialog(dialogId);
        chatDialog.setType(QBDialogType.GROUP);
        return chatDialog;
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
            Log.d(TAG, "localView onClick");
        }
    }
}


