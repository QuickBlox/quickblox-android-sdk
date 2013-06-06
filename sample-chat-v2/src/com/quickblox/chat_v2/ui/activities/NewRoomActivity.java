package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.quickblox.module.chat.model.QBChatRoom;

public class NewRoomActivity extends ListActivity {

    private EditText roomNameEditText;
    private Button joinRoomButton;
    private ContactsAdapter contactsAdapter;
    private ListView selectionTable;

    private ChatApplication app;
    private ProgressDialog progress;

    private StringBuilder sb;
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

                finishArctivityRecivedResult();

            } else {
                Toast.makeText(getBaseContext(), getString(R.string.room_name_emty_msg), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void finishArctivityRecivedResult() {
        createRoomInfoAndSendtoQb();

        Intent intent = new Intent();
        intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.ROOM_ACTIVITY);
        intent.putExtra(GlobalConsts.ROOM_NAME, roomName);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void createRoomInfoAndSendtoQb() {
        roomName = roomNameEditText.getText().toString();
//        Handler handler = new android.os.Handler(Looper.getMainLooper());
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                QBChatRoom chatRoom = QBChat.createRoom(roomName, app.getQbUser(), true, true);

                app.getMsgManager().createRoom(roomName, sb.append(getResources().getString(R.string.quickblox_app_id))
                        .append(roomName).append("_").append("@muc.quickblox.com").toString(), app.getInviteUserList());
                sb.setLength(0);
        //    }
        //}, 5000);


    }

}
