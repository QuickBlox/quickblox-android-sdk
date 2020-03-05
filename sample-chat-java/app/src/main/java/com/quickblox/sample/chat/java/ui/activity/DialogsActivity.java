package com.quickblox.sample.chat.java.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.async.BaseAsyncTask;
import com.quickblox.sample.chat.java.managers.DialogsManager;
import com.quickblox.sample.chat.java.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.java.utils.FcmConsts;
import com.quickblox.sample.chat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.QbChatDialogMessageListenerImp;
import com.quickblox.sample.chat.java.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.java.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.sample.chat.java.utils.qb.callback.QBPushSubscribeListenerImpl;
import com.quickblox.sample.chat.java.utils.qb.callback.QbEntityCallbackImpl;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.view.ActionMode;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class DialogsActivity extends BaseActivity implements DialogsManager.ManagingDialogsCallbacks {
    private static final String TAG = DialogsActivity.class.getSimpleName();

    public static final int DIALOGS_PER_PAGE = 100;
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;
    private static final int PLAY_SERVICES_REQUEST_CODE = 9000;

    private SwipyRefreshLayout refreshLayout;
    private ProgressBar progress;
    private boolean isProcessingResultInProgress = false;
    private BroadcastReceiver pushBroadcastReceiver;
    private ConnectionListener chatConnectionListener;

    private DialogsAdapter dialogsAdapter;
    private QBChatDialogMessageListener allDialogsMessagesListener = new AllDialogsMessageListener();
    private DialogsActivity.SystemMessagesListener systemMessagesListener = new SystemMessagesListener();
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private DialogsManager dialogsManager = new DialogsManager();

    private QBUser currentUser;
    private ActionMode currentActionMode;
    private boolean hasMoreDialogs = true;
    private final Set<DialogJoinerAsyncTask> joinerTasksSet = new HashSet<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        if (!ChatHelper.getInstance().isLogged()) {
            Log.w(TAG, "Restarting App...");
            restartApp(this);
        }

        if (ChatHelper.getCurrentUser() != null) {
            currentUser = ChatHelper.getCurrentUser();
        } else {
            finish();
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));
        }
        initUi();
        initConnectionListener();
    }

    @Override
    public void onResumeFinished() {
        if (ChatHelper.getInstance().isLogged()) {
            checkPlayServicesAvailable();
            registerQbChatListeners();
            if (!QbDialogHolder.getInstance().getDialogs().isEmpty()) {
                loadDialogsFromQb(true, true);
            } else {
                loadDialogsFromQb(false, true);
            }
        } else {
            reloginToChat();
        }
    }

    private void reloginToChat() {
        showProgressDialog(R.string.dlg_relogin);
        if (SharedPrefsHelper.getInstance().hasQbUser()) {
            ChatHelper.getInstance().loginToChat(SharedPrefsHelper.getInstance().getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Relogin Successful");
                    checkPlayServicesAvailable();
                    registerQbChatListeners();
                    loadDialogsFromQb(false, false);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Relogin Failed " + e.getMessage());
                    hideProgressDialog();
                    finish();
                }
            });
        }
    }

    private void checkPlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_REQUEST_CODE).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelTasks();
        unregisterQbChatListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterQbChatListeners();
    }

    private void registerQbChatListeners() {
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        try {
            systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();
            incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        } catch (Exception e) {
            Log.d(TAG, "Can not get SystemMessagesManager. Need relogin. " + e.getMessage());
            reloginToChat();
            return;
        }
        if (incomingMessagesManager == null) {
            reloginToChat();
            return;
        }

        systemMessagesManager.addSystemMessageListener(systemMessagesListener);
        incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener);
        dialogsManager.addManagingDialogsCallbackListener(this);

        pushBroadcastReceiver = new PushBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(FcmConsts.ACTION_NEW_FCM_EVENT));
    }

    private void unregisterQbChatListeners() {
        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(allDialogsMessagesListener);
        }

        if (systemMessagesManager != null) {
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }

        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    private void cancelTasks() {
        for (DialogJoinerAsyncTask task : joinerTasksSet) {
            task.cancel(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_dialogs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.menu_dialogs_action_logout:
                isProcessingResultInProgress = true;
                item.setEnabled(false);
                invalidateOptionsMenu();
                userLogout();
                return true;
            case R.id.menu_appinfo:
                AppInfoActivity.start(this);
                return true;
            case R.id.menu_add_chat:
                showProgressDialog(R.string.dlg_loading);
                SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult with ResultCode: " + resultCode + " RequestCode: " + requestCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_SELECT_PEOPLE:
                    ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);
                    String chatName = data.getStringExtra(SelectUsersActivity.EXTRA_CHAT_NAME);
                    if (isPrivateDialogExist(selectedUsers)) {
                        selectedUsers.remove(ChatHelper.getCurrentUser());
                        QBChatDialog existingPrivateDialog = QbDialogHolder.getInstance().getPrivateDialogWithUser(selectedUsers.get(0));
                        isProcessingResultInProgress = false;
                        if (existingPrivateDialog != null) {
                            ChatActivity.startForResult(this, REQUEST_DIALOG_ID_FOR_UPDATE, existingPrivateDialog);
                        }
                    } else {
                        showProgressDialog(R.string.create_chat);
                        if (TextUtils.isEmpty(chatName)) {
                            chatName = "";
                        }
                        createDialog(selectedUsers, chatName);
                    }
                    break;
                case REQUEST_DIALOG_ID_FOR_UPDATE:
                    if (data != null) {
                        String dialogID = data.getStringExtra(ChatActivity.EXTRA_DIALOG_ID);
                        loadUpdatedDialog(dialogID);
                    } else {
                        isProcessingResultInProgress = false;
                        loadDialogsFromQb(true, false);
                    }
                    break;
            }
        } else {
            updateDialogsAdapter();
        }
    }

    private boolean isPrivateDialogExist(ArrayList<QBUser> allSelectedUsers) {
        ArrayList<QBUser> selectedUsers = new ArrayList<>();
        selectedUsers.addAll(allSelectedUsers);
        selectedUsers.remove(ChatHelper.getCurrentUser());
        return selectedUsers.size() == 1 && QbDialogHolder.getInstance().hasPrivateDialogWithUser(selectedUsers.get(0));
    }

    private void loadUpdatedDialog(String dialogId) {
        ChatHelper.getInstance().getDialogById(dialogId, new QbEntityCallbackImpl<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog result, Bundle bundle) {
                QbDialogHolder.getInstance().addDialog(result);
                updateDialogsAdapter();
                isProcessingResultInProgress = false;
            }

            @Override
            public void onError(QBResponseException e) {
                isProcessingResultInProgress = false;
            }
        });
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void userLogout() {
        Log.d(TAG, "SignOut");
        showProgressDialog(R.string.dlg_logout);
        unsubscribeFromPushes();
    }

    private void unsubscribeFromPushes() {
        if (QBPushManager.getInstance().isSubscribedToPushes()) {
            QBPushManager.getInstance().addListener(new QBPushSubscribeListenerImpl() {
                @Override
                public void onSubscriptionDeleted(boolean success) {
                    Log.d(TAG, "Subscription Deleted -> " + (success? "TRUE" : "FALSE"));
                    QBPushManager.getInstance().removeListener(this);

                    QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            ChatHelper.getInstance().destroy();
                            SharedPrefsHelper.getInstance().removeQbUser();
                            QbDialogHolder.getInstance().clear();
                            LoginActivity.start(DialogsActivity.this);
                            hideProgressDialog();
                            finish();
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            Log.d(TAG, "Unable to SignOut: " + e.getMessage());
                            hideProgressDialog();
                            showErrorSnackbar(R.string.error_logout, e, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    userLogout();
                                }
                            });
                        }
                    });
                }
            });
            SubscribeService.unSubscribeFromPushes(DialogsActivity.this);
        }
    }

    private void initUi() {
        LinearLayout emptyHintLayout = findViewById(R.id.ll_chat_empty);
        ListView dialogsListView = findViewById(R.id.list_dialogs_chats);
        refreshLayout = findViewById(R.id.swipy_refresh_layout);
        progress = findViewById(R.id.pb_dialogs);

        ArrayList<QBChatDialog> dialogs = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        dialogsAdapter = new DialogsAdapter(this, dialogs);

        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                if (currentActionMode != null) {
                    dialogsAdapter.toggleSelection(selectedDialog);
                    String subtitle = "";
                    if (dialogsAdapter.getSelectedItems().size() != 1) {
                        subtitle = getString(R.string.dialogs_actionmode_subtitle, String.valueOf(dialogsAdapter.getSelectedItems().size()));
                    } else {
                        subtitle = getString(R.string.dialogs_actionmode_subtitle_single, String.valueOf(dialogsAdapter.getSelectedItems().size()));
                    }
                    currentActionMode.setSubtitle(subtitle);
                    currentActionMode.getMenu().getItem(0).setVisible(dialogsAdapter.getSelectedItems().size() >= 1);
                } else if (ChatHelper.getInstance().isLogged()) {
                    showProgressDialog(R.string.dlg_login);
                    ChatHelper.getInstance().loginToChat(currentUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            hideProgressDialog();
                            ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            hideProgressDialog();
                            ToastUtils.shortToast(R.string.login_chat_login_error);
                        }
                    });
                }
            }
        });

        dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                startSupportActionMode(new DeleteActionModeCallback());
                dialogsAdapter.selectItem(selectedDialog);
                return true;
            }
        });

        refreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                cancelTasks();
                loadDialogsFromQb(true, true);
            }
        });
        refreshLayout.setColorSchemeResources(R.color.color_new_blue, R.color.random_color_2, R.color.random_color_3, R.color.random_color_7);
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers, String chatName) {
        Log.d(TAG, "Creating Dialog");
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers, chatName,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        Log.d(TAG, "Creating Dialog Successful");
                        isProcessingResultInProgress = false;
                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        ArrayList<QBChatDialog> dialogs = new ArrayList<>();
                        dialogs.add(dialog);
                        new DialogJoinerAsyncTask(DialogsActivity.this, dialogs).execute();
                        ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, dialog, true);
                        hideProgressDialog();
                    }

                    @Override
                    public void onError(QBResponseException error) {
                        Log.d(TAG, "Creating Dialog Error: " + error.getMessage());
                        isProcessingResultInProgress = false;
                        hideProgressDialog();
                        showErrorSnackbar(R.string.dialogs_creation_error, error, null);
                    }
                }
        );
    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        isProcessingResultInProgress = true;
        if (silentUpdate) {
            progress.setVisibility(View.VISIBLE);
        } else {
            showProgressDialog(R.string.dlg_loading);
        }
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(DIALOGS_PER_PAGE);
        requestBuilder.setSkip(clearDialogHolder ? 0 : QbDialogHolder.getInstance().getDialogs().size());

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                if (dialogs.size() < DIALOGS_PER_PAGE) {
                    hasMoreDialogs = false;
                }
                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                    hasMoreDialogs = true;
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                updateDialogsAdapter();

                DialogJoinerAsyncTask joinerTask = new DialogJoinerAsyncTask(DialogsActivity.this, dialogs);
                joinerTasksSet.add(joinerTask);
                joinerTask.execute();

                disableProgress();
                if (hasMoreDialogs) {
                    loadDialogsFromQb(true, false);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                ToastUtils.shortToast(e.getMessage());
            }
        });
    }

    private void disableProgress() {
        isProcessingResultInProgress = false;
        hideProgressDialog();
        refreshLayout.setRefreshing(false);
        progress.setVisibility(View.GONE);
    }

    private void initConnectionListener() {
        View rootView = findViewById(R.id.layout_root);
        chatConnectionListener = new VerboseQbChatConnectionListener(rootView) {
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                loadDialogsFromQb(false, true);
            }
        };
    }

    private void updateDialogsAdapter() {
        ArrayList<QBChatDialog> listDialogs = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        dialogsAdapter.updateList(listDialogs);
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        loadDialogsFromQb(true, true);
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        updateDialogsAdapter();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        updateDialogsAdapter();
    }

    private class DeleteActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(80);
            }

            mode.setTitle(R.string.dialogs_actionmode_title);
            mode.setSubtitle(getString(R.string.dialogs_actionmode_subtitle_single, "1"));
            mode.getMenuInflater().inflate(R.menu.menu_activity_dialogs_action_mode, menu);

            dialogsAdapter.prepareToSelect();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_dialogs_action_delete:
                    deleteSelected();
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            dialogsAdapter.clearSelection();
        }

        private void deleteSelected() {
            showProgressDialog(R.string.dlg_deleting_chats);
            List<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            List<QBChatDialog> groupDialogsToDelete = new ArrayList<>();
            List<QBChatDialog> privateDialogsToDelete = new ArrayList<>();

            for (QBChatDialog dialog : selectedDialogs) {
                if (dialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
                    ToastUtils.shortToast(getString(R.string.dialogs_cannot_delete_chat, dialog.getName()));
                } else if (dialog.getType().equals(QBDialogType.GROUP)) {
                    groupDialogsToDelete.add(dialog);
                } else if (dialog.getType().equals(QBDialogType.PRIVATE)) {
                    privateDialogsToDelete.add(dialog);
                }
            }

            if (privateDialogsToDelete.size() > 0) {
                deletePrivateDialogs(privateDialogsToDelete);
            }

            if (groupDialogsToDelete.size() > 0) {
                notifyDialogsLeave(groupDialogsToDelete);
                leaveGroupDialogs(groupDialogsToDelete);
            } else {
                hideProgressDialog();
            }
        }

        private void deletePrivateDialogs(final List<QBChatDialog> privateDialogsToDelete) {
            ChatHelper.getInstance().deletePrivateDialogs(privateDialogsToDelete, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    Log.d(TAG, "PRIVATE Dialogs Deleting Successful");
                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    updateDialogsAdapter();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Deleting PRIVATE Dialogs Error: " + e.getMessage());
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deletePrivateDialogs(privateDialogsToDelete);
                                }
                            });
                }
            });
        }

        private void leaveGroupDialogs(final List<QBChatDialog> groupDialogsToDelete) {
            ChatHelper.getInstance().leaveGroupDialogs(groupDialogsToDelete, new QBEntityCallback<List<QBChatDialog>>() {
                @Override
                public void onSuccess(List<QBChatDialog> qbChatDialogs, Bundle bundle) {
                    Log.d(TAG, "GROUP Dialogs Deleting Successful");
                    QbDialogHolder.getInstance().deleteDialogs(qbChatDialogs);
                    updateDialogsAdapter();
                    hideProgressDialog();
                }

                @Override
                public void onError(QBResponseException e) {
                    hideProgressDialog();
                    Log.d(TAG, "Deleting GROUP Dialogs Error: " + e.getMessage());
                    ToastUtils.longToast(R.string.dialogs_deletion_error);
                }
            });
        }

        private void notifyDialogsLeave(List<QBChatDialog> dialogsToNotify) {
            for (final QBChatDialog dialog : dialogsToNotify) {
                dialogsManager.sendMessageLeftUser(dialog);
                dialogsManager.sendSystemMessageLeftUser(systemMessagesManager, dialog);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(FcmConsts.EXTRA_FCM_MESSAGE);
            Log.v(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
            loadDialogsFromQb(false, false);
        }
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            dialogsManager.onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException ignored, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener extends QbChatDialogMessageListenerImp {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
            Log.d(TAG, "Processing received Message: " + qbChatMessage.getBody());
            if (!senderId.equals(currentUser.getId())) {
                dialogsManager.onGlobalMessageReceived(dialogId, qbChatMessage);
            }
        }
    }

    private static class DialogJoinerAsyncTask extends BaseAsyncTask<Void, Void, Void> {
        private WeakReference<DialogsActivity> activityRef;
        private ArrayList<QBChatDialog> dialogs;

        DialogJoinerAsyncTask(DialogsActivity dialogsActivity, ArrayList<QBChatDialog> dialogs) {
            activityRef = new WeakReference<>(dialogsActivity);
            this.dialogs = dialogs;
        }

        @Override
        public Void performInBackground(Void... voids) throws Exception {
            if (!isCancelled()) {
                ChatHelper.getInstance().join(dialogs);
            }
            return null;
        }

        @Override
        public void onResult(Void aVoid) {
            if (!isCancelled() && activityRef.get().hasMoreDialogs) {
                activityRef.get().disableProgress();
            }
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            if (!isCancelled()) {
                Log.d("Dialog Joiner Task", "Error: " + e.getMessage());
                ToastUtils.shortToast("Error: " + e.getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancel(true);
        }
    }
}