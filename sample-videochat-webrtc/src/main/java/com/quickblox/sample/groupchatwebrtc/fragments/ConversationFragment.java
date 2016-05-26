package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsFromCallAdapter;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.CameraUtils;
import com.quickblox.sample.groupchatwebrtc.view.RTCGLVideoView;
import com.quickblox.sample.groupchatwebrtc.view.RTCGLVideoView.RendererConfig;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static android.support.v7.widget.LinearLayoutManager.*;


/**
 * QuickBlox team
 */
public class ConversationFragment extends BaseConversationFragment implements Serializable, QBRTCClientVideoTracksCallbacks,
        QBRTCSessionConnectionCallbacks, CallActivity.QBRTCSessionUserCallback, OpponentsFromCallAdapter.OnAdapterEventListener {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;

    private String TAG = ConversationFragment.class.getSimpleName();

    private ToggleButton cameraToggle;
    private View view;
    private boolean isVideoCall = false;
    private LinearLayout actionVideoButtonsLayout;
    private RTCGLVideoView localVideoView;
    private CameraState cameraState = CameraState.NONE;
    private RecyclerView recyclerView;
    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private Handler mainHandler;
    private TextView connectionStatusLocal;

    private Map<Integer, QBRTCVideoTrack> videoTrackMap;
    private OpponentsFromCallAdapter opponentsAdapter;
    private boolean isRemoteShown;

    private int amountOpponents;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = super.onCreateView(inflater,container,savedInstanceState);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        initActionBarInner();

        mainHandler = new FragmentLifeCycleHandler();
        return view;
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_conversation;
    }

    @Override
    protected void initFields() {
        super.initFields();
        amountOpponents = opponents.size();

        isPeerToPeerCall = opponents.size() == 1;
        isVideoCall = (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(currentSession.getConferenceType()));
    }

    public void initActionBarInner() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar_call);
        if (toolbar != null) {
            QBUser user = QBChatService.getInstance().getUser();
            Log.d(TAG, "user = "+ user.toString());
            if (user != null) {
                Log.d(TAG, "userFullName = "+ user.getFullName() + "AMOUNT_OPPONENTS = " + amountOpponents);
                toolbar.setTitle(user.getFullName());
                toolbar.setSubtitle(getString(R.string.opponents, amountOpponents));
            }

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_w);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Conversation", "back clicked");
                }
            });
        }
    }

    private void initVideoTrackSListener() {
        if (currentSession != null){
            currentSession.addVideoTrackCallbacksListener(this);
        }
    }

    private void removeVideoTrackSListener(){
        if (currentSession != null){
            currentSession.removeVideoTrackCallbacksListener(this);
        }
    }

    @Override
    protected void actionButtonsEnabled(boolean enability) {
        super.actionButtonsEnabled(enability);
        cameraToggle.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
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

        if(!isPeerToPeerCall){
            recyclerView = (RecyclerView) view.findViewById(R.id.grid_opponents);

            recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), R.dimen.grid_item_divider));
            recyclerView.setHasFixedSize(true);
            final int columnsCount = defineColumnsCount();
            final int rowsCount = defineRowCount();
            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(getActivity(), HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    setGrid(columnsCount, rowsCount);
                    recyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
        connectionStatusLocal = (TextView)view.findViewById(R.id.connectionStatusLocal);

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
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

        opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), opponents, (int) getResources().getDimension(R.dimen.item_width),
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
        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                toggleCamera(isChecked);
            }
        });
    }

    private void switchCamera(){
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
                        toggleCamerainternal(mediaStreamManager);
                    }
                });
            }
        });
    }

    private void toggleCamerainternal(QBMediaStreamManager mediaStreamManager) {
        toggleCameraOnUiThread(false);
        int currentCameraId = mediaStreamManager.getCurrentCameraId();
        Log.d(TAG, "Camera was switched!");
        RendererConfig config = new RendererConfig();
        config.mirror = CameraUtils.isCameraFront(currentCameraId);
        localVideoView.updateRenderer(isPeerToPeerCall ? RTCGLVideoView.RendererSurface.SECOND :
                RTCGLVideoView.RendererSurface.MAIN, config);
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

    ////////////////////////////  callbacks from QBRTCClientVideoTracksCallbacks ///////////////////

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");
        localVideoTrack = videoTrack;
        if (localVideoView != null) {
//            fillVideoView(localVideoView, videoTrack, !isPeerToPeerCall);
            fillVideoView(localVideoView, videoTrack, false);
        }
        if(isPeerToPeerCall){
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (localVideoView != null) {
                        return;
                    }
                    Log.i(TAG, "onLocalVideoTrackReceive init localView");
                    localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();

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

        if(isPeerToPeerCall){
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    Log.d("onRemoteVideoTrackRe", "localVideoView==null?" + (localVideoView == null));
                    Log.d("onRemoteVideoTrackRe", "videoTrack==null?" + (videoTrack == null));
                    RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
                    config.mirror = true;
                    config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
                    localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
                    fillVideoView(localVideoView, videoTrack);
                }
            }, LOCAL_TRACk_INITIALIZE_DELAY);

        } else {
            OpponentsFromCallAdapter.ViewHolder itemHolder = getViewHolderForOpponent(userID);
            if (itemHolder == null) {
                return;
            }
            RTCGLVideoView remoteVideoView = itemHolder.getOpponentView();

            getVideoTrackMap().put(userID, videoTrack);
            if (remoteVideoView != null) {
                Log.d(TAG, "onRemoteVideoTrackReceive fillVideoView");
                fillVideoView(remoteVideoView, videoTrack);
                if (!isRemoteShown) {
                    isRemoteShown = true;

                    RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
                    config.mirror = true;
                    config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
                    localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
                    fillVideoView(localVideoView, videoTrack);
                }
            }
        }
    }
    /////////////////////////////////////////    end    ////////////////////////////////////////////

    //last opponent view is bind
    @Override
    public void OnBindLastViewHolder(OpponentsFromCallAdapter.ViewHolder holder, int position) {
        Log.i(TAG, "OnBindLastViewHolder position=" + position);
        if (!isVideoCall) {
            return;
        }
        if (isPeerToPeerCall) {
            Log.i(TAG, " isPeerToPeerCall");
            localVideoView = holder.getOpponentView();

//            initLocalViewUI(holder.itemView);
        } else {
            //on group call we postpone initialization of localVideoView due to set it on Gui renderer.
            // Refer to RTCGlVIew
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (localVideoView != null) {
                        return;
                    }
                    Log.i(TAG, "OnBindLastViewHolder init localView");
                    localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();
                    localVideoView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Log.i(TAG, "Click! Click! Who is there?");
                            ActionBar actionBar = ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar();
                            if (actionBar.isShowing()) {
                                Log.i(TAG, "Here's Johnny");
                                actionBar.hide();
                                localVideoView.releaseLocalRendererCallback();
                                actionVideoButtonsLayout.setVisibility(View.GONE);

                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                params.setMargins(0, 0, 0, 0);

//                              IMHO setLayoutParams not necessary to call
                                recyclerView.setLayoutParams(params);
                            } else {
                                Log.i(TAG, "Johnny is out");
                                actionBar.show();
                                fillVideoView(localVideoView, localVideoTrack, false);
                                RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
                                config.mirror = true;
                                config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
                                localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
                                actionVideoButtonsLayout.setVisibility(View.VISIBLE);

                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) recyclerView.getLayoutParams();
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                                params.setMargins(0, 0, 0, (int) getResources().getDimension(R.dimen.margin_common));

                                recyclerView.setLayoutParams(params);
                            }
                        }
                    });
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
        localVideoView.release();
        QBRTCVideoTrack remoteVideoTrack = videoTrackMap.get(userId);
        fillVideoView(localVideoView, remoteVideoTrack);
        Log.d(TAG, "fullscreen enabled");

        fillVideoView(localVideoView, localVideoTrack, false);
        RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
        config.mirror = true;
        config.coordinates = getResources().getIntArray(R.array.local_view_coordinates_my_screen);
        localVideoView.updateRenderer(RTCGLVideoView.RendererSurface.SECOND, config);
        Log.d(TAG, "preview screen enabled");

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

    private OpponentsFromCallAdapter.ViewHolder findHolder(Integer userID) {
        int childCount = recyclerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = recyclerView.getChildAt(i);
            OpponentsFromCallAdapter.ViewHolder childViewHolder = (OpponentsFromCallAdapter.ViewHolder) recyclerView.getChildViewHolder(childView);
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

    private void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    private void setStatusForOpponent(int userId, final String status) {
        if(isPeerToPeerCall){
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


    ///////////////////////////////  QBRTCSessionConnectionCallbacks ///////////////////////////

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, final Integer userId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionButtonsEnabled(true);
            }
        });
        setStatusForOpponent(userId, getString(R.string.connected));
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
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
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
                return true;
            case R.id.camera_switch:
                Log.d("Conversation", "camera_switch");
                switchCamera();
            default:
                return super.onOptionsItemSelected(item);
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
}


