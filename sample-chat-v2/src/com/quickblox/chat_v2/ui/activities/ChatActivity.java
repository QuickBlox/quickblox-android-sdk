package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.core.CustomPictureAttachListener;
import com.quickblox.chat_v2.ui.activities.chat.Chat;
import com.quickblox.chat_v2.ui.activities.chat.RoomChat;
import com.quickblox.chat_v2.ui.activities.chat.SingleChat;
import com.quickblox.chat_v2.ui.activities.chat.UIChat;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.users.model.QBUser;

import java.util.List;

public class ChatActivity extends Activity implements UIChat {

    private final int SELECT_PHOTO = 2;
    private boolean isAttach;
    private String[] parts;

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

    private List<String> incomeRoomMessages;

    private ChatApplication app;

    @Override
    public void setBarTitle(String pTitle) {
        friendLabel.setText(pTitle);
    }

    @Override
    public void setTopBarParams(String s, int v, boolean b) {
        topBar.setFragmentParams(s, v, b);
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showMessages(List<String> pList) {
        roomShowMessage(pList);
    }

    @Override
    public void setTopBarFriendParams(QBUser pOpponentUser, boolean b) {
        topBar.setFriendParams(pOpponentUser, b);
    }

    @Override
    public void changeUploadState(boolean b) {
        topBar.swichProgressBarVisibility(b ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showMessage(String pLastMsg, boolean b) {
        showMessageUI(pLastMsg, b);
    }

    private enum Mode {SINGLE, GROUP}

    private Mode mMode = Mode.SINGLE;

    private Chat mChat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        app = ChatApplication.getInstance();

        initViews();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mChat.registerListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mChat.unRegisterListeners();
    }

    @Override
    public void onBackPressed() {
        mChat.release();
        super.onBackPressed();
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

        byte previousActivity = getIntent().getByteExtra(GlobalConsts.PREVIOUS_ACTIVITY, (byte) 0);

        switch (previousActivity) {
            case GlobalConsts.ROOM_ACTIVITY:
                mMode = Mode.GROUP;
                mChat = new RoomChat(this);
                break;

            case GlobalConsts.DIALOG_ACTIVITY:
                mMode = Mode.SINGLE;
                mChat = new SingleChat(this, false);
                break;
            case GlobalConsts.CONTACTS_ACTIVITY:
                mMode = Mode.SINGLE;
                mChat = new SingleChat(this, true);
                break;
        }

        if (mMode == Mode.SINGLE) {
            attachButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                }
            });
        } else {
            attachButton.setVisibility(View.GONE);
        }


        sendButton.setOnClickListener(onSendClick);
        meLabel.setText(app.getQbUser().getFullName());
    }

    private OnClickListener onSendClick = new OnClickListener() {

        @Override
        public void onClick(View v) {

            String lastMsg = msgTxt.getText().toString();
            if (TextUtils.isEmpty(lastMsg)) {
                return;
            }

            msgTxt.setText("");

            mChat.sendMessage(lastMsg);

            if (mMode == Mode.SINGLE) {
                showMessageUI(lastMsg, true);
            }
        }
    };

    private void showMessageUI(String message, boolean leftSide) {

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

                    Intent intent = new Intent(ChatActivity.this, AttachViewActivity.class);
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

    private void roomShowMessage(List<String> messageQuery) {
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
            }
        });
    }

    private void scrollDown() {
        scrollContainer.post(new Runnable() {

            @Override
            public void run() {
                scrollContainer.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (mChat instanceof SingleChat) {
                        ((SingleChat) mChat).uploadAttach(imageReturnedIntent.getData());
                    }
                }
        }
    }


}