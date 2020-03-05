package com.quickblox.sample.chat.java.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.async.BaseAsyncTask;
import com.quickblox.sample.chat.java.managers.DialogsManager;
import com.quickblox.sample.chat.java.ui.adapter.DialogsAdapter;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.QbDialogHolder;
import com.quickblox.sample.chat.java.utils.qb.QbUsersHolder;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;

public class ForwardToActivity extends BaseActivity implements DialogsManager.ManagingDialogsCallbacks {

    private static final String TAG = ForwardToActivity.class.getSimpleName();

    private static final String EXTRA_FORWARD_MESSAGE = "extra_forward_message";

    private QBRequestGetBuilder requestBuilder;
    private SwipyRefreshLayout refreshLayout;
    private QBChatMessage originMessage;
    private DialogsAdapter dialogsAdapter;

    private QBUser currentUser;
    private Menu menu;

    private Boolean isProcessingResultInProgress = false;
    private DialogsManager dialogsManager = new DialogsManager();
    private boolean hasMoreDialogs = true;
    private Set<QBChatDialog> loadedDialogs = new HashSet<>();

    public static void start(Context context, QBChatMessage messageToForward) {
        Intent intent = new Intent(context, ForwardToActivity.class);
        intent.putExtra(EXTRA_FORWARD_MESSAGE, messageToForward);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);

        if (!ChatHelper.getInstance().isLogged()) {
            Log.w(TAG, "Restarting App...");
            restartApp(this);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.forward_to);
            getSupportActionBar().setSubtitle(getString(R.string.dialogs_actionmode_subtitle, "0"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (ChatHelper.getCurrentUser() != null) {
            currentUser = ChatHelper.getCurrentUser();
        } else {
            finish();
        }

        originMessage = (QBChatMessage) getIntent().getSerializableExtra(EXTRA_FORWARD_MESSAGE);
        requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(DialogsActivity.DIALOGS_PER_PAGE);
        requestBuilder.setSkip(0);
        initUi();
    }

    @Override
    public void onResumeFinished() {
        if (ChatHelper.getInstance().isLogged()) {
            loadDialogsFromQb();
        } else {
            reloginToChat();
        }
    }

    private void reloginToChat() {
        showProgressDialog(R.string.dlg_loading);
        QBUser qbUser = ChatHelper.getCurrentUser();
        if (qbUser != null) {
            ChatHelper.getInstance().loginToChat(qbUser, new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Relogin Successful");
                    loadDialogsFromQb();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Relogin Failed " + e.getMessage());
                    LoginActivity.start(ForwardToActivity.this);
                    hideProgressDialog();
                    finish();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dialogsManager.removeManagingDialogsCallbackListener(this);
    }

    private void initUi() {
        LinearLayout emptyHintLayout = findViewById(R.id.ll_chat_empty);
        ListView dialogsListView = findViewById(R.id.list_dialogs_chats);
        refreshLayout = findViewById(R.id.swipy_refresh_layout);

        List<QBChatDialog> dialogs = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        dialogsAdapter = new DialogsAdapter(this, dialogs);
        dialogsAdapter.prepareToSelect();

        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                dialogsAdapter.toggleSelection(selectedDialog);
                menu.getItem(0).setVisible(dialogsAdapter.getSelectedItems().size() >= 1);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setSubtitle(getString(R.string.dialogs_actionmode_subtitle, String.valueOf(dialogsAdapter.getSelectedItems().size())));
                }
            }
        });

        refreshLayout.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                loadDialogsFromQb();
            }
        });

        refreshLayout.setColorSchemeResources(R.color.color_new_blue, R.color.random_color_2, R.color.random_color_3, R.color.random_color_7);
        dialogsAdapter.clearSelection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_activity_forward, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (isProcessingResultInProgress) {
            return super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.menu_send) {
            showProgressDialog(R.string.dlg_sending);
            new ForwardedMessageSenderAsyncTask(this, dialogsAdapter.getSelectedItems()).execute();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void sendForwardedMessage(List<QBChatDialog> dialogs) {
        for (QBChatDialog dialog : dialogs) {
            try {
                QBChatMessage messageToForward = new QBChatMessage();
                messageToForward.setSaveToHistory(true);
                messageToForward.setDateSent(System.currentTimeMillis() / 1000);
                messageToForward.setMarkable(true);

                messageToForward.setAttachments(originMessage.getAttachments());
                if (originMessage.getBody().equals("null")) {
                    messageToForward.setBody(null);
                } else {
                    messageToForward.setBody(originMessage.getBody());
                }

                String senderName = "";
                if (originMessage.getSenderId().equals(currentUser.getId())) {
                    senderName = currentUser.getFullName();
                } else {
                    QBUser sender = QbUsersHolder.getInstance().getUserById(originMessage.getSenderId());
                    if (sender != null) {
                        senderName = sender.getFullName();
                    }
                }
                messageToForward.setProperty(ChatActivity.PROPERTY_FORWARD_USER_NAME, senderName);
                dialog.sendMessage(messageToForward);
            } catch (SmackException.NotConnectedException e) {
                Log.d(TAG, "Send Forwarded Message Exception: " + e.getMessage());
                ToastUtils.shortToast(R.string.error_forwarding_not_connected);
            }
        }
        disableProgress();
        ToastUtils.shortToast("Forwarding Complete");
        finish();
    }

    private void loadDialogsFromQb() {
        isProcessingResultInProgress = true;
        showProgressDialog(R.string.dlg_loading);

        ChatHelper.getInstance().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                if (dialogs.size() < DialogsActivity.DIALOGS_PER_PAGE) {
                    hasMoreDialogs = false;
                }
                loadedDialogs.addAll(dialogs);
                QbDialogHolder.getInstance().addDialogs(dialogs);
                updateDialogsAdapter();
                requestBuilder.setSkip(loadedDialogs.size());
                if (hasMoreDialogs) {
                    loadDialogsFromQb();
                }
                disableProgress();
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                dialogsAdapter.clearSelection();
                ToastUtils.shortToast(e.getMessage());
            }
        });
    }

    private void disableProgress() {
        isProcessingResultInProgress = false;
        hideProgressDialog();
        refreshLayout.setRefreshing(false);
    }

    private void updateDialogsAdapter() {
        ArrayList<QBChatDialog> listDialogs = new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values());
        dialogsAdapter.updateList(listDialogs);
        dialogsAdapter.prepareToSelect();
    }

    @Override
    public void onDialogCreated(QBChatDialog chatDialog) {
        loadDialogsFromQb();
    }

    @Override
    public void onDialogUpdated(String chatDialog) {
        updateDialogsAdapter();
    }

    @Override
    public void onNewDialogLoaded(QBChatDialog chatDialog) {
        updateDialogsAdapter();
    }

    private static class ForwardedMessageSenderAsyncTask extends BaseAsyncTask {
        private WeakReference<ForwardToActivity> activityRef;
        private List<QBChatDialog> dialogs;

        ForwardedMessageSenderAsyncTask(ForwardToActivity forwardToActivity, List<QBChatDialog> dialogs) {
            activityRef = new WeakReference<>(forwardToActivity);
            this.dialogs = dialogs;
        }

        @Override
        public Object performInBackground(Object[] objects) throws Exception {
            ChatHelper.getInstance().join(dialogs);
            return null;
        }

        @Override
        public void onResult(Object o) {
            activityRef.get().sendForwardedMessage(dialogs);
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            Log.d("Dialog Joiner Task", "Error: $e");
            ToastUtils.shortToast("Error: " + e.getMessage());
        }
    }
}