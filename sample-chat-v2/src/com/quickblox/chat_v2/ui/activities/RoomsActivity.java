package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.RoomListAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnRoomListDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 11.04.13 Time: 9:58
 */
public class RoomsActivity extends Activity implements RoomListener, OnRoomListDownloaded {

    private ListView roomListLv;
    private Button newRoomBtn;
    private RoomListAdapter roomListAdapter;

    private QBChatRoom chatRoom;
    private String roomName;

    private ChatApplication app;
    private static final int REQUEST_NEW_ROOM = 0;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.rooms_layout);
        app = ChatApplication.getInstance();

        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.getMsgManager().setRoomListDownloadListener(this);
        app.getMsgManager().downloadPersistentRoom();
        initViews();
    }

    private void initViews() {

        roomListLv = (ListView) findViewById(R.id.room_list_lv);
        roomListLv.setOnItemClickListener(itemClick);
        newRoomBtn = (Button) findViewById(R.id.new_room_btn);
        newRoomBtn.setOnClickListener(newRoomBtnClickListener);
        applyRoomList(app.getUserPresentRoomList());

    }

    private View.OnClickListener newRoomBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(RoomsActivity.this, NewRoomActivity.class);
            startActivityForResult(intent, REQUEST_NEW_ROOM);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_NEW_ROOM:
                if (resultCode != Activity.RESULT_CANCELED) {
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtras(data.getExtras());
                    startActivity(intent);
                }

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void applyRoomList(List<QBCustomObject> roomList) {

        roomListAdapter = new RoomListAdapter(this, (ArrayList) roomList);
        roomListLv.setAdapter(roomListAdapter);
        refreshData();
    }

    private OnItemClickListener itemClick = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {


            QBCustomObject co = (QBCustomObject) parent.getItemAtPosition(position);
            roomName = co.getFields().get(GlobalConsts.ROOM_NAME).toString();
            chatRoom = QBChat.joinRoom(roomName, app.getQbUser(), RoomsActivity.this);
        }


    };

    private void saveRoomAndStartActivity() {
        Intent i = new Intent(RoomsActivity.this, ChatActivity.class);
        i.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.ROOM_ACTIVITY);
        i.putExtra(GlobalConsts.ROOM_NAME, roomName);
        RoomsActivity.this.startActivity(i);
    }

    private void refreshData() {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                roomListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void roomListDownloaded() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                applyRoomList(app.getUserPresentRoomList());
            }
        });

    }

    @Override
    public void onCreatedRoom() {

    }

    @Override
    public void onJoinedRoom() {
        app.setJoinedRoom(chatRoom);
        saveRoomAndStartActivity();
    }

    @Override
    public void onReceiveRooms(List<String> list) {

    }
}
