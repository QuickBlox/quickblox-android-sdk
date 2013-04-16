package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.InvitationListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 12:53 PM
 */
public class ChatActivity extends Activity {

    private TopBar topBar;
    private ViewGroup messagesContainer;
    private ScrollView scrollContainer;
    private EditText msgTxt;

    private int userId;
    private String dialogId;
    private String lastMsg;
    private QBChatRoom chatRoom;
    private byte previousActivity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.chat_layout);
        initViews();
        getDialogMessages();
    }

    @Override
    public void onBackPressed() {
        updateDialogLastMessage();
        super.onBackPressed();
    }


    private void initViews() {
        topBar = (TopBar) findViewById(R.id.top_bar);
        topBar.setFragmentName(TopBar.CHAT_ACTIVITY);
        messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);
        scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);
        msgTxt = (EditText) findViewById(R.id.messageEdit);

        previousActivity = getIntent().getByteExtra(GlobalConsts.PREVIOUS_ACTIVITY, (byte) 0);
        if (previousActivity == GlobalConsts.ROOM_ACTIVITY) {
            QBChat.openXmmpRoom(pChatMessageListener, pInvitationListener, pParticipantListener);
            boolean isPersistent = getIntent().getBooleanExtra(GlobalConsts.IS_ROOM_PERSISTENT, false);
            boolean isOnlyMembers = getIntent().getBooleanExtra(GlobalConsts.IS_ONLY_MEMBERS, false);
            String chatRoomName = getIntent().getStringExtra(GlobalConsts.ROOM_NAME);
            chatRoom = QBChat.createRoom(chatRoomName, DataHolder.getInstance().getQbUser(), isOnlyMembers, isPersistent);

        } else if (previousActivity == GlobalConsts.DIALOG_ACTIVITY) {
            userId = getIntent().getIntExtra(GlobalConsts.USER_ID, 0);
            dialogId = getIntent().getStringExtra(GlobalConsts.DIALOG_ID);
            QBChat.openXmmpChat(pDialogMessageListener);
        }


    }

    public void onSendBtnClick(View view) {
        lastMsg = msgTxt.getText().toString();
        msgTxt.setText("");
        showMessage(lastMsg, true);

        if (previousActivity == GlobalConsts.DIALOG_ACTIVITY) {
            QBChat.sendMessage(userId, lastMsg);
            createMessage(lastMsg, DataHolder.getInstance().getQbUser().getId());
        } else {
            try {
                chatRoom.sendMessage(msgTxt.getText().toString());
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }

        updateDialogLastMessage();
    }

    private void showMessage(String message, boolean leftSide) {
        final TextView textView = new TextView(ChatActivity.this);
        textView.setTextColor(Color.BLACK);
        textView.setText(message);

        int bgRes = R.drawable.left_message_bg;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        if (!leftSide) {
            bgRes = R.drawable.right_message_bg;
            params.gravity = Gravity.RIGHT;
        }

        textView.setLayoutParams(params);

        textView.setBackgroundResource(bgRes);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messagesContainer.addView(textView);
                // Scroll to bottom
                if (scrollContainer.getChildAt(0) != null) {
                    scrollContainer.scrollTo(scrollContainer.getScrollX(), scrollContainer.getChildAt(0).getHeight());
                }
                scrollContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void getDialogMessages() {
        QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
        requestBuilder.eq(GlobalConsts.USER_ID_FIELD, DataHolder.getInstance().getQbUser().getId());
        QBCustomObjects.getObjects(GlobalConsts.MESSAGES, requestBuilder, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    applyDialogMessags(((QBCustomObjectLimitedResult) result).getCustomObjects());
                }
            }
        });
    }

    private void updateDialogLastMessage() {
        QBCustomObject co = new QBCustomObject();
        co.setClassName(GlobalConsts.DIALOGS);
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.LAST_MSG, lastMsg);
        co.setFields(fields);
        co.setCustomObjectId(dialogId);
        QBCustomObjects.updateObject(co, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {

            }
        });
    }

    private void createMessage(String msgText, int recepientId) {
        QBCustomObject co = new QBCustomObject();
        co.setClassName(GlobalConsts.MESSAGES);
        HashMap<String, Object> fields = new HashMap<String, Object>();
        fields.put(GlobalConsts.MSG_TEXT, msgText);
        fields.put(GlobalConsts.RECEPIENT_ID, recepientId);
        co.setFields(fields);
        QBCustomObjects.createObject(co, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {

            }
        });
    }

    private void applyDialogMessags(List<QBCustomObject> messageList) {
        for (QBCustomObject message : messageList) {
            int userId = Integer.parseInt(message.getFields().get(GlobalConsts.RECEPIENT_ID).toString());
            if (userId == DataHolder.getInstance().getQbUser().getId()) {
                showMessage(message.getFields().get(GlobalConsts.MSG_TEXT).toString(), true);
            } else {
                showMessage(message.getFields().get(GlobalConsts.MSG_TEXT).toString(), false);
            }
        }
    }


    private MessageListener pDialogMessageListener = new MessageListener() {
        @Override
        public void processMessage(Chat chat, Message message) {
            lastMsg = message.getBody();
            showMessage(lastMsg, false);
            createMessage(lastMsg, userId);
        }
    };

    private PacketListener pChatMessageListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {
            Message msg = (Message) packet;
            showMessage(msg.getBody(), false);
        }
    };

    private InvitationListener pInvitationListener = new InvitationListener() {
        @Override
        public void invitationReceived(Connection connection, String s, String s2, String s3, String s4, Message message) {

        }
    };

    private PacketListener pParticipantListener = new PacketListener() {
        @Override
        public void processPacket(Packet packet) {

        }
    };
}
