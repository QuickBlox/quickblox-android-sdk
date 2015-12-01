package com.quickblox.sample.chat.ui.activity;

import android.app.AlertDialog;
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
import android.widget.ImageView;
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
import com.quickblox.sample.chat.utils.chat.Chat;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.GroupChatImpl;
import com.quickblox.sample.chat.utils.chat.PrivateChatImpl;
import com.quickblox.sample.chat.utils.chat.VerboseQbChatConnectionListener;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.Date;
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
    private ListView messagesContainer;
    private ProgressBar progressBar;
    private KeyboardHandleRelativeLayout keyboardHandleLayout;
    private View stickersFrame;
    private ImageView stickerButton;
    private RelativeLayout container;

    private ChatAdapter adapter;

    private Chat chat;
    private QBDialog dialog;

    private boolean isStickersFrameVisible;

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initViews();

        // Init chat if the session is active
        if (isSessionActive()) {
            initChat();
        }

        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (isStickersFrameVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            try {
                chat.release();
            } catch (XMPPException e) {
                Log.e(TAG, "Failed to release chat", e);
            }
            super.onBackPressed();

            Intent i = new Intent(ChatActivity.this, DialogsActivity.class);
            startActivity(i);
            finish();
        }
    }

    private void initViews() {
        messagesContainer = (ListView) findViewById(R.id.messagesContainer);
        messageEditText = (EditText) findViewById(R.id.messageEdit);
        progressBar = (ProgressBar) findViewById(R.id.progress_dialogs);
        TextView companionLabel = (TextView) findViewById(R.id.companionLabel);

        // Setup opponents info
        //
        Intent intent = getIntent();
        dialog = (QBDialog) intent.getSerializableExtra(EXTRA_DIALOG);
        container = (RelativeLayout) findViewById(R.id.container);
        if (dialog.getType() == QBDialogType.GROUP) {
            TextView meLabel = (TextView) findViewById(R.id.meLabel);
            container.removeView(meLabel);
            container.removeView(companionLabel);
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatHelper.getInstance().getOpponentIdForPrivateDialog(dialog);
            companionLabel.setText(ChatHelper.getInstance().getDialogsUsersMap().get(opponentID).getLogin());
        }

        // Send button
        Button sendButton = (Button) findViewById(R.id.chatSendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String messageText = messageEditText.getText().toString();
                if (TextUtils.isEmpty(messageText)) {
                    return;
                }
                sendChatMessage(messageText);

            }
        });

        // Stickers
        keyboardHandleLayout = (KeyboardHandleRelativeLayout) findViewById(R.id.sizeNotifierLayout);
        keyboardHandleLayout.listener = this;
        stickersFrame = findViewById(R.id.frame);
        stickerButton = (ImageView) findViewById(R.id.stickers_button);

        stickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isStickersFrameVisible) {
                    showKeyboard();
                    stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
                } else {
                    if (keyboardHandleLayout.isKeyboardVisible()) {
                        keyboardHandleLayout.hideKeyboard(ChatActivity.this, new KeyboardHandleRelativeLayout.OnKeyboardHideCallback() {
                            @Override
                            public void onKeyboardHide() {
                                stickerButton.setImageResource(R.drawable.ic_action_keyboard);
                                setStickersFrameVisible(true);
                            }
                        });
                    } else {
                        stickerButton.setImageResource(R.drawable.ic_action_keyboard);
                        setStickersFrameVisible(true);
                    }
                }
            }
        });

        updateStickersFrameParams();
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager().findFragmentById(R.id.frame);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment.Builder()
                    .setStickerPlaceholderColorFilterRes(android.R.color.darker_gray)
                    .build();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, stickersFragment)
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
        setStickersFrameVisible(isStickersFrameVisible);
    }

    private void showKeyboard() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendChatMessage(String messageText) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(messageText);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(new Date().getTime() / 1000);

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
//                setStickersFrameVisible(false);
            } else {
                // append emoji to edit
                messageEditText.append(code);
            }
        }
    };

    @Override
    public void onKeyboardVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            setStickersFrameVisible(false);
            stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            if (isStickersFrameVisible) {
                stickerButton.setImageResource(R.drawable.ic_action_keyboard);
            } else {
                stickerButton.setImageResource(R.drawable.ic_action_insert_emoticon);
            }
        }
    }

    private void setStickersFrameVisible(final boolean isVisible) {
        stickersFrame.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        isStickersFrameVisible = isVisible;
        if (stickersFrame.getHeight() != KeyboardUtils.getKeyboardHeight()) {
            updateStickersFrameParams();
        }
        final int padding = isVisible ? KeyboardUtils.getKeyboardHeight() : 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            keyboardHandleLayout.post(new Runnable() {
                @Override
                public void run() {
                    setContentBottomPadding(padding);
                    scrollDown();
                }
            });
        } else {
            setContentBottomPadding(padding);
        }
        scrollDown();
    }

    private void updateStickersFrameParams() {
        stickersFrame.getLayoutParams().height = vc908.stickerfactory.utils.KeyboardUtils.getKeyboardHeight();
    }

    public void setContentBottomPadding(int padding) {
        container.setPadding(0, 0, 0, padding);
    }

    private void initChat() {
        if (dialog.getType() == QBDialogType.GROUP) {
            chat = new GroupChatImpl(this);

            progressBar.setVisibility(View.VISIBLE);
            joinGroupChat();
        } else if (dialog.getType() == QBDialogType.PRIVATE) {
            Integer opponentID = ChatHelper.getInstance().getOpponentIdForPrivateDialog(dialog);
            chat = new PrivateChatImpl(this, opponentID);
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
            public void onError(List<String> list) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                dialog.setMessage("error when join group chat: " + list.toString()).create().show();
            }
        });
    }

    private void loadChatHistory() {
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(100);
        customObjectRequestBuilder.sortDesc("date_sent");

        QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {

                adapter = new ChatAdapter(ChatActivity.this, new ArrayList<QBChatMessage>());
                messagesContainer.setAdapter(adapter);

                for (int i = messages.size() - 1; i >= 0; --i) {
                    QBChatMessage msg = messages.get(i);
                    showMessage(msg);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                if (!ChatActivity.this.isFinishing()) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
                    dialog.setMessage("load chat history errors: " + errors).create().show();
                }
            }
        });
    }

    public void showMessage(QBChatMessage message) {
        adapter.add(message);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                scrollDown();
            }
        });
    }

    private void scrollDown() {
        messagesContainer.setSelection(messagesContainer.getCount() - 1);
    }


    ConnectionListener chatConnectionListener = new VerboseQbChatConnectionListener() {

        @Override
        public void connectionClosedOnError(final Exception e) {
            Log.i(TAG, "connectionClosedOnError: " + e.getLocalizedMessage());

            // leave active room
            //
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
            Log.i(TAG, "reconnectionSuccessful");

            // Join active room
            //
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


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onSessionRecreationFinish(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    initChat();
                }
            }
        });
    }
}
