package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.ChatAdapter;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.sample.chat.utils.chat.Chat;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.GroupChatImpl;
import com.quickblox.sample.chat.utils.chat.PrivateChatImpl;
import com.quickblox.sample.chat.utils.chat.VerboseQbChatConnectionListener;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vc908.stickerfactory.StickersManager;
import vc908.stickerfactory.ui.OnEmojiBackspaceClickListener;
import vc908.stickerfactory.ui.OnStickerSelectedListener;
import vc908.stickerfactory.ui.fragment.StickersFragment;
import vc908.stickerfactory.ui.view.KeyboardHandleRelativeLayout;
import vc908.stickerfactory.utils.KeyboardUtils;

public class ChatActivity extends BaseActivity implements KeyboardHandleRelativeLayout.KeyboardSizeChangeListener {

    private static final String TAG = ChatActivity.class.getSimpleName();

    private static final String EXTRA_DIALOG = "dialog";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private EditText messageEditText;
    private ListView messagesListView;
    private ProgressBar progressBar;
    private KeyboardHandleRelativeLayout keyboardHandleLayout;
    private View stickersContainer;
    private ImageButton stickerImageButton;
    private RelativeLayout container;

    private ChatAdapter adapter;

    private Chat chat;
    private QBDialog dialog;

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dialog = (QBDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);
        initViews();

        if (isSessionActive()) {
            initChat();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (isStickersFrameVisible()) {
            setStickersContainerVisible(false);
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            try {
                chat.release();
            } catch (XMPPException e) {
                Log.e(TAG, "Failed to release chat", e);
            }
            super.onBackPressed();
        }
    }

    private void initViews() {
        TextView companionLabel = (TextView) findViewById(R.id.text_chat_companion);
        messagesListView = (ListView) findViewById(R.id.list_chat_messages);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat);
        container = (RelativeLayout) findViewById(R.id.container);

        if (dialog.getType() == QBDialogType.GROUP) {
            TextView meLabel = (TextView) findViewById(R.id.text_chat_me);
            container.removeView(meLabel);
            container.removeView(companionLabel);
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatHelper.getInstance().getOpponentIdForPrivateDialog(dialog);
            companionLabel.setText(ChatHelper.getInstance().getDialogsUsersMap().get(opponentID).getLogin());
        }

