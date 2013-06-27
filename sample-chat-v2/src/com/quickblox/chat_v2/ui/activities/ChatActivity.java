package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.core.CustomPictureAttachListener;
import com.quickblox.chat_v2.interfaces.OnDialogCreateComplete;
import com.quickblox.chat_v2.interfaces.OnFileUploadComplete;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnNewMessageIncome;
import com.quickblox.chat_v2.interfaces.OnNewRoomMessageIncome;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.InvitationListener;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends Activity implements OnMessageListDownloaded, OnFileUploadComplete, OnNewMessageIncome, OnDialogCreateComplete, OnNewRoomMessageIncome {

    private final int SELECT_PHOTO = 2;
    private boolean isAttach;
    private String[] parts;
    private String userId;

    private TopBar topBar;

    private ViewGroup messagesContainer;
    private ScrollView scrollContainer;
    private EditText msgTxt;
    private Button attachButton;
    private Button sendButton;
    private TextView messageText;
    private ImageView userAttach;
    private TextView meLabel;
    private TextView friendLabel;

    private ArrayList<String> incomeRoomMessages;

    private String dialogId;
    private String lastMsg;
    private QBChatRoom chatRoom;

    private String dialogFreezingStatus;

    private MessageManager msgManager;
    private ChatApplication app;

    //new arch

    private QBUser opponentUser;
    private byte previousActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        app = ChatApplication.getInstance();
        msgManager = app.getMsgManager();
        msgManager.setListDownloadedListener(this);
        initViews();

    }

    @Override
    public void onBackPressed() {


        if (previousActivity == GlobalConsts.DIALOG_ACTIVITY || previousActivity == GlobalConsts.CONTACTS_ACTIVITY) {
            msgManager.updateDialogLastMessage(lastMsg, dialogId);
        } else {
            app.getMsgManager().downloadPersistentRoom();
        }
        if (chatRoom != null) {
            chatRoom.removeMessageListener(app.getMsgManager());
            chatRoom.leave();
            app.setJoinedRoom(null);
        }

        this.finish();
    }

    private void initViews() {
        topBar = (TopBar) findViewById(R.id.top_bar);
        meLabel = (TextView) findViewById(R.id.meLabel);
        friendLabel = (TextView) findViewById(R.id.friendLabel);

        messagesContainer = (ViewGroup) findViewById(R.id.messagesContainer);

        scrollContainer = (ScrollView) findViewById(R.id.scrollContainer);
        msgTxt = (EditText) findViewById(R.id.messageEdit);
        sendButton = (Button) findViewById(R.id.chatSendButton);
        attachButton = (Button) findViewById(R.id.attachbutton);

        previousActivity = getIntent().getByteExtra(GlobalConsts.PREVIOUS_ACTIVITY, (byte) 0);

        switch (previousActivity) {


            case GlobalConsts.ROOM_ACTIVITY:

                QBChat.getInstance().startWatchRoom(pInvitationListener);
                String chatRoomName = getIntent().getStringExtra(GlobalConsts.ROOM_NAME);

                chatRoom = app.getJoinedRoom();
                chatRoom.addParticipantListener(pParticipantListener);
                app.getMsgManager().setmNewRoomMessageIncome(this);

                friendLabel.setText(getIntent().getStringExtra(GlobalConsts.ROOM_NAME));

                topBar.setFragmentParams(TopBar.ROOM_ACTIVITY, View.INVISIBLE, false);

                sendButton.setOnClickListener(onRoomSendBtnClick);
                attachButton.setVisibility(View.GONE);
                break;

            case GlobalConsts.DIALOG_ACTIVITY:

                userId = getIntent().getStringExtra(GlobalConsts.USER_ID);
                opponentUser = app.getDialogsUsersMap().get(userId);

                dialogId = getIntent().getStringExtra(GlobalConsts.DIALOG_ID);

                topBar.setFriendParams(opponentUser, app.getContactsMap().containsKey(String.valueOf(opponentUser.getId())));
                topBar.setFragmentParams(TopBar.CHAT_ACTIVITY, View.VISIBLE, true);

                msgManager.getDialogMessages(opponentUser.getId());
                msgManager.setNewMessageListener(this, opponentUser.getId());

                friendLabel.setText(opponentUser.getFullName() != null ? opponentUser.getFullName() : opponentUser.getLogin());
                sendButton.setOnClickListener(onDialogSendBtnClick);
                break;

            case GlobalConsts.CONTACTS_ACTIVITY:


                userId = getIntent().getStringExtra(GlobalConsts.USER_ID);
                opponentUser = app.getContactsMap().get(userId);

                if (app.getUserIdDialogIdMap().get(opponentUser.getId()) != null) {
                    dialogId = app.getUserIdDialogIdMap().get(opponentUser.getId()).getCustomObjectId();
                }

                topBar.setFriendParams(opponentUser, true);
                topBar.setFragmentParams(TopBar.CHAT_ACTIVITY, View.VISIBLE, true);

                msgManager.getDialogMessages(opponentUser.getId());
                msgManager.setNewMessageListener(this, opponentUser.getId());

                friendLabel.setText(opponentUser.getFullName() != null ? opponentUser.getFullName() : opponentUser.getLogin());
                sendButton.setOnClickListener(onDialogSendBtnClick);
                break;
        }


        attachButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        }

        );

        meLabel.setText(app.getQbUser().getFullName());

        if (app.getInviteUserList().size() > 1 && chatRoom != null) {
            chatRoom.invite(app.getInviteUserList());
            app.getInviteUserList().clear();
        }

    }

    public OnClickListener onDialogSendBtnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {

            lastMsg = msgTxt.getText().toString();
            msgTxt.setText("");
            showMessage(lastMsg, true);

            if (dialogId == null && dialogFreezingStatus == null) {
                msgManager.setDialogCreateListener(ChatActivity.this);
                msgManager.createDialog(opponentUser, false);
                app.getMsgManager().setLastHoldMessage(lastMsg);
                dialogFreezingStatus = "processed";
            }

            msgManager.sendSingleMessage(opponentUser.getId(), lastMsg, dialogId);

        }
    };

    public OnClickListener onRoomSendBtnClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            lastMsg = msgTxt.getText().toString();
            msgTxt.setText("");

            try {
                if (chatRoom != null) {
                    chatRoom.sendMessage(lastMsg);
                } else {
                    Toast.makeText(ChatActivity.this, getResources().getString(R.string.room_join_fall), Toast.LENGTH_LONG).show();
                }
            } catch (XMPPException e) {
                e.printStackTrace();
            }
        }
    };


    private void showMessage(String message, boolean leftSide) {

        if (message.length() > 12 && message.substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
            parts = message.split("#");

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            int bgRes = R.drawable.left_message_bg;

            if (!leftSide) {
                bgRes = R.drawable.right_message_bg;
                params.gravity = Gravity.RIGHT;
            }


            CustomPictureAttachListener attachViewListener = new CustomPictureAttachListener() {
                @Override
                public void onClick(View view) {
                    super.onClick(view);

                    Intent intent = new Intent(ChatActivity.this, AttachWiewActivity.class);
                    intent.putExtra(GlobalConsts.ATTACH_URL, getPictureUrl());
                    ChatActivity.this.startActivity(intent);
                }
            };

            userAttach = new ImageView(ChatActivity.this);
            userAttach.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.attach_preview_margin));
            userAttach.setMaxWidth(getResources().getDimensionPixelSize(R.dimen.attach_preview_margin));

            userAttach.setLayoutParams(params);
            userAttach.setBackgroundResource(bgRes);
            userAttach.setImageDrawable(getResources().getDrawable(R.drawable.com_facebook_profile_default_icon));
            userAttach.setOnClickListener(attachViewListener);
            attachViewListener.setPictureUrl(parts[1]);


            app.getPicManager().downloadPicAndDisplay(parts[1], userAttach, null);


            isAttach = true;
        } else {
            messageText = new TextView(ChatActivity.this);
            messageText.setTextColor(Color.BLACK);
            messageText.setText(message);

            int bgRes = R.drawable.left_message_bg;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            if (!leftSide) {
                bgRes = R.drawable.right_message_bg;
                params.gravity = Gravity.RIGHT;
            }

            messageText.setLayoutParams(params);
            messageText.setBackgroundResource(bgRes);

            isAttach = false;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isAttach) {
                    messagesContainer.addView(userAttach);
                } else {
                    messagesContainer.addView(messageText);
                }

            }
        });

        scrollDown();
    }

    private void roomShowMessage(ArrayList<String> messageQuery) {
        incomeRoomMessages = messageQuery;

        ChatActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (String visibleMessage : incomeRoomMessages) {

                    messageText = new TextView(ChatActivity.this);
                    messageText.setText(visibleMessage);
                    messagesContainer.addView(messageText);
                }
                scrollDown();
                incomeRoomMessages.clear();
                incomeRoomMessages.trimToSize();
            }
        });
    }

    public void applyDialogMessags(List<QBCustomObject> messageList) {


        for (QBCustomObject message : messageList) {

            int userId = Integer.parseInt(message.getFields().get("author_id").toString());

            if (userId == app.getQbUser().getId()) {
                showMessage(message.getFields().get(GlobalConsts.MSG_TEXT).toString(), true);
            } else {
                showMessage(message.getFields().get(GlobalConsts.MSG_TEXT).toString(), false);
            }
        }

    }

    private void scrollDown() {
        scrollContainer.post(new Runnable() {

            @Override
            public void run() {
                scrollContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

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

    @Override
    public void messageListDownloaded(List<QBCustomObject> downloadedList) {
        applyDialogMessags(downloadedList);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {

                        Toast.makeText(ChatActivity.this, getResources().getString(R.string.chat_activity_attach_info), Toast.LENGTH_LONG).show();
                        topBar.swichProgressBarVisibility(View.VISIBLE);

                        app.getQbm().setUploadListener(ChatActivity.this);
                        Bitmap yourSelectedImage = app.getPicManager().decodeUri(imageReturnedIntent.getData());
                        app.getQbm().uploadPic(app.getPicManager().convertBitmapToFile(yourSelectedImage), true);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    @Override
    public void uploadComplete(int uploafFileId, String picUrl) {
        String serviceMessage = "<Attach file>#" + picUrl;
        msgManager.sendSingleMessage(opponentUser.getId(), serviceMessage, dialogId);
        showMessage(serviceMessage, true);
        topBar.swichProgressBarVisibility(View.INVISIBLE);
    }

    @Override
    public void incomeNewMessage(String messageBody) {
        lastMsg = messageBody;
        showMessage(lastMsg, false);

    }

    @Override
    public void dialogCreate(int userId, String customObjectUid) {
        dialogId = String.valueOf(userId);
        dialogFreezingStatus = null;
        msgManager.setDialogCreateListener(null);
        if (lastMsg != null) {
            showMessage(lastMsg, false);
        }
    }

    @Override
    public void incomeMessagePool(final ArrayList<String> messagesPool) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomShowMessage(messagesPool);
            }
        });
    }
}