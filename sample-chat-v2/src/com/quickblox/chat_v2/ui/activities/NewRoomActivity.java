package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.ContactsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class NewRoomActivity extends ListActivity {

    private EditText roomNameEditText;
    private Button joinRoomButton;
    private ContactsAdapter contactsAdapter;
    private ListView selectionTable;
    private ProgressDialog progress;

    private ChatApplication app;
    private QBChatRoom chatRoom;
    private ArrayList<QBUser> contactsList;

    private StringBuilder sb;
    private String roomName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_room_layout);
        app = ChatApplication.getInstance();
        sb = new StringBuilder();

        contactsList = new ArrayList<QBUser>(app.getContactsMap().values());
        initViews();
    }

    private void initViews() {

        roomNameEditText = (EditText) findViewById(R.id.room_name_et);

        joinRoomButton = (Button) findViewById(R.id.room_join_btn);
        joinRoomButton.setOnClickListener(joinButtonClickListener);

        selectionTable = (ListView) findViewById(android.R.id.list);
        selectionTable.setClickable(true);

        contactsAdapter = new ContactsAdapter(this, contactsList, true);
        setListAdapter(contactsAdapter);
    }

    private View.OnClickListener joinButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String roomName = roomNameEditText.getText().toString();
            if (!TextUtils.isEmpty(roomName)) {
                switchProgressDialog(true);
                createAndSaveRoom();
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.room_name_emty_msg), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void createAndSaveRoom() {

        roomName = roomNameEditText.getText().toString();

        final List<Integer> users = contactsAdapter.getCheckedUsers();

        QBChat.getInstance().createRoom(roomName, app.getQbUser(), true, false, new RoomListener() {

            @Override
            public void onCreatedRoom(QBChatRoom pRoom) {
                chatRoom = pRoom;

                chatRoom.addMessageListener(app.getMsgManager());
                app.setJoinedRoom(chatRoom);
                chatRoom.addMembers(users);
                pRoom.invite(users);
                finishArctivityRecivedResult();
                switchProgressDialog(false);
            }

            @Override
            public void onJoinedRoom(QBChatRoom pRoom) {

            }
        });

        users.add(app.getQbUser().getId());
        app.getMsgManager().createRoom(roomName, sb.append(getResources().getString(R.string.quickblox_app_id)).append("_").append(roomName).append("@muc.quickblox.com").toString(),
                users);
        sb.setLength(0);
    }


    private void finishArctivityRecivedResult() {

        Intent intent = new Intent();
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.ROOM_ACTIVITY);
        intent.putExtra(GlobalConsts.ROOM_NAME, roomName);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void switchProgressDialog(boolean enable) {
        if (enable) {
            progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.room_activity_connecting_room),
                    true);
        } else {
            progress.dismiss();
        }
    }

}