        // Send button
        Button sendButton = (Button) findViewById(R.id.button_chat_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (!TextUtils.isEmpty(messageText)) {
                    sendChatMessage(messageText);
                }
            }
        });

        keyboardHandleLayout = (KeyboardHandleRelativeLayout) findViewById(R.id.layout_keyboard_notifier);
        keyboardHandleLayout.listener = this;
        stickersContainer = findViewById(R.id.layout_chat_stickers_container);
        stickerImageButton = (ImageButton) findViewById(R.id.button_chat_stickers);
        stickerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStickersFrameVisible()) {
                    showKeyboard();
                    stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
                } else {
                    if (keyboardHandleLayout.isKeyboardVisible()) {
                        keyboardHandleLayout.hideKeyboard(ChatActivity.this, new KeyboardHandleRelativeLayout.OnKeyboardHideCallback() {
                            @Override
                            public void onKeyboardHide() {
                                stickerImageButton.setImageResource(R.drawable.ic_action_keyboard);
                                setStickersContainerVisible(true);
                            }
                        });
                    } else {
                        stickerImageButton.setImageResource(R.drawable.ic_action_keyboard);
                        setStickersContainerVisible(true);
                    }
                }
            }
        });

        updateStickersContainerParams();

        String stickerFragmentTag = StickersFragment.class.getSimpleName();
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager()
                .findFragmentByTag(stickerFragmentTag);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment.Builder()
                    .setStickerPlaceholderColorFilterRes(android.R.color.darker_gray)
                    .build();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_chat_stickers_container, stickersFragment, stickerFragmentTag)
                    .commit();
        }
        stickersFragment.setOnStickerSelectedListener(stickerSelectedListener);
        stickersFragment.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked() {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                messageEditText.dispatchKeyEvent(event);
            }
        });
        setStickersContainerVisible(isStickersFrameVisible());
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendChatMessage(String messageText) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(messageText);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        try {
            chat.sendMessage(chatMessage);
        } catch (XMPPException | SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
        }

        messageEditText.setText("");

        if (dialog.getType() == QBDialogType.PRIVATE) {
            showMessage(chatMessage);
        }
    }

    private OnStickerSelectedListener stickerSelectedListener = new OnStickerSelectedListener() {
        @Override
        public void onStickerSelected(String code) {
            if (StickersManager.isSticker(code)) {
                sendChatMessage(code);
            } else {
                messageEditText.append(code);
            }
        }
    };

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            setStickersContainerVisible(false);
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            if (isStickersFrameVisible()) {
                stickerImageButton.setImageResource(R.drawable.ic_action_keyboard);
            } else {
                stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
            }
        }
    }

    private void setStickersContainerVisible(boolean isVisible) {
        stickersContainer.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        if (stickersContainer.getHeight() != KeyboardUtils.getKeyboardHeight()) {
            updateStickersContainerParams();
        }

        final int bottomPadding = isVisible ? KeyboardUtils.getKeyboardHeight() : 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            keyboardHandleLayout.post(new Runnable() {
                @Override
                public void run() {
                    setContentBottomPadding(bottomPadding);
                    scrollMessageListDown();
                }
            });
        } else {
            setContentBottomPadding(bottomPadding);
        }
        scrollMessageListDown();
    }

    private boolean isStickersFrameVisible() {
        return stickersContainer.getVisibility() == View.VISIBLE;
    }

    private void updateStickersContainerParams() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) stickersContainer.getLayoutParams();
        lp.height = KeyboardUtils.getKeyboardHeight();
        stickersContainer.setLayoutParams(lp);
    }

    public void setContentBottomPadding(int padding) {
        int leftPadding = container.getPaddingLeft();
        int topPadding = container.getPaddingTop();
        int rightPadding = container.getPaddingRight();
        container.setPadding(leftPadding, topPadding, rightPadding, padding);
    }

    private void initChat() {
        if (dialog.getType() == QBDialogType.GROUP) {
            chat = new GroupChatImpl(this);

            progressBar.setVisibility(View.VISIBLE);
            joinGroupChat();
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentId = ChatHelper.getInstance().getOpponentIdForPrivateDialog(dialog);
            chat = new PrivateChatImpl(this, opponentId);
            loadChatHistory();
        }
    }

    private void joinGroupChat() {
        ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                loadChatHistory();
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(ChatActivity.this, "Error join group chat: ", errors);
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder,
                new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                        // The newest messages should be in the end of list,
                        // so we need to reverse list to show messages in the right order
                        Collections.reverse(messages);

                        adapter = new ChatAdapter(ChatActivity.this, messages);
                        messagesListView.setAdapter(adapter);
                        scrollMessageListDown();

                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        if (!ChatActivity.this.isFinishing()) {
                            ErrorUtils.showErrorDialog(ChatActivity.this, "Load chat history errors: ", errors);
                        }
                    }
                });
    }

    public void showMessage(QBChatMessage message) {
        adapter.add(message);
        scrollMessageListDown();
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private ConnectionListener chatConnectionListener = new VerboseQbChatConnectionListener() {

        @Override
        public void connectionClosedOnError(final Exception e) {
            super.connectionClosedOnError(e);

            // leave active room
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((GroupChatImpl) chat).leave();
                    }
                });
            }
        }

        @Override
        public void reconnectionSuccessful() {
            super.reconnectionSuccessful();

            // Join active room
            if (dialog.getType() == QBDialogType.GROUP) {
                ChatActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupChat();
                    }
                });
            }
        }
    };

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            initChat();
        }
    }
}
