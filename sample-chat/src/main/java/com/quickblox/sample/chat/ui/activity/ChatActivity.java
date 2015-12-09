package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.ChatAdapter;
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.sample.chat.utils.chat.Chat;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.GroupChatImpl;
import com.quickblox.sample.chat.utils.chat.PrivateChatImpl;
import com.quickblox.sample.chat.utils.chat.VerboseQbChatConnectionListener;
import com.quickblox.users.model.QBUser;

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

    private KeyboardHandleRelativeLayout keyboardHandleLayout;
    private RelativeLayout containerLayout;
    private FrameLayout stickersContainerLayout;

    private ProgressBar progressBar;
    private ListView messagesListView;
    private EditText messageEditText;
    private ImageButton stickerImageButton;

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
        if (isStickersContainerVisible()) {
            setStickersContainerVisible(false);
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else {
            releaseChat();
            super.onBackPressed();
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            initChat();
        }
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean isKeyboardVisible) {
        if (isKeyboardVisible) {
            setStickersContainerVisible(false);
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else if (isStickersContainerVisible()) {
            stickerImageButton.setImageResource(R.drawable.ic_action_keyboard);
        } else {
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.menu_action_info:
            // TODO Open chat info activity
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void onSendChatClick(View view) {
        String text = messageEditText.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text);
        }
    }

    public void onStickersClick(View view) {
        if (isStickersContainerVisible()) {
            showKeyboard();
            stickerImageButton.setImageResource(R.drawable.ic_action_insert_emoticon);
        } else if (keyboardHandleLayout.isKeyboardVisible()) {
            keyboardHandleLayout.hideKeyboard(this, new KeyboardHandleRelativeLayout.KeyboardHideCallback() {
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

    public void showMessage(QBChatMessage message) {
        adapter.add(message);
        scrollMessageListDown();
    }

    private void setChatNameToActionBar() {
        List<QBUser> chatUsers = ChatHelper.getInstance().getUsersByIds(dialog.getOccupants());
        String chatName = ChatUtils.createChatNameFromUserList(chatUsers);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(chatName);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    private void initViews() {
        messagesListView = (ListView) findViewById(R.id.list_chat_messages);
        messageEditText = (EditText) findViewById(R.id.edit_chat_message);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat);
        containerLayout = (RelativeLayout) findViewById(R.id.layout_container);
        keyboardHandleLayout = (KeyboardHandleRelativeLayout) findViewById(R.id.layout_keyboard_notifier);
        stickersContainerLayout = (FrameLayout) findViewById(R.id.layout_chat_stickers_container);
        stickerImageButton = (ImageButton) findViewById(R.id.button_chat_stickers);

        keyboardHandleLayout.setKeyboardSizeChangeListener(this);

        String fragmentTag = StickersFragment.class.getSimpleName();
        StickersFragment stickersFragment = (StickersFragment) getSupportFragmentManager().findFragmentByTag(fragmentTag);
        if (stickersFragment == null) {
            stickersFragment = new StickersFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.layout_chat_stickers_container, stickersFragment, fragmentTag)
                    .commit();
        }

        stickersFragment.setOnStickerSelectedListener(stickerSelectedListener);
        stickersFragment.setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked() {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                messageEditText.dispatchKeyEvent(event);
            }
        });

        setStickersContainerVisible(isStickersContainerVisible());
        updateStickersContainerParams();
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(messageEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void sendChatMessage(String text) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(text);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        try {
            chat.sendMessage(chatMessage);
            messageEditText.setText("");
            if (dialog.getType() == QBDialogType.PRIVATE) {
                showMessage(chatMessage);
            }
        } catch (XMPPException | SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
            Toast.makeText(this, R.string.chat_send_message_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void setStickersContainerVisible(boolean isVisible) {
        stickersContainerLayout.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        int keyboardHeight = KeyboardUtils.getKeyboardHeight();
        if (stickersContainerLayout.getHeight() != keyboardHeight) {
            updateStickersContainerParams();
        }

        final int bottomPadding = isVisible ? keyboardHeight : 0;
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
            scrollMessageListDown();
        }
    }

    private boolean isStickersContainerVisible() {
        return stickersContainerLayout.getVisibility() == View.VISIBLE;
    }

    private void updateStickersContainerParams() {
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) stickersContainerLayout.getLayoutParams();
        lp.height = KeyboardUtils.getKeyboardHeight();
        stickersContainerLayout.setLayoutParams(lp);
    }

    public void setContentBottomPadding(int padding) {
        int leftPadding = containerLayout.getPaddingLeft();
        int topPadding = containerLayout.getPaddingTop();
        int rightPadding = containerLayout.getPaddingRight();
        containerLayout.setPadding(leftPadding, topPadding, rightPadding, padding);
    }

    private void initChat() {
        switch (dialog.getType()) {
        case GROUP:
            chat = new GroupChatImpl(this);
            joinGroupChat();
            break;

        case PRIVATE:
            chat = new PrivateChatImpl(this, ChatUtils.getOpponentIdForPrivateDialog(dialog));
            loadDialogUsers();
            break;
        }
    }

    private void joinGroupChat() {
        Toast.makeText(this, R.string.chat_joining_room, Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.VISIBLE);

        ((GroupChatImpl) chat).joinGroupChat(dialog, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                Toast.makeText(ChatActivity.this, R.string.chat_join_successful, Toast.LENGTH_SHORT).show();
                loadDialogUsers();
            }

            @Override
            public void onError(List<String> errors) {
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(ChatActivity.this, getString(R.string.chat_join_error), errors);
            }
        });
    }

    private void leaveGroupChat() {
        ((GroupChatImpl) chat).leave();
    }

    private void releaseChat() {
        try {
            chat.release();
        } catch (XMPPException e) {
            Log.e(TAG, "Failed to release chat", e);
        }
    }

    private void loadDialogUsers() {
        ChatHelper.getInstance().getUsersFromDialog(dialog, new QBEntityCallbackImpl<List<QBUser>>() {
            @Override
            public void onSuccess(List<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
                loadChatHistory();
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(ChatActivity.this, getString(R.string.chat_load_users_error), errors);
            }
        });
    }

    private void loadChatHistory() {
        ChatHelper.getInstance().loadChatHistory(dialog, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
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
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(ChatActivity.this, getString(R.string.chat_load_history_error), errors);
            }
        });
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
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

    private ConnectionListener chatConnectionListener = new VerboseQbChatConnectionListener() {
        @Override
        public void connectionClosedOnError(final Exception e) {
            super.connectionClosedOnError(e);

            // Leave active room if we're in Group Chat
            if (dialog.getType() == QBDialogType.GROUP) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaveGroupChat();
                    }
                });
            }
        }

        @Override
        public void reconnectionSuccessful() {
            super.reconnectionSuccessful();

            // Join active room if we're in Group Chat
            if (dialog.getType() == QBDialogType.GROUP) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        joinGroupChat();
                    }
                });
            }
        }
    };
}
