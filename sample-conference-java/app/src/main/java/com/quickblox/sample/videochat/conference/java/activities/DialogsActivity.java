package com.quickblox.sample.videochat.conference.java.activities;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.navigation.NavigationView;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBIncomingMessagesManager;
import com.quickblox.chat.QBRestChatService;
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
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.adapters.DialogsAdapter;
import com.quickblox.sample.videochat.conference.java.async.BaseAsyncTask;
import com.quickblox.sample.videochat.conference.java.managers.DialogsManager;
import com.quickblox.sample.videochat.conference.java.services.CallService;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.FcmConsts;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;
import com.quickblox.sample.videochat.conference.java.utils.UiUtils;
import com.quickblox.sample.videochat.conference.java.utils.qb.QBPushSubscribeListenerImpl;
import com.quickblox.sample.videochat.conference.java.utils.qb.VerboseQbChatConnectionListener;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class DialogsActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = DialogsActivity.class.getSimpleName();

    public static final int DIALOGS_PER_PAGE = 100;
    private static final int REQUEST_SELECT_PEOPLE = 174;
    public static final int REQUEST_DIALOG_ID_FOR_UPDATE = 165;
    private static final int PLAY_SERVICES_REQUEST_CODE = 9000;
    private static final long CLICK_DELAY = 700;

    private DrawerLayout drawerLayout;

    private SwipyRefreshLayout refreshLayout;
    private ProgressBar progress;
    private boolean isProcessingResultInProgress = false;
    private BroadcastReceiver pushBroadcastReceiver;
    private ConnectionListener chatConnectionListener;

    private DialogsAdapter dialogsAdapter;
    private DialogsListener dialogsListener = new DialogsListener(TAG);
    private QBChatDialogMessageListener allDialogsMessagesListener = new AllDialogsMessageListener();
    private DialogsActivity.SystemMessagesListener systemMessagesListener = new SystemMessagesListener();
    private QBSystemMessagesManager systemMessagesManager;
    private QBIncomingMessagesManager incomingMessagesManager;
    private long lastClickTime = 0L;

    private QBUser currentUser;
    private ActionMode currentActionMode;
    private boolean hasMoreDialogs = true;
    private final Set<DialogJoinerAsyncTask> joinerTasksSet = new HashSet<>();

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void start(Context context, String dialogID) {
        Intent intent = new Intent(context, DialogsActivity.class);
        intent.putExtra(Consts.EXTRA_CERTAIN_DIALOG_ID, dialogID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String dialogToOpen = getIntent().getStringExtra(Consts.EXTRA_CERTAIN_DIALOG_ID);
        if (!TextUtils.isEmpty(dialogToOpen)) {
            Log.d(TAG, "Auto Starting ChatActivity with dialog : " + dialogToOpen);
            ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, dialogToOpen, false);
        }
        setContentView(R.layout.activity_dialogs);

        if (getChatHelper().getCurrentUser() != null) {
            currentUser = getChatHelper().getCurrentUser();
        } else {
            ToastUtils.shortToast(getApplicationContext(), "Something went wrong with application cache");
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
        if (isCallServiceRunning(CallService.class)) {
            hideProgressDialog();
            Log.d(TAG, "CallService is running now");
            CallActivity.start(this);
            return;
        }

        if (getChatHelper().isLogged()) {
            checkPlayServicesAvailable();
            registerQbChatListeners();
            if (!getQBDialogsHolder().getDialogs().isEmpty()) {
                loadDialogsFromQb(true, true);
            } else {
                loadDialogsFromQb(false, true);
            }
        } else {
            reloginToChat();
        }
    }

    private boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void reloginToChat() {
        showProgressDialog(R.string.dlg_relogin);
        if (getSharedPrefsHelper().hasQbUser()) {
            getChatHelper().loginToChat(getSharedPrefsHelper().getQbUser(), new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    Log.d(TAG, "Relogin Successful");
                    checkPlayServicesAvailable();
                    registerQbChatListeners();
                    loadDialogsFromQb(false, true);
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Relogin Failed " + e.getMessage());
                    hideProgressDialog();
                    showErrorSnackbar(R.string.error_relogin_to_chat, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reloginToChat();
                        }
                    });
                }
            });
        } else {
            hideProgressDialog();
            unsubscribeFromPushesAndLogout();
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
        QBChatService.getInstance().removeConnectionListener(chatConnectionListener);
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

    @Override
    public void onBackPressed() {
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void registerQbChatListeners() {
        QBChatService.getInstance().addConnectionListener(chatConnectionListener);
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

        if (systemMessagesManager != null) {
            systemMessagesManager.addSystemMessageListener(systemMessagesListener);
        }
        if (incomingMessagesManager != null) {
            incomingMessagesManager.addDialogMessageListener(allDialogsMessagesListener);
        }
        getDialogsManager().addDialogsCallbacks(dialogsListener);

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

        getDialogsManager().removeDialogsCallbacks(dialogsListener);
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
        switch (item.getItemId()) {
            case R.id.menu_add_chat:
                if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
                    return false;
                }
                lastClickTime = SystemClock.uptimeMillis();
                if (!isProcessingResultInProgress) {
                    showProgressDialog(R.string.dlg_loading);
                    SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE, null);
                }
                return true;
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
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
                    showProgressDialog(R.string.create_chat);
                    createDialog(selectedUsers, chatName);
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

    private void loadUpdatedDialog(String dialogId) {
        QBRestChatService.getChatDialogById(dialogId).performAsync(new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                getQBDialogsHolder().addDialog(qbChatDialog);
                updateDialogsAdapter();
                isProcessingResultInProgress = false;
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                }
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
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().destroy();
                getSharedPrefsHelper().removeQbUser();
                getQBDialogsHolder().clear();
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

    private void unsubscribeFromPushesAndLogout() {
        showProgressDialog(R.string.dlg_logout);
        if (QBPushManager.getInstance().isSubscribedToPushes()) {
            QBPushManager.getInstance().addListener(new QBPushSubscribeListenerImpl() {
                @Override
                public void onSubscriptionDeleted(boolean success) {
                    Log.d(TAG, "Subscription Deleted");
                    QBPushManager.getInstance().removeListener(this);
                    userLogout();
                }
            });
            SubscribeService.unSubscribeFromPushes(DialogsActivity.this);
        } else {
            userLogout();
        }
    }

    private void initUi() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        TextView tvUserName = headerView.findViewById(R.id.nav_head_username);
        TextView tvUserLogin = headerView.findViewById(R.id.nav_head_login);
        ImageView ivUserAvatar = headerView.findViewById(R.id.image_user_avatar);
        TextView tvAvatarTitle = headerView.findViewById(R.id.user_avatar_title);
        TextView tvUserID = headerView.findViewById(R.id.nav_head_user_id);

        tvUserName.setText(currentUser.getFullName());
        tvUserLogin.setText("Login: " + currentUser.getLogin());
        tvUserID.setText("User ID: " + currentUser.getId());
        tvAvatarTitle.setText(currentUser.getFullName().substring(0, 1).toUpperCase());
        ivUserAvatar.setImageDrawable(UiUtils.getColorCircleDrawable(getApplicationContext(), currentUser.getId()));

        LinearLayout emptyHintLayout = findViewById(R.id.ll_chat_empty);
        ListView dialogsListView = findViewById(R.id.list_dialogs_chats);
        refreshLayout = findViewById(R.id.swipy_refresh_layout);
        progress = findViewById(R.id.pb_dialogs);

        ArrayList<QBChatDialog> dialogs = new ArrayList<>(getQBDialogsHolder().getDialogs().values());
        dialogsAdapter = new DialogsAdapter(this, dialogs);

        dialogsListView.setEmptyView(emptyHintLayout);
        dialogsListView.setAdapter(dialogsAdapter);

        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
                    return;
                }
                lastClickTime = SystemClock.uptimeMillis();

                final QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                if (currentActionMode != null) {
                    dialogsAdapter.toggleSelection(selectedDialog);
                    String subtitle;
                    if (dialogsAdapter.getSelectedItems().size() != 1) {
                        subtitle = getString(R.string.dialogs_actionmode_subtitle, String.valueOf(dialogsAdapter.getSelectedItems().size()));
                    } else {
                        subtitle = getString(R.string.dialogs_actionmode_subtitle_single, String.valueOf(dialogsAdapter.getSelectedItems().size()));
                    }
                    currentActionMode.setSubtitle(subtitle);
                    currentActionMode.getMenu().getItem(0).setVisible(dialogsAdapter.getSelectedItems().size() >= 1);
                } else {
                    showProgressDialog(R.string.dlg_login);
                    getChatHelper().loginToChat(currentUser, new QBEntityCallback<Void>() {
                        @Override
                        public void onSuccess(Void aVoid, Bundle bundle) {
                            hideProgressDialog();
                            ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, selectedDialog.getDialogId(), false);
                        }

                        @Override
                        public void onError(QBResponseException e) {
                            hideProgressDialog();
                            ToastUtils.shortToast(getApplicationContext(), e.getMessage());
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
        getChatHelper().createDialogWithSelectedUsers(selectedUsers, chatName,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        Log.d(TAG, "Creating Dialog Successful");
                        isProcessingResultInProgress = false;
                        getDialogsManager().sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        ArrayList<QBChatDialog> dialogsToJoin = new ArrayList<>();
                        dialogsToJoin.add(dialog);
                        new DialogJoinerAsyncTask(DialogsActivity.this, dialogsToJoin).execute();
                        getQBDialogsHolder().addDialog(dialog);
                        ChatActivity.startForResult(DialogsActivity.this, REQUEST_DIALOG_ID_FOR_UPDATE, dialog.getDialogId(), true);
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
            if (progress == null) {
                progress = findViewById(R.id.pb_dialogs);
            }
            progress.setVisibility(View.VISIBLE);
        } else {
            showProgressDialog(R.string.dlg_loading);
        }
        QBRequestGetBuilder requestBuilder = new QBRequestGetBuilder();
        requestBuilder.setLimit(DIALOGS_PER_PAGE);
        requestBuilder.setSkip(clearDialogHolder ? 0 : getQBDialogsHolder().getDialogs().size());

        getChatHelper().getDialogs(requestBuilder, new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> dialogs, Bundle bundle) {
                if (dialogs.size() < DIALOGS_PER_PAGE) {
                    hasMoreDialogs = false;
                }
                if (clearDialogHolder) {
                    getQBDialogsHolder().clear();
                    hasMoreDialogs = true;
                }
                getQBDialogsHolder().addDialogs(dialogs);
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
                ToastUtils.shortToast(getApplicationContext(), e.getMessage());
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
        chatConnectionListener = new VerboseQbChatConnectionListener(getApplicationContext(), rootView) {
            @Override
            public void reconnectionSuccessful() {
                super.reconnectionSuccessful();
                loadDialogsFromQb(false, true);
            }

            @Override
            public void reconnectionFailed(Exception error) {
                super.reconnectionFailed(error);
                showErrorSnackbar(R.string.reconnect_failed, error, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reloginToChat();
                    }
                });
            }
        };
    }

    private void updateDialogsAdapter() {
        ArrayList<QBChatDialog> listDialogs = new ArrayList<>(getQBDialogsHolder().getDialogs().values());
        dialogsAdapter.updateList(listDialogs);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return false;
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (menuItem.getItemId()) {
            case R.id.menu_video_settings: {
                SettingsActivity.start(this, true);
                break;
            }
            case R.id.menu_audio_settings: {
                SettingsActivity.start(this, false);
                break;
            }
            case R.id.menu_appinfo: {
                AppInfoActivity.start(this);
                break;
            }
            case R.id.menu_dialogs_action_logout: {
                isProcessingResultInProgress = true;
                menuItem.setEnabled(false);
                invalidateOptionsMenu();
                unsubscribeFromPushesAndLogout();
                break;
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                    openAlertDialog();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            dialogsAdapter.clearSelection();
        }

        private void openAlertDialog() {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DialogsActivity.this, R.style.AlertDialogStyle);
            alertDialogBuilder.setTitle(getString(R.string.dlg_delete_dialogs));
            alertDialogBuilder.setMessage(getString(R.string.dlg_delete_question));
            alertDialogBuilder.setCancelable(false);

            alertDialogBuilder.setPositiveButton(getString(R.string.dlg_delete), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteSelected();
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                }
            });
            alertDialogBuilder.setNegativeButton(getString(R.string.dlg_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                }
            });
            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }

        private void deleteSelected() {
            showProgressDialog(R.string.dlg_deleting_chats);
            List<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            List<QBChatDialog> groupDialogsToDelete = new ArrayList<>();
            List<QBChatDialog> privateDialogsToDelete = new ArrayList<>();

            for (QBChatDialog dialog : selectedDialogs) {
                if (dialog.getType().equals(QBDialogType.PUBLIC_GROUP)) {
                    ToastUtils.shortToast(getApplicationContext(), getString(R.string.dialogs_cannot_delete_chat, dialog.getName()));
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
            getChatHelper().deletePrivateDialogs(privateDialogsToDelete, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
                    Log.d(TAG, "PRIVATE Dialogs Deleting Successful");
                    getQBDialogsHolder().deleteDialogs(dialogsIds);
                    updateDialogsAdapter();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "Deleting PRIVATE Dialogs Error: " + e.getMessage());
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            v -> deletePrivateDialogs(privateDialogsToDelete));
                }
            });
        }

        private void leaveGroupDialogs(final List<QBChatDialog> groupDialogsToDelete) {
            getChatHelper().leaveGroupDialogs(groupDialogsToDelete, new QBEntityCallback<List<QBChatDialog>>() {
                @Override
                public void onSuccess(List<QBChatDialog> qbChatDialogs, Bundle bundle) {
                    Log.d(TAG, "GROUP Dialogs Deleting Successful");
                    getQBDialogsHolder().deleteDialogs(qbChatDialogs);
                    updateDialogsAdapter();
                    hideProgressDialog();
                }

                @Override
                public void onError(QBResponseException e) {
                    hideProgressDialog();
                    Log.d(TAG, "Deleting GROUP Dialogs Error: " + e.getMessage());
                    runOnUiThread(() -> ToastUtils.longToast(getApplicationContext(), R.string.dialogs_deletion_error));
                }
            });
        }

        private void notifyDialogsLeave(List<QBChatDialog> dialogsToNotify) {
            for (final QBChatDialog dialog : dialogsToNotify) {
                getDialogsManager().sendMessageLeftUser(dialog);
                getDialogsManager().sendSystemMessageLeftUser(systemMessagesManager, dialog);
                try {
                    Thread.sleep(300);
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
            getDialogsManager().onSystemMessageReceived(qbChatMessage);
        }

        @Override
        public void processError(QBChatException ignored, QBChatMessage qbChatMessage) {

        }
    }

    private class AllDialogsMessageListener implements QBChatDialogMessageListener {
        @Override
        public void processMessage(final String dialogId, final QBChatMessage qbChatMessage, Integer senderId) {
            Log.d(TAG, "Processing received Message: " + qbChatMessage.getBody());
            if (!senderId.equals(currentUser.getId())) {
                getDialogsManager().onGlobalMessageReceived(dialogId, qbChatMessage);
            }
        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {
            Log.d(TAG, "Processing Received ERROR: " + e.getMessage() + qbChatMessage.getBody());
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
                activityRef.get().getChatHelper().join(dialogs);
            }
            return null;
        }

        @Override
        public void onResult(Void aVoid) {
            if (!isCancelled() && activityRef.get() != null && activityRef.get().hasMoreDialogs) {
                activityRef.get().disableProgress();
            }
        }

        @Override
        public void onException(Exception e) {
            super.onException(e);
            if (!isCancelled() && activityRef.get() != null) {
                Log.d(TAG + "Joiner Task", "Error: " + e.getMessage());
                ToastUtils.shortToast(activityRef.get(), "Error: " + e.getMessage());
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            cancel(true);
        }
    }

    private class DialogsListener implements DialogsManager.DialogsCallbacks {
        private String tag;

        DialogsListener(String tag) {
            this.tag = tag;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 53 * hash + tag.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            boolean equals;
            if (obj instanceof DialogsListener) {
                equals = TAG.equals(((DialogsListener) obj).tag);
            } else {
                equals = super.equals(obj);
            }
            return equals;
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
    }
}