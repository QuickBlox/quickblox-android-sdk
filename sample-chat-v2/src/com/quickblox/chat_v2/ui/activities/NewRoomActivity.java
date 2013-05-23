package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.ContactsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/12/13
 * Time: 4:38 PM
 */
public class NewRoomActivity extends ListActivity {

    private EditText roomNameEditText;
    private Button joinRoomButton;
    private ContactsAdapter contactsAdapter;
    private ListView selectionTable;

    private ChatApplication app;
    private ProgressDialog progress;

    private StringBuilder sb;
    private QBChatRoom chatRoom;
    private String roomName;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_room_layout);
        app = ChatApplication.getInstance();
        sb = new StringBuilder();
        initViews();
    }

    private void initViews() {

        roomNameEditText = (EditText) findViewById(R.id.room_name_et);
        joinRoomButton = (Button) findViewById(R.id.room_join_btn);
        joinRoomButton.setOnClickListener(joinButtonClickListener);

        selectionTable = (ListView) findViewById(android.R.id.list);
        selectionTable.setClickable(true);
        contactsAdapter = new ContactsAdapter(this, app.getContactsList(), true, true);
        setListAdapter(contactsAdapter);
    }

    private View.OnClickListener joinButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String roomName = roomNameEditText.getText().toString();
            if (!TextUtils.isEmpty(roomName)) {

                loadChatActivity();

            } else {
                Toast.makeText(getBaseContext(), getString(R.string.room_name_emty_msg), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void loadChatActivity() {
        roomName = roomNameEditText.getText().toString();

        chatRoom = QBChat.createRoom(roomName, app.getQbUser(), true, true);

        app.getMsgManager().createRoom(roomName, sb.append(roomName).append("_").append(getResources().getString(R.string.quickblox_app_id)).append("@muc.quickblox.com").toString());
        sb.setLength(0);

        Intent intent = new Intent(NewRoomActivity.this, ChatActivity.class);
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.ROOM_ACTIVITY);
        intent.putExtra(GlobalConsts.ROOM_NAME, roomName);
        startActivity(intent);
        finish();
    }


}
