package com.quickblox.sample.chat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBGroupChatManagerListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.QbDialogHolder;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.utils.constant.GcmConsts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;

    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private ActionMode currentActionMode;
    private SwipyRefreshLayout setOnRefreshListener;
    private QBRequestGetBuilder requestBuilder;
    private int skipRecords = 0;

    private BroadcastReceiver pushBroadcastReceiver;
    private GooglePlayServicesHelper googlePlayServicesHelper;
    private DialogsAdapter dialogsAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        context.startActivity(intent);
    }

    private QBPrivateChatManagerListener privateChatManagerListener;
    private QBGroupChatManagerListener groupChatManagerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            googlePlayServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }

        pushBroadcastReceiver = new PushBroadcastReceiver();

        privateChatManagerListener = new QBPrivateChatManagerListener() {
            @Override
            public void chatCreated(QBPrivateChat qbPrivateChat, boolean createdLocally) {
                Log.d("DialogsActiv", "qbPrivateChat");
                loadDialogsFromQbInUiThread(true);
            }
        };
        groupChatManagerListener = new QBGroupChatManagerListener() {
            @Override
            public void chatCreated(QBGroupChat qbGroupChat) {
                loadDialogsFromQbInUiThread(true);
                Log.d("DialogsActiv", "qbGroupChat");
            }
        };

        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        googlePlayServicesHelper.checkPlayServicesAvailable(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(pushBroadcastReceiver,
                new IntentFilter(GcmConsts.ACTION_NEW_GCM_EVENT));

        if (isAppSessionActive) {
            registerQbChatListeners();
            requestBuilder.setSkip(skipRecords = 0);

            loadDialogsFromQb(true, true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver);

        if (isAppSessionActive) {
            unregisterQbChatListeners();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_dialogs, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_dialogs_action_logout:
                ChatHelper.getInstance().logout();
                if (googlePlayServicesHelper.checkPlayServicesAvailable()) {
                    googlePlayServicesHelper.unregisterFromGcm(Consts.GCM_SENDER_ID);
                }
                SharedPreferencesUtil.removeQbUser();
                LoginActivity.start(this);
                finish();
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
                actionBar.setTitle(getString(R.string.dialogs_logged_in_as, currentUser.getFullName()));
            }

            registerQbChatListeners();
            if (QbDialogHolder.getInstance().getDialogList().size() > 0) {
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
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                createDialog(selectedUsers);
            }
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

    public void onStartNewChatClick(View view) {
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    private void initUi() {
        LinearLayout emptyHintLayout = _findViewById(R.id.layout_chat_empty);
        ListView dialogsListView = _findViewById(R.id.list_dialogs_chats);
        progressBar = _findViewById(R.id.progress_dialogs);
        fab = _findViewById(R.id.fab_dialogs_new_chat);
        setOnRefreshListener = _findViewById(R.id.swipy_refresh_layout);

        dialogsAdapter = new DialogsAdapter(this, QbDialogHolder.getInstance().getDialogList());

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, dialogsListView, false);
        listHeader.setText(R.string.dialogs_list_hint);
        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.addHeaderView(listHeader, null, false);

        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) parent.getItemAtPosition(position);
                if (currentActionMode == null) {
                    ChatActivity.start(DialogsActivity.this, selectedDialog);
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
                }
            }
        });
        dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBDialog selectedDialog = (QBDialog) parent.getItemAtPosition(position);
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
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();

        if (privateChatManager != null) {
            privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
        }

        if (groupChatManager != null) {
            groupChatManager.addGroupChatManagerListener(groupChatManagerListener);
        }
    }

    private void unregisterQbChatListeners() {
        QBPrivateChatManager privateChatManager = QBChatService.getInstance().getPrivateChatManager();
        QBGroupChatManager groupChatManager = QBChatService.getInstance().getGroupChatManager();

        if (privateChatManager != null) {
            privateChatManager.removePrivateChatManagerListener(privateChatManagerListener);
        }

        if (groupChatManager != null) {
            groupChatManager.removeGroupChatManagerListener(groupChatManagerListener);
        }
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        ChatHelper.getInstance().createDialogWithSelectedUsers(selectedUsers,
                new QBEntityCallback<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        ChatActivity.start(DialogsActivity.this, dialog);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        showErrorSnackbar(R.string.dialogs_creation_error, e,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        createDialog(selectedUsers);
                                    }
                                });
                    }
                }
        );
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

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBDialog> dialogs, Bundle bundle) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);

                if (clearDialogHolder) {
                    QbDialogHolder.getInstance().clear();
                }
                QbDialogHolder.getInstance().addDialogs(dialogs);
                dialogsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                progressBar.setVisibility(View.GONE);
                setOnRefreshListener.setRefreshing(false);
                if (!silentUpdate || !clearDialogHolder) {
                    showErrorSnackbar(R.string.dialogs_get_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (!silentUpdate) {
                                        loadDialogsFromQb();
                                    } else {
                                        setOnRefreshListener.setRefreshing(true);
                                        loadDialogsFromQb(true, false);
                                    }
                                }
                            }).setCallback(new Snackbar.Callback() {

                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            setOnRefreshListener.setEnabled(true);
                            switch (event) {
                                case Snackbar.Callback.DISMISS_EVENT_SWIPE:
                                    if (!clearDialogHolder) {
                                        skipRecords -= ChatHelper.DIALOG_ITEMS_PER_PAGE;
                                    }
                                    break;
                            }
                        }

                        @Override
                        public void onShown(Snackbar snackbar) {
                            setOnRefreshListener.setEnabled(false);
                        }
                    });
                }
            }
        });
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
            final Collection<QBDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            ChatHelper.getInstance().deleteDialogs(selectedDialogs, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    QbDialogHolder.getInstance().deleteDialogs(selectedDialogs);
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
            Log.i(TAG, "Received broadcast " + intent.getAction() + " with data: " + message);
            loadDialogsFromQb(true, true);
        }
    }
}
