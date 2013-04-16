package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.RoomListAdapter;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 11.04.13
 * Time: 9:58
 */
public class RoomsActivity extends Activity {

    private ListView roomListLv;
    private Button newRoomBtn;

    private RoomListAdapter roomListAdapter;


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.rooms_layout);
        initViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        downloadRoomList();
    }

    private void initViews() {
        roomListLv = (ListView) findViewById(R.id.room_list_lv);
        newRoomBtn = (Button) findViewById(R.id.new_room_btn);
        newRoomBtn.setOnClickListener(newRoomBtnClickListener);
    }

    private View.OnClickListener newRoomBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getBaseContext(), NewRoomActivity.class);
            startActivity(intent);
        }
    };

    private void applyRoomList(Collection<QBChatRoom> roomList) {
        roomListAdapter = new RoomListAdapter(this, roomList);
        roomListLv.setAdapter(roomListAdapter);
    }

    private void downloadRoomList() {
        Collection<QBChatRoom> roomList = QBChat.requestAllRooms();
        if (roomList != null) {
            applyRoomList(roomList);
        }
    }
}
