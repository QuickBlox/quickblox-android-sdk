package com.quickblox.sample.chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.QBSystemMessagesManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.listeners.QBSystemMessageListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.utils.CollectionUtils;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.utils.qb.QbDialogUtils;
import com.quickblox.sample.chat.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;

import java.util.ArrayList;
import java.util.Collection;

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_MARK_READ = 165;

    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private ActionMode currentActionMode;
    private SwipyRefreshLayout setOnRefreshListener;
    private QBRequestGetBuilder requestBuilder;
    private Menu menu;
    private int skipRecords = 0;
    private boolean isActivityForeground;
    private boolean isProcessingResultInProgress;

    private BroadcastReceiver pushBroadcastReceiver;
    private GooglePlayServicesHelper googlePlayServicesHelper;
    private DialogsAdapter dialogsAdapter;
    private QBChatDialogMessageListener chatServiceMessagesListener;
    private SystemMessagesListener systemMessagesListener;
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        context.startActivity(intent);
    }

    private ConnectionListener chatConnectionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }

        pushBroadcastReceiver = new PushBroadcastReceiver();

        chatServiceMessagesListener = new AllDialogsMessageListener();

        systemMessagesListener = new SystemMessagesListener();

        chatConnectionListener = new VerboseQbChatConnectionListener(getSnackbarAnchorView()) {

            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();

                requestBuilder.setSkip(skipRecords = 0);
                loadDialogsFromQbInUiThread(true);
            }
        };

        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ChatHelper.getInstance().addConnectionListener(chatConnectionListener);
        isActivityForeground = true;
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));
    }

    @Override
    protected void onPause() {
        super.onPause();
        ChatHelper.getInstance().removeConnectionListener(chatConnectionListener);
        isActivityForeground = false;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isAppSessionActive) {
            unregisterQbChatListeners();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dialogs, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item);
        }

        switch (item.getItemId()) {
            case R.id.menu_dialogs_action_logout:
                userLogout();
                item.setEnabled(false);
                invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            QBUser currentUser = ChatHelper.getCurrentUser();
            if (currentUser != null) {
                setActionBarTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));
            }

            registerQbChatListeners();
            if (QbDialogHolder.getInstance().getDialogsMap().size() > 0) {
                loadDialogsFromQb(true, true);
            } else {
                loadDialogsFromQb();
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            isProcessingResultInProgress = true;
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ProgressDialogFragment.show(getSupportFragmentManager(), R.string.create_chat);
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                createDialog(selectedUsers);
            } else if (requestCode == REQUEST_MARK_READ) {
                if (data != null) {
                    ArrayList<String> chatMessageIds = (ArrayList<String>) data
                            .getSerializableExtra(ChatActivity.EXTRA_MARK_READ);
                    final StringifyArrayList<String> messagesIds = new StringifyArrayList<>();
                    messagesIds.addAll(chatMessageIds);

                    String dialogId = (String) data.getSerializableExtra(ChatActivity.EXTRA_DIALOG_ID);

                    markMessagesRead(messagesIds, dialogId);
                } else {
                    isProcessingResultInProgress = false;
                    updateDialogsList();
                }
            }
        } else {
            updateDialogsList();
        }
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void userLogout() {
        if (ChatHelper.getInstance().logout()) {
            if (googlePlayServicesHelper.checkPlayServicesAvailable()) {
                googlePlayServicesHelper.unregisterFromGcm(Consts.GCM_SENDER_ID);
            }
            SharedPreferencesUtil.removeQbUser();
            LoginActivity.start(this);
            QbDialogHolder.getInstance().clear();
            ProgressDialogFragment.hide(getSupportFragmentManager());
            finish();
        } else {
            reconnectToChatLogout(SharedPreferencesUtil.getQbUser());
        }
    }

    private void reconnectToChatLogout(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_restoring_chat_session_logout);

        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                userLogout();
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                menu.findItem(R.id.menu_dialogs_action_logout).setEnabled(true);
                invalidateOptionsMenu();
                ErrorUtils.showSnackbar(getSnackbarAnchorView(), R.string.no_internet_connection,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                reconnectToChatLogout(SharedPreferencesUtil.getQbUser());
                            }
                        });
            }
        });
    }

    private void markMessagesRead(StringifyArrayList<String> messagesIds, String dialogId) {
        if (messagesIds.size() > 0) {
            QBRestChatService.markMessagesAsRead(dialogId, messagesIds).performAsync(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    isProcessingResultInProgress = false;
                    updateDialogsList();
                }

                @Override
                public void onError(QBResponseException e) {
                    isProcessingResultInProgress = false;
                }
            });
        } else {
            isProcessingResultInProgress = false;
            updateDialogsList();
        }
    }

    private void updateDialogsList() {
        if (isAppSessionActive) {
            requestBuilder.setSkip(skipRecords = 0);

            loadDialogsFromQb(true, true);
        }
    }

    public void onStartNewChatClick(View view) {
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    private void initUi() {
        LinearLayout emptyHintLayout = _findViewById(R.id.layout_chat_empty);
        ListView dialogsListView = _findViewById(R.id.list_dialogs_chats);
        progressBar = _findViewById(R.id.progress_dialogs);
        fab = _findViewById(R.id.fab_dialogs_new_chat);
        setOnRefreshListener = _findViewById(R.id.swipy_refresh_layout);

        dialogsAdapter = new DialogsAdapter(this,
                CollectionUtils.convertMapToList(QbDialogHolder.getInstance().getDialogsMap()));

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, dialogsListView, false);
        listHeader.setText(R.string.dialogs_list_hint);
        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.addHeaderView(listHeader, null, false);

        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                if (currentActionMode == null) {
                    ChatActivity.startForResult(DialogsActivity.this, REQUEST_MARK_READ, selectedDialog.getDialogId());
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
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
        requestBuilder = new QBRequestGetBuilder();

        setOnRefreshListener.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                requestBuilder.setSkip(skipRecords += ChatHelper.DIALOG_ITEMS_PER_PAGE);
                loadDialogsFromQb(true, false);
            }
        });
    }

    private void registerQbChatListeners() {
        incomingMessagesManager = QBChatService.getInstance().getIncomingMessagesManager();
        systemMessagesManager = QBChatService.getInstance().getSystemMessagesManager();

        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(chatServiceMessagesListener);
        }

        if (systemMessagesManager != null){
            systemMessagesManager.addSystemMessageListener(systemMessagesListener);
        }
    }

    private void unregisterQbChatListeners() {
        if (incomingMessagesManager != null) {
            incomingMessagesManager.removeDialogMessageListrener(chatServiceMessagesListener);
        }

        if (systemMessagesManager != null){
            systemMessagesManager.removeSystemMessageListener(systemMessagesListener);
        }
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        isProcessingResultInProgress = false;
                        sendSystemMessageAboutCreatingDialog(dialog);
                        ChatActivity.startForResult(DialogsActivity.this, REQUEST_MARK_READ, dialog.getDialogId());
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        isProcessingResultInProgress = false;
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        showErrorSnackbar(R.string.dialogs_creation_error, null, null);
                    }
                }
        );
    }

    private void sendSystemMessageAboutCreatingDialog(QBChatDialog dialog) {
        QBChatMessage systemMessageCreatingDialog = QbDialogUtils.createSystemMessageAboutCreatingGroupDialog(dialog);

        try {
            for (Integer recipientId : dialog.getOccupants()) {
                if (!recipientId.equals(QBChatService.getInstance().getUser().getId())) {
                    systemMessageCreatingDialog.setRecipientId(recipientId);
                    systemMessagesManager.sendSystemMessage(systemMessageCreatingDialog);
                }
            }
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void loadDialogsFromQb() {
        loadDialogsFromQb(false, true);
    }

    private void loadDialogsFromQbInUiThread(final boolean silentUpdate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadDialogsFromQb(silentUpdate, true);
            }
        });
    }

    private void loadDialogsFromQb(final boolean silentUpdate, final boolean clearDialogHolder) {
        if (!silentUpdate) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);

                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                dialogsAdapter.addList(CollectionUtils.convertMapToList(QbDialogHolder.getInstance().getDialogsMap()));
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                Toast.makeText(DialogsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUsersFromDialog(QBChatDialog chatDialog){
        ChatHelper.getInstance().getUsersFromDialog(chatDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {

            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }

    private void removeDialogsFromAdapter(Collection<QBChatDialog> selectedDialogs) {
        for (QBChatDialog chatDialog : selectedDialogs){
            dialogsAdapter.remove(chatDialog);
        }
    }

    private class DeleteActionModeCallback implements ActionMode.Callback {

        public DeleteActionModeCallback() {
            fab.hide();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_dialogs, menu);
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
                    deleteSelectedDialogs();
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
            fab.show();
        }

        private void deleteSelectedDialogs() {
            final Collection<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            ChatHelper.getInstance().deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    removeDialogsFromAdapter(selectedDialogs);
                }

                @Override
                public void onError(QBResponseException e) {
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteSelectedDialogs();
                                }
                            });
                }
            });
        }
    }

    private class PushBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String message = intent.getStringExtra(GcmConsts.EXTRA_GCM_MESSAGE);
            Log.e(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
//            loadDialogsFromQb(true, true);
        }
    }

    private class SystemMessagesListener implements QBSystemMessageListener {
        @Override
        public void processMessage(final QBChatMessage qbChatMessage) {
            Log.d(TAG, "Received system message");
            if (qbChatMessage.getProperty(QbDialogUtils.PROPERTY_NOTIFICATION_TYPE).equals(QbDialogUtils.CREATING_DIALOG)){
                Log.d(TAG, "Received system message about creating dialog by id " + qbChatMessage.getDialogId());
                final QBChatDialog newGroupDialog = QbDialogUtils.buildChatDialogFromSystemMessage(qbChatMessage);
                QbDialogHolder.getInstance().addDialogToMap(newGroupDialog);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toaster.shortToast("Received system message about creating dialog by id " + qbChatMessage.getDialogId());
                        dialogsAdapter.add(newGroupDialog);
                    }
                });
            }
        }

        @Override
        public void processError(QBChatException e, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener implements QBChatDialogMessageListener{

        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer integer) {
            Log.d(TAG, "new incoming message dialog id = " + dialogId + " message id = " + qbChatMessage.getId());
            if (!QbDialogHolder.getInstance().hadDialogWithId(dialogId)) {
                Log.d(TAG, "dialogsAdapter not have this dialog");
                ChatHelper.getInstance().getDialogById(dialogId, new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog chatDialog, Bundle bundle) {
                        QbDialogHolder.getInstance().addDialogToMap(chatDialog);
                        dialogsAdapter.add(chatDialog);
                        loadUsersFromDialog(chatDialog);
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            } else {
                Log.d(TAG, "dialogsAdapter has this dialog");
                dialogsAdapter.notifyDataSetChanged();
            }

        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        }
    }
}
