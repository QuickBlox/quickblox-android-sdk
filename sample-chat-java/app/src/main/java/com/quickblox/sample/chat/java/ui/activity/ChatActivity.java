package com.quickblox.sample.chat.java.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBMessageStatusesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBMessageStatusListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.managers.DialogsManager;
import com.quickblox.sample.chat.java.ui.adapter.AttachmentPreviewAdapter;
import com.quickblox.sample.chat.java.ui.adapter.ChatAdapter;
import com.quickblox.sample.chat.java.ui.adapter.listeners.AttachClickListener;
import com.quickblox.sample.chat.java.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.chat.java.ui.widget.AttachmentPreviewAdapterView;
import com.quickblox.sample.chat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.chat.java.utils.SystemPermissionHelper;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.imagepick.ImagePickHelper;
import com.quickblox.sample.chat.java.utils.imagepick.OnImagePickedListener;
import com.quickblox.sample.chat.java.utils.qb.PaginationHistoryListener;
import com.quickblox.sample.chat.java.utils.qb.QbChatDialogMessageListenerImp;
import com.quickblox.sample.chat.java.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.java.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.java.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.DiscussionHistory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatActivity extends BaseActivity implements OnImagePickedListener, QBMessageStatusListener, DialogsManager.ManagingDialogsCallbacks {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int MAX_UPLOAD_FILES = 1;
    private static final int REQUEST_CODE_ATTACHMENT = 721;
    private static final int REQUEST_CODE_SELECT_PEOPLE = 752;

    private static final String ORDER_RULE = "order";
    private static final String ORDER_VALUE = "desc string created_at";
    private static final int LIMIT_PER_PAGE = 100;

    public static final String EXTRA_DIALOG_ID = "dialogId";
    public static final String EXTRA_IS_NEW_DIALOG = "isNewDialog";

    private ProgressBar progressBar;
    private EditText messageEditText;

    private LinearLayout attachmentPreviewContainerLayout;

    private ChatAdapter chatAdapter;
    private RecyclerView chatMessagesRecyclerView;
    protected List<QBChatMessage> messagesList;
    private AttachmentPreviewAdapter attachmentPreviewAdapter;
    private ConnectionListener chatConnectionListener;
    private ImageAttachClickListener imageAttachClickListener;
    private QBMessageStatusesManager qbMessageStatusesManager;
    private DialogsManager dialogsManager;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;

    private QBChatDialog qbChatDialog;
    private ArrayList<QBChatMessage> unShownMessages;
    private int skipPagination = 0;
    private ChatMessageListener chatMessageListener;
    private boolean checkAdapterInit;

    public static void startForResult(Activity activity, int code, QBChatDialog dialogId) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG_ID, dialogId);
        activity.startActivityForResult(intent, code);
    }

    public static void startForResult(Activity activity, int code, QBChatDialog dialogId, boolean isNewDialog) {
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(ChatActivity.EXTRA_IS_NEW_DIALOG, isNewDialog);
        activity.startActivityForResult(intent, code);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Log.v(TAG, "onCreate ChatActivity on Thread ID = " + Thread.currentThread().getId());

        if (!ChatHelper.getInstance().isLogged()) {
            Log.w(TAG, "Restarting App...");
            restartApp(this);
        }

        dialogsManager = new DialogsManager();
        dialogsManager.addManagingDialogsCallbackListener(this);
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
        systemMessagesListener = new SystemMessagesListener();

        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_DIALOG_ID);
        Log.v(TAG, "Deserialized dialog = " + qbChatDialog);

        qbChatDialog.initForChat(QBChatService.getInstance());
        chatMessageListener = new ChatMessageListener();
        qbChatDialog.addMessageListener(chatMessageListener);

        initViews();
        initMessagesRecyclerView();
        initChatConnectionListener();
        initChat();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        if (qbChatDialog != null) {
            outState.putString(EXTRA_DIALOG_ID, qbChatDialog.getDialogId());
        }
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (qbChatDialog == null) {
            qbChatDialog = QbDialogHolder.getInstance().getChatDialogById(savedInstanceState.getString(EXTRA_DIALOG_ID));
        }
    }

    @Override
    public void onResumeFinished() {
        if (ChatHelper.getInstance().isLogged()) {
            if (qbChatDialog == null) {
                qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_DIALOG_ID);
            }
            qbChatDialog.initForChat(QBChatService.getInstance());
            try {
                qbChatDialog.join(new DiscussionHistory());
            } catch (Exception ignored) {

            }
            returnListeners();
        } else {
            showProgressDialog(R.string.dlg_loading);
            ChatHelper.getInstance().loginToChat(SharedPrefsHelper.getInstance().getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    qbChatDialog.initForChat(QBChatService.getInstance());
                    returnListeners();
                    hideProgressDialog();
                }

                @Override
                public void onError(QBResponseException e) {
                    hideProgressDialog();
                    finish();
                }
            });
        }
    }

    private void returnListeners() {
        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener != null
                    ? systemMessagesListener : new SystemMessagesListener());
        }

        chatAdapter.setAttachImageClickListener(imageAttachClickListener);
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        qbMessageStatusesManager = QBChatService.getInstance().getMessageStatusesManager();
        qbMessageStatusesManager.addMessageStatusListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatAdapter.removeAttachImageClickListener();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
        qbMessageStatusesManager.removeMessageStatusListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }
        qbChatDialog.removeMessageListrener(chatMessageListener);
        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    @Override
    public void onBackPressed() {
        qbChatDialog.removeMessageListrener(chatMessageListener);
        sendDialogId();
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chat, menu);

        MenuItem menuItemLeave = menu.findItem(R.id.menu_chat_action_leave);
        MenuItem menuItemAdd = menu.findItem(R.id.menu_chat_action_add);
        MenuItem menuItemDelete = menu.findItem(R.id.menu_chat_action_delete);

        if (qbChatDialog.getType() == QBDialogType.PRIVATE) {
            menuItemLeave.setVisible(false);
            menuItemAdd.setVisible(false);
        } else {
            menuItemDelete.setVisible(false);
        }

        if (qbChatDialog.getType() != QBDialogType.GROUP) {
            menu.findItem(R.id.menu_chat_action_add).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_chat_action_info:
                ChatInfoActivity.start(this, qbChatDialog);
                return true;

            case R.id.menu_chat_action_add:
                updateDialog();
                return true;

            case R.id.menu_chat_action_leave:
                leaveGroupChat();
                return true;

            case R.id.menu_chat_action_delete:
                deleteChat();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDialog() {
        ProgressDialogFragment.show(getSupportFragmentManager());
        Log.d(TAG, "Update Dialog");
        ChatHelper.getInstance().getDialogById(qbChatDialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog updatedChatDialog, Bundle bundle) {
                Log.d(TAG, "Update Dialog Successful: " + updatedChatDialog.getDialogId());
                qbChatDialog = updatedChatDialog;
                loadUsersFromQb(updatedChatDialog);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Update Dialog Error: " + e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, null);
            }
        });
    }

    private void loadUsersFromQb(final QBChatDialog qbChatDialog) {
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ORDER_RULE, ORDER_VALUE));

        QBPagedRequestBuilder qbPagedRequestBuilder = new QBPagedRequestBuilder();
        qbPagedRequestBuilder.setRules(rules);
        qbPagedRequestBuilder.setPerPage(LIMIT_PER_PAGE);

        Log.d(TAG, "Loading Users");
        QBUsers.getUsers(qbPagedRequestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                Log.d(TAG, "Loading Users Successful");
                ProgressDialogFragment.hide(getSupportFragmentManager());
                if (qbChatDialog.getOccupants().size() >= users.size()) {
                    ToastUtils.shortToast(R.string.added_users);
                } else {
                    SelectUsersActivity.startForResult(ChatActivity.this, REQUEST_CODE_SELECT_PEOPLE, qbChatDialog);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Loading Users Error: " + e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                showErrorSnackbar(R.string.select_users_get_users_error, e, null);
            }
        });
    }

    private void sendDialogId() {
        Intent result = new Intent();
        result.putExtra(EXTRA_DIALOG_ID, qbChatDialog.getDialogId());
        setResult(RESULT_OK, result);
    }

    private void leaveGroupChat() {
        ProgressDialogFragment.show(getSupportFragmentManager());
        dialogsManager.sendMessageLeftUser(qbChatDialog);
        dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, qbChatDialog);
        Log.d(TAG, "Leaving Dialog");
        ChatHelper.getInstance().exitFromDialog(qbChatDialog, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbDialog, Bundle bundle) {
                Log.d(TAG, "Leaving Dialog Successful: " + qbDialog.getDialogId());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                QbDialogHolder.getInstance().deleteDialog(qbDialog);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Leaving Dialog Error: " + e.getMessage());
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
        Log.d(TAG, "onActivityResult with resultCode: " + resultCode + " requestCode: " + requestCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SELECT_PEOPLE) {
                progressBar.setVisibility(View.VISIBLE);
                final ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(
                        SelectUsersActivity.EXTRA_QB_USERS);
                List<Integer> existingOccupantsIds = qbChatDialog.getOccupants();
                final List<Integer> newUsersIds = new ArrayList<>();

                for (QBUser user : selectedUsers) {
                    if (!existingOccupantsIds.contains(user.getId())) {
                        newUsersIds.add(user.getId());
                    }
                }

                ChatHelper.getInstance().getDialogById(qbChatDialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                        progressBar.setVisibility(View.GONE);
                        dialogsManager.sendMessageAddedUsers(qbChatDialog, newUsersIds);
                        dialogsManager.sendSystemMessageAddedUser(systemMessagesManager, qbChatDialog, newUsersIds);
                        ChatActivity.this.qbChatDialog = qbChatDialog;
                        updateDialog(selectedUsers);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressBar.setVisibility(View.GONE);
                        showErrorSnackbar(R.string.update_dialog_error, e, null);
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SystemPermissionHelper.PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST && grantResults[0] != -1) {
            openImagePicker();
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
        showErrorSnackbar(0, e, null);
    }

    @Override
    public void onImagePickClosed(int ignored) {

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
                ToastUtils.shortToast(R.string.chat_wait_for_attachments_to_upload);
            }
        }

        String text = messageEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(text)) {
            sendChatMessage(text, null);
        }
    }

    public void onAttachmentsClick(View view) {
        if (attachmentPreviewAdapter.getCount() >= MAX_UPLOAD_FILES) {
            ToastUtils.shortToast(getString(R.string.restriction_upload_files, MAX_UPLOAD_FILES));
        } else {
            openImagePicker();
        }
    }

    private void openImagePicker() {
        SystemPermissionHelper permissionHelper = new SystemPermissionHelper(this);
        if (permissionHelper.isSaveImagePermissionGranted()) {
            new ImagePickHelper().pickAnImage(this, REQUEST_CODE_ATTACHMENT);
        } else {
            permissionHelper.requestPermissionsForSaveFileImage();
        }
    }

    public void showMessage(QBChatMessage message) {
        if (isAdapterConnected()) {
            chatAdapter.add(message);
            scrollMessageListDown();
        } else {
            delayShowMessage(message);
        }
    }

    private boolean isAdapterConnected() {
        return checkAdapterInit;
    }

    private void delayShowMessage(QBChatMessage message) {
        if (unShownMessages == null) {
            unShownMessages = new ArrayList<>();
        }
        unShownMessages.add(message);
    }

    private void initViews() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        messageEditText = findViewById(R.id.edit_chat_message);
        progressBar = findViewById(R.id.progress_chat);
        attachmentPreviewContainerLayout = findViewById(R.id.layout_attachment_preview_container);

        attachmentPreviewAdapter = new AttachmentPreviewAdapter(this,
                new AttachmentPreviewAdapter.AttachmentCountChangedListener() {
                    @Override
                    public void onAttachmentCountChanged(int count) {
                        attachmentPreviewContainerLayout.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
                    }
                },
                new AttachmentPreviewAdapter.AttachmentUploadErrorListener() {
                    @Override
                    public void onAttachmentUploadError(QBResponseException e) {
                        showErrorSnackbar(0, e, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onAttachmentsClick(v);
                            }
                        });
                    }
                });
        AttachmentPreviewAdapterView previewAdapterView = findViewById(R.id.adapter_view_attachment_preview);
        previewAdapterView.setAdapter(attachmentPreviewAdapter);
    }

    private void initMessagesRecyclerView() {
        chatMessagesRecyclerView = findViewById(R.id.list_chat_messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatMessagesRecyclerView.setLayoutManager(layoutManager);

        messagesList = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, qbChatDialog, messagesList);
        chatAdapter.setPaginationHistoryListener(new PaginationListener());
        chatMessagesRecyclerView.addItemDecoration(
                new StickyRecyclerHeadersDecoration(chatAdapter));

        chatMessagesRecyclerView.setAdapter(chatAdapter);
        imageAttachClickListener = new ImageAttachClickListener();
    }

    private void sendChatMessage(final String text, final QBAttachment attachment) {
        if (ChatHelper.getInstance().isLogged()) {
            QBChatMessage chatMessage = new QBChatMessage();
            if (attachment != null) {
                chatMessage.addAttachment(attachment);
            } else {
                chatMessage.setBody(text);
            }
            chatMessage.setSaveToHistory(true);
            chatMessage.setDateSent(System.currentTimeMillis() / 1000);
            chatMessage.setMarkable(true);

            if (!QBDialogType.PRIVATE.equals(qbChatDialog.getType()) && !qbChatDialog.isJoined()) {
                ToastUtils.shortToast(R.string.chat_still_joining);
                return;
            }

            try {
                Log.d(TAG, "Sending Message with ID: " + chatMessage.getId());
                qbChatDialog.sendMessage(chatMessage);

                if (QBDialogType.PRIVATE.equals(qbChatDialog.getType())) {
                    showMessage(chatMessage);
                }

                if (attachment != null) {
                    attachmentPreviewAdapter.remove(attachment);
                } else {
                    messageEditText.setText("");
                }
            } catch (SmackException.NotConnectedException e) {
                Log.w(TAG, e);
                ToastUtils.shortToast(R.string.chat_error_send_message);
            }
        } else {
            showProgressDialog(R.string.dlg_login);
            Log.d(TAG, "Relogin to Chat");
            ChatHelper.getInstance().loginToChat(ChatHelper.getCurrentUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Relogin Successfull");
                    sendChatMessage(text, attachment);
                    hideProgressDialog();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Relogin Error: " + e.getMessage());
                    hideProgressDialog();
                    ToastUtils.shortToast(R.string.chat_error_send_message);
                }
            });
        }
    }

    private void initChat() {
        switch (qbChatDialog.getType()) {
            case GROUP:
            case PUBLIC_GROUP:
                joinGroupChat();
                break;

            case PRIVATE:
                loadDialogUsers();
                break;

            default:
                ToastUtils.shortToast(String.format("%s %s", getString(R.string.chat_unsupported_type), qbChatDialog.getType().name()));
                finish();
                break;
        }
    }

    private void joinGroupChat() {
        progressBar.setVisibility(View.VISIBLE);
        ChatHelper.getInstance().join(qbChatDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle b) {
                Log.d(TAG, "Joined to Dialog Successful");
                notifyUsersAboutCreatingDialog();
                hideProgressDialog();
                loadDialogUsers();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Joining Dialog Error:" + e.getMessage());
                progressBar.setVisibility(View.GONE);
                showErrorSnackbar(R.string.connection_error, e, null);
            }
        });
    }

    private void notifyUsersAboutCreatingDialog() {
        if (getIntent().getBooleanExtra(EXTRA_IS_NEW_DIALOG, false)) {
            dialogsManager.sendMessageCreatedDialog(qbChatDialog);
            getIntent().removeExtra(EXTRA_IS_NEW_DIALOG);
        }
    }

    private void leaveGroupDialog() {
        try {
            ChatHelper.getInstance().leaveChatDialog(qbChatDialog);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private void updateDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().updateDialogUsers(qbChatDialog, selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        qbChatDialog = dialog;
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
        ChatHelper.getInstance().getUsersFromDialog(qbChatDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle bundle) {
                setChatNameToActionBar();
                loadChatHistory();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
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
        String chatName = QbDialogUtils.getDialogName(qbChatDialog);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(chatName);
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setHomeButtonEnabled(true);
        }
    }

    private void loadChatHistory() {
        ChatHelper.getInstance().loadChatHistory(qbChatDialog, skipPagination, new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> messages, Bundle args) {
                // The newest messages should be in the end of list,
                // so we need to reverse list to show messages in the right order
                Collections.reverse(messages);
                if (!checkAdapterInit) {
                    checkAdapterInit = true;
                    chatAdapter.addList(messages);
                    addDelayedMessagesToAdapter();
                } else {
                    chatAdapter.addToList(messages);
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Loading Dialog History Error: " + e.getMessage());
                progressBar.setVisibility(View.GONE);
                skipPagination -= ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
                showErrorSnackbar(R.string.connection_error, e, null);
            }
        });
        skipPagination += ChatHelper.CHAT_HISTORY_ITEMS_PER_PAGE;
    }

    private void addDelayedMessagesToAdapter() {
        if (unShownMessages != null && !unShownMessages.isEmpty()) {
            List<QBChatMessage> chatList = chatAdapter.getList();
            for (QBChatMessage message : unShownMessages) {
                if (!chatList.contains(message)) {
                    chatAdapter.add(message);
                }
            }
        }
    }

    private void scrollMessageListDown() {
        chatMessagesRecyclerView.scrollToPosition(messagesList.size() - 1);
    }

    private void deleteChat() {
        ChatHelper.getInstance().deleteDialog(qbChatDialog, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                setResult(RESULT_OK);
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

    private void initChatConnectionListener() {
        View rootView = findViewById(R.id.list_chat_messages);
        chatConnectionListener = new VerboseQbChatConnectionListener(rootView) {
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                Log.d(TAG, "Reconnection Successful");
                skipPagination = 0;
                switch (qbChatDialog.getType()) {
                    case GROUP:
                        checkAdapterInit = false;
                        // Join active room if we're in Group Chat
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                joinGroupChat();
                            }
                        });
                        break;
                }
            }
        };
    }

    @Override
    public void processMessageDelivered(String messageID, String dialogID, Integer userID) {
        if (qbChatDialog.getDialogId().equals(dialogID)) {
            chatAdapter.updateStatusDelivered(messageID, userID);
        }
    }

    @Override
    public void processMessageRead(String messageID, String dialogID, Integer userID) {
        if (qbChatDialog.getDialogId().equals(dialogID)) {
            chatAdapter.updateStatusRead(messageID, userID);
        }
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {

    }

    @Override
    public void onDialogUpdated(String chatDialog) {

    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {

    }

    private class ChatMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {
            Log.d(TAG, "Processing Received Message: " + qbChatMessage.getBody());
            showMessage(qbChatMessage);
        }
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            Log.d(TAG, "System Message Received: " + qbChatMessage.getId());
            dialogsManager.onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {
            Log.d(TAG, "System Messages Error: " + e.getMessage() + "With MessageID: " + qbChatMessage.getId());
        }
    }

    private class ImageAttachClickListener implements AttachClickListener {

        @Override
        public void onLinkClicked(QBAttachment qbAttachment, int position) {
            if (qbAttachment != null) {
                String url = QBFile.getPrivateUrlForUID(qbAttachment.getId());
                AttachmentImageActivity.start(ChatActivity.this, url);
            }
        }
    }

    private class PaginationListener implements PaginationHistoryListener {

        @Override
        public void downloadMore() {
            Log.w(TAG, "Download More");
            loadChatHistory();
        }
    }
}