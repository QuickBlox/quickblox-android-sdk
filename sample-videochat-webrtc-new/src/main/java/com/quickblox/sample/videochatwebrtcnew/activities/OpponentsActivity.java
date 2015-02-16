package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBVideoChatWebRTCSignalingManager;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.callbacks.QBRTCChatCallback;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBGLVideoView;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtcnew.view.VideoCallBacks;

import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by tereha on 27.01.15.
 */
public class OpponentsActivity  extends LogginedUserABActivity implements View.OnClickListener, QBEntityCallback<ArrayList<QBUser>>, QBRTCChatCallback {

    private OpponentsAdapter opponentsAdapter;
    private PullToRefreshListView opponentsList;
    public static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private ArrayList<String> opponentsListToCall;
    private ArrayList<QBUser> opponents;
    public static ArrayList<QBUser> usersList1;
    private List<QBUser> users = new ArrayList<QBUser>();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int listViewIndex;
    private int listViewTop;
    private PullToRefreshListView usersList;
    private Map<String, QBRTCSession> sessionList = new HashMap<>();
    private QBRTCVideoTrack localVideoTrack;
    private String currentSession;

    QBRTCTypes.QBConferenceType qbConferenceType;

    public static ArrayList<QBUser> testUsers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);
        //testUsers.addAll((java.util.Collection<? extends QBUser>) QBUsers.getUsers(getQBPagedRequestBuilder(1), OpponentsActivity.this));

//        usersList1 = DataHolder.createUsersList();
        usersList = (PullToRefreshListView) findViewById(R.id.opponentsList);

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        loadNextPage();



        initUI();
        super.initActionBar();
        //initUsersList();

        QBChatService instance = QBChatService.getInstance();
        QBVideoChatWebRTCSignalingManager videoChatWebRTCSignalingManager = instance.getVideoChatWebRTCSignalingManager();
        videoChatWebRTCSignalingManager.addSignalingManagerListener(
                new QBVideoChatSignalingManagerListener() {
                    @Override
                    public void signalingCreated(QBSignaling signaling, boolean createdLocally) {
                        if (!createdLocally) {
                            // Init Conversation
                            QBRTCClient.init(OpponentsActivity.this);
                            QBRTCClient.getInstance().addCallback(OpponentsActivity.this);
                            QBRTCClient.getInstance().setQBWebRTCSignaling((QBWebRTCSignaling) signaling);
                        }
                    }
                });
    }

    private void initUI() {

        opponentsList = (PullToRefreshListView) findViewById(R.id.opponentsList);
        login = getIntent().getStringExtra("login");

        btnAudioCall = (Button)findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);

    }

    /*private ArrayList<QBUser> createOpponentsFromUserList(ArrayList<QBUser> usersList){
        opponents = new ArrayList<>();
        opponents.addAll(usersList);
        opponents.remove(searchIndexLogginedUser(opponents));

        return opponents;

    }*/

    public static int searchIndexLogginedUser(ArrayList<QBUser> usersList) {

        int indexLogginedUser = -1;

        for (QBUser usr : usersList) {
            if (usr.getLogin().equals(login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }

    /*private void initUsersList() {

        opponentsAdapter = new OpponentsAdapter(this, createOpponentsFromUserList(usersList1));
        opponentsList.setAdapter(opponentsAdapter);



    }
*/
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAudioCall:


                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

                break;

            case R.id.btnVideoCall:

                // get call type
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;


                // prepare custom user data
                Map<String, String> userInfo =  new HashMap<>();
                userInfo.put("any_custom_data", "some data");
                userInfo.put("my_avatar_url", "avatar_reference");

                startVideoChatActivity(getUserIds(opponentsAdapter.getSelected()), qbConferenceType, userInfo);
                getCurrentSession().startCall(null);

                break;
        }
    }

    /*public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : users){
            ids.add(user.getUserNumber());
        }
        return ids;
    }*/

    @Override
    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
        users.addAll(qbUsers);
        int i = searchIndexLogginedUser((ArrayList<QBUser>) users);
        if (i>=0)
        users.remove(i);

        // Prepare users list for simple adapter.
        //
        //opponentsAdapter = new OpponentsAdapter(OpponentsActivity.this, (ArrayList<QBUser>) users, login);
        opponentsList.setAdapter(opponentsAdapter);
        opponentsList.onRefreshComplete();
        opponentsList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);
        //progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(List<String> strings) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(OpponentsActivity.this);
        dialog.setMessage("get users errors: " + strings).create().show();

    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }

    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), OpponentsActivity.this);

    }

    public void startVideoChatActivity(List<Integer> opponents,
                                                        QBRTCTypes.QBConferenceType qbConferenceType,
                                                        Map<String, String> userInfo) {

        QBRTCClient.init(this);
        //QBRTCClient.getInstance().addCallback(this);

        Intent intent = new Intent (this, VideoChatActivity.class);
        intent.putExtra("opponents", (java.io.Serializable) opponents);
        intent.putExtra("qbConferenceType", qbConferenceType);
        intent.putExtra("userInfo", (java.io.Serializable) userInfo);

        for (String key : userInfo.keySet()){
            intent.putExtra("UserInfo:" + key, userInfo.get(key));
        }

        startActivity(intent);

    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : users){
            ids.add(user.getId());
        }
        return ids;
    }

    public QBRTCSession getCurrentSession() {
        return sessionList.get(currentSession);
    }


    @Override
    public void onReceiveNewCallWithSession(QBRTCSession session) {
        Toast.makeText(this, "IncomeCall", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReceiveDialingWithSession(QBRTCSession session) {

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer integer) {

    }

    @Override
    public void onBeginConnectToUser(QBRTCSession session, Integer integer) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer integer, Map<String, String> stringStringMap) {

    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack qbrtcVideoTrack) {

    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack qbrtcVideoTrack, Integer integer) {

    }

    @Override
    public void onSessionEnd(QBRTCSession session) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession session, Integer integer) {

    }

    @Override
    public void onUserDisconnected(QBRTCSession session, Integer integer) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer integer) {

    }
}
