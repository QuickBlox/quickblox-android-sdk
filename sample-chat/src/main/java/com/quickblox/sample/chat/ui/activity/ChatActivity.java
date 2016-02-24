package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.AttachmentPreviewAdapter;
import com.quickblox.sample.chat.ui.adapter.ChatAdapter;
import com.quickblox.sample.chat.ui.widget.AttachmentPreviewAdapterView;
import com.quickblox.sample.chat.utils.chat.Chat;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.chat.GroupChatImpl;
import com.quickblox.sample.chat.utils.chat.PrivateChatImpl;
import com.quickblox.sample.chat.utils.chat.QBChatMessageListener;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
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
import java.util.Collection;
import java.util.Collections;

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

    private LinearLayout attachmentPreviewContainerLayout;

    private ChatAdapter chatAdapter;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;

    private Chat chat;
    private QBDialog qbDialog;

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
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (qbDialog != null) {
            outState.putSerializable(EXTRA_DIALOG, qbDialog);
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (qbDialog == null) {
            qbDialog = (QBDialog) savedInstanceState.getSerializable(EXTRA_DIALOG);
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

        MenuItem menuItem = menu.findItem(R.id.menu_chat_action_leave);
        if (qbDialog.getType() == QBDialogType.PRIVATE) {
            menuItem.setVisible(false);
        } else {
            menuItem.setVisible(true);
        }

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
            leaveGroupChat();
            return true;

        case R.id.menu_chat_action_delete:
            deleteChat();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void leaveGroupChat() {
        ((GroupChatImpl) chat).leaveChatRoom();
        ProgressDialogFragment.show(getSupportFragmentManager());
        ChatHelper.getInstance().leaveDialog(qbDialog, new QBEntityCallback<QBDialog>() {
            @Override
            public void onSuccess(QBDialog qbDialog, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                showErrorSnackbar(R.string.error_leave_chat, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveGroupChat();
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(
                        SelectUsersActivity.EXTRA_QB_USERS);

                updateDialog(selectedUsers);
            }
        }
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        switch (requestCode) {
        case REQUEST_CODE_ATTACHMENT:
            attachmentPreviewAdapter.add(file);
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

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_chat_messages);
    }

    public void onSendChatClick(View view) {
        int totalAttachmentsCount = attachmentPreviewAdapter.getCount();
        Collection<QBAttachment> uploadedAttachments = attachmentPreviewAdapter.getUploadedAttachments();
        if (!uploadedAttachments.isEmpty()) {
            if (uploadedAttachments.size() == totalAttachmentsCount) {
                for (QBAttachment attachment : uploadedAttachments) {
                    sendChatMessage(null, attachment);
                }
            } else {
                Toaster.shortToast(R.string.chat_wait_for_attachments_to_upload);
            }
        }

        String text = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }
    }

    public void onAttachmentsClick(View view) {
        new ImagePickHelper().pickAnImage(this, REQUEST_CODE_ATTACHMENT);
    }

    public void showMessage(QBChatMessage message) {
        if (chatAdapter != null) {
            chatAdapter.add(message);
            scrollMessageListDown();
        }
    }

    private void initViews() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        messagesListView = _findViewById(R.id.list_chat_messages);
        messageEditText = _findViewById(R.id.edit_chat_message);
        progressBar = _findViewById(R.id.progress_chat);
        attachmentPreviewContainerLayout = _findViewById(R.id.layout_attachment_preview_container);

        attachmentPreviewAdapter = new AttachmentPreviewAdapter(this,
                new AttachmentPreviewAdapter.OnAttachmentCountChangedListener() {
                    @Override
                    public void onAttachmentCountChanged(int count) {
                        attachmentPreviewContainerLayout.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    }
                });
        AttachmentPreviewAdapterView previewAdapterView = _findViewById(R.id.adapter_view_attachment_preview);
        previewAdapterView.setAdapter(attachmentPreviewAdapter);
    }

    private void sendChatMessage(String text, QBAttachment attachment) {
        QBChatMessage chatMessage = new QBChatMessage();
        if (attachment != null) {
            chatMessage.addAttachment(attachment);
        } else {
            chatMessage.setBody(text);
        }
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, "1");
        chatMessage.setDateSent(System.currentTimeMillis() / 1000);

        try {
            chat.sendMessage(chatMessage);

            if (qbDialog.getType() == QBDialogType.PRIVATE) {
                showMessage(chatMessage);
            }

            if (attachment != null) {
                attachmentPreviewAdapter.remove(attachment);
            } else {
                messageEditText.setText("");
            }
        } catch (XMPPException | SmackException e) {
            Log.e(TAG, "Failed to send a message", e);
            Toaster.shortToast(R.string.chat_send_message_error);
        }
    }

    private void initChat() {
        switch (qbDialog.getType()) {
        case GROUP:
        case PUBLIC_GROUP:
            chat = new GroupChatImpl(chatMessageListener);
            joinGroupChat();
            break;

        case PRIVATE:
            chat = new PrivateChatImpl(chatMessageListener, QbDialogUtils.getOpponentIdForPrivateDialog(qbDialog));
            loadDialogUsers();
            break;

        default:
            Toaster.shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type), qbDialog.getType().name()));
            finish();
            break;
        }
    }

    private void joinGroupChat() {
        progressBar.setVisibility(View.VISIBLE);

        ((GroupChatImpl) chat).joinGroupChat(qbDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                loadDialogUsers();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                showErrorSnackbar(R.string.chat_join_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        joinGroupChat();
                    }
                });
            }
        });
    }

    private void leaveGroupChatRoom() {
        if (chat != null) {
            ((GroupChatImpl) chat).leaveChatRoom();
        }
    }

    private void releaseChat() {
        try {
            if (chat != null) {
                chat.release();
            }
        } catch (XMPPException e) {
            Log.e(TAG, "Failed to release chat", e);
        }
    }

    private void updateDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        qbDialog = dialog;
                        loadDialogUsers();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        showErrorSnackbar(R.string.chat_info_add_people_error, e,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        updateDialog(selectedUsers);
                                    }
                                });
                    }
                }
        );
    }

    private void loadDialogUsers() {
        ChatHelper.getInstance().getUsersFromDialog(qbDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
                loadChatHistory();
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.chat_load_users_error, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadDialogUsers();
                            }
                        });
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
        ChatHelper.getInstance().loadChatHistory(qbDialog, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);

                chatAdapter = new ChatAdapter(ChatActivity.this, messages);
                chatAdapter.setOnItemInfoExpandedListener(new ChatAdapter.OnItemInfoExpandedListener() {
                    @Override
                    public void onItemInfoExpanded(final int position) {
                        if (isLastItem(position)) {
                            // HACK need to allow info textview visibility change so posting it via handler
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    messagesListView.setSelection(position);
                                }
                            });
                        } else {
                            messagesListView.smoothScrollToPosition(position);
                        }
                    }

                    private boolean isLastItem(int position) {
                        return position == chatAdapter.getCount() - 1;
                    }
                });
                messagesListView.setAdapter(chatAdapter);
                messagesListView.setAreHeadersSticky(false);
                messagesListView.setDivider(null);
                scrollMessageListDown();

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                showErrorSnackbar(R.string.chat_load_history_error, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadChatHistory();
                            }
                        });
            }
        });
    }

    private void scrollMessageListDown() {
        messagesListView.setSelection(messagesListView.getCount() - 1);
    }

    private void deleteChat() {
        ChatHelper.getInstance().deleteDialog(qbDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.dialogs_deletion_error, e,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                deleteChat();
                            }
                        });
            }
        });
    }

    private QBChatMessageListener chatMessageListener = new QBChatMessageListener() {
        @Override
        public void onQBChatMessageReceived(QBChat chat, QBChatMessage message) {
            showMessage(message);
        }
    };

    private ConnectionListener chatConnectionListener = new VerboseQbChatConnectionListener() {
        @Override
        public void connectionClosedOnError(final Exception e) {
            super.connectionClosedOnError(e);

            // Leave active room if we're in Group Chat
            if (qbDialog.getType() == QBDialogType.GROUP) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaveGroupChatRoom();
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
