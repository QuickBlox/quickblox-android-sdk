package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.ChatAdapter;
import com.quickblox.sample.chat.utils.chat.Chat;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.GroupChatImpl;
import com.quickblox.sample.chat.utils.chat.PrivateChatImpl;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.imagepick.ImagePickHelper;
import com.quickblox.sample.core.utils.imagepick.OnImagePickedListener;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class ChatActivity extends BaseActivity implements OnImagePickedListener {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int REQUEST_CODE_ATTACHMENT = 721;
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;

    private static final String EXTRA_DIALOG = "dialog";
    private static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    private ProgressBar progressBar;
    private StickyListHeadersListView messagesListView;
    private EditText messageEditText;

    private ChatAdapter adapter;

    private Chat chat;
    private QBDialog qbDialog;
    private File attachmentFile;

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        qbDialog = (QBDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);
        initViews();
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
        releaseChat();
        super.onBackPressed();
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            initChat();
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
            case R.id.menu_chat_action_info:
                ChatInfoActivity.start(this, qbDialog);
                return true;

            case R.id.menu_chat_action_add:
                SelectUsersActivity.startForResult(this, REQUEST_CODE_SELECT_PEOPLE, qbDialog);
                return true;

            case R.id.menu_chat_action_leave:
                // TODO Add leaving
                return true;

            case R.id.menu_chat_action_delete:
                ChatHelper.getInstance().deleteDialog(qbDialog, new QBEntityCallbackImpl<Void>() {
                    @Override
                    public void onSuccess() {
                        finish();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        ErrorUtils.showErrorDialog(ChatActivity.this, R.string.dialogs_deletion_error, errors);
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(
                        SelectUsersActivity.EXTRA_QB_USERS);

                ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                        new QBEntityCallbackImpl<QBDialog>() {
                            @Override
                            public void onSuccess(QBDialog dialog, Bundle args) {
                                qbDialog = dialog;
                                loadDialogUsers();
                            }

                            @Override
                            public void onError(List<String> errors) {
                                ErrorUtils.showErrorDialog(ChatActivity.this, R.string.chat_info_add_people_error, errors);
                            }
                        }
                );
            }
        }
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        switch (requestCode) {
        case REQUEST_CODE_ATTACHMENT:
            attachmentFile = file;
            // TODO Show attachment preview
            break;
        }

    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        ErrorUtils.showErrorDialog(this, R.string.chat_attachment_error, e.toString());
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        // ignore
    }

    public void onSendChatClick(View view) {
        final String text = messageEditText.getText().toString();

        if (attachmentFile != null) {
            ChatHelper.getInstance().loadFileAsAttachment(attachmentFile, new QBEntityCallbackImpl<QBAttachment>() {
                @Override
                public void onSuccess(QBAttachment result, Bundle params) {
                    sendChatMessage(text, result);
                }

                @Override
                public void onError(List<String> errors) {
                    ErrorUtils.showErrorDialog(ChatActivity.this, R.string.chat_attachment_error, errors);
                }
            });
        } else if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }
    }

    public void onAttachmentsClick(View view) {
        new ImagePickHelper().pickAnImage(this, REQUEST_CODE_ATTACHMENT);
    }

    public void showMessage(QBChatMessage message) {
        adapter.add(message);
        scrollMessageListDown();
    }

    private void initViews() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        messagesListView = _findViewById(R.id.list_chat_messages);
        messageEditText = _findViewById(R.id.edit_chat_message);
        progressBar = _findViewById(R.id.progress_chat);
    }

    private void sendChatMessage(String text, QBAttachment attachment) {
        QBChatMessage chatMessage = new QBChatMessage();
        if (attachment != null) {
            chatMessage.addAttachment(attachment);
        }
        chatMessage.setBody(text);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        try {
            chat.sendMessage(chatMessage);
            messageEditText.setText("");
            attachmentFile = null;
            if (qbDialog.getType() == QBDialogType.PRIVATE) {
                showMessage(chatMessage);
            }
        } catch (XMPPException | SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
            Toaster.shortToast(R.string.chat_send_message_error);
        }
    }

    private void initChat() {
        switch (qbDialog.getType()) {
            case GROUP:
                chat = new GroupChatImpl(this);
                joinGroupChat();
                break;

            case PRIVATE:
                chat = new PrivateChatImpl(this, QbDialogUtils.getOpponentIdForPrivateDialog(qbDialog));
                loadDialogUsers();
                break;
        }
    }

    private void joinGroupChat() {
        Toaster.shortToast(R.string.chat_joining_room);
        progressBar.setVisibility(View.VISIBLE);

        ((GroupChatImpl) chat).joinGroupChat(qbDialog, new QBEntityCallbackImpl<String>() {
            @Override
            public void onSuccess() {
                Toaster.shortToast(R.string.chat_join_successful);
                loadDialogUsers();
            }

            @Override
            public void onError(List<String> errors) {
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(ChatActivity.this, R.string.chat_join_error, errors);
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
        ChatHelper.getInstance().getUsersFromDialog(qbDialog, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
                loadChatHistory();
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(ChatActivity.this, R.string.chat_load_users_error, errors);
            }
        });
    }

    private void setChatNameToActionBar() {
        String chatName = QbDialogUtils.getDialogName(qbDialog);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(chatName);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    private void loadChatHistory() {
        ChatHelper.getInstance().loadChatHistory(qbDialog, new QBEntityCallbackImpl<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);

                adapter = new ChatAdapter(ChatActivity.this, messages);
                messagesListView.setAdapter(adapter);
                messagesListView.setAreHeadersSticky(false);
                messagesListView.setDivider(null);
                scrollMessageListDown();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(ChatActivity.this, R.string.chat_load_history_error, errors);
            }
        });
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private ConnectionListener chatConnectionListener = new VerboseQbChatConnectionListener() {
        @Override
        public void connectionClosedOnError(final Exception e) {
            super.connectionClosedOnError(e);

            // Leave active room if we're in Group Chat
            if (qbDialog.getType() == QBDialogType.GROUP) {
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
            if (qbDialog.getType() == QBDialogType.GROUP) {
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
