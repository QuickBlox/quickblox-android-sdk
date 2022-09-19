package com.quickblox.sample.videochat.java.activities;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.messages.services.QBPushManager;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.adapters.UsersAdapter;
import com.quickblox.sample.videochat.java.db.UsersDbManager;
import com.quickblox.sample.videochat.java.executor.Executor;
import com.quickblox.sample.videochat.java.executor.ExecutorTask;
import com.quickblox.sample.videochat.java.services.CallService;
import com.quickblox.sample.videochat.java.services.LoginService;
import com.quickblox.sample.videochat.java.utils.CollectionsUtils;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.PermissionsChecker;
import com.quickblox.sample.videochat.java.utils.PushNotificationSender;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.java.utils.ToastUtils;
import com.quickblox.sample.videochat.java.utils.UsersUtils;
import com.quickblox.sample.videochat.java.utils.WebRtcSessionManager;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class OpponentsActivity extends BaseActivity {
    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final int PER_PAGE_SIZE_100 = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_DESC_UPDATED = "desc date updated_at";
    public static final String TOTAL_PAGES_BUNDLE_PARAM = "total_pages";

    private RecyclerView usersRecyclerview;
    private QBUser currentUser;
    private UsersAdapter usersAdapter;
    private int currentPage = 0;
    private Boolean isLoading = false;
    private Boolean hasNextPage = true;
    private UsersDbManager dbManager;
    private PermissionsChecker checker;

    public static void start(Context context) {
        Intent intent = new Intent(context, OpponentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);
        currentUser = SharedPrefsHelper.getInstance().getUser();
        dbManager = UsersDbManager.getInstance();
        checker = new PermissionsChecker(getApplicationContext());

        initDefaultActionBar();
        initUi();
        startLoginService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isIncomingCall = SharedPrefsHelper.getInstance().get(Consts.EXTRA_IS_INCOMING_CALL, false);
        if (isCallServiceRunning(CallService.class)) {
            Log.d(TAG, "CallService is running now");
            CallActivity.start(this, isIncomingCall);
        }
        clearAppNotifications();
        loadUsers();
    }

    private boolean isCallServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void clearAppNotifications() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startActivity(this, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void loadUsers() {
        isLoading = true;
        showProgressDialog(R.string.dlg_loading_opponents);
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ORDER_RULE, ORDER_DESC_UPDATED));
        int nextPage = currentPage + 1;
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setRules(rules);
        requestBuilder.setPerPage(PER_PAGE_SIZE_100);
        requestBuilder.setPage(nextPage);

        QBUsers.getUsers(requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                Log.d(TAG, "Successfully loaded users");
                dbManager.saveAllUsers(qbUsers, true);
                currentPage = bundle.getInt("current_page");

                int totalPages = (int) bundle.get(TOTAL_PAGES_BUNDLE_PARAM);
                if (currentPage >= totalPages) {
                    hasNextPage = false;
                }

                if (currentPage == 1) {
                    updateUsers();
                } else {
                    usersAdapter.addUsers(qbUsers);
                }
                hideProgressDialog();
                isLoading = false;
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Error load users" + e.getMessage());
                hideProgressDialog();
                isLoading = false;
                showErrorSnackbar(R.string.loading_users_error, e, v -> loadUsers());
            }
        });
    }

    private void initUi() {
        usersRecyclerview = findViewById(R.id.list_select_users);
        if (usersAdapter == null) {
            List<QBUser> opponents = dbManager.getAllUsers();
            opponents.remove(sharedPrefsHelper.getUser());
            usersAdapter = new UsersAdapter(this, opponents);
            usersAdapter.setSelectedItemsListener(this::updateActionBar);

            usersRecyclerview.setLayoutManager(new LinearLayoutManager(this));
            usersRecyclerview.setAdapter(usersAdapter);
            usersRecyclerview.addOnScrollListener(new ScrollListener((LinearLayoutManager) usersRecyclerview.getLayoutManager()));
        }
    }

    private void updateUsers() {
        List<QBUser> opponents = dbManager.getAllUsers();
        Log.d(TAG, "updateUsers opponents= " + opponents);
        opponents.remove(sharedPrefsHelper.getUser());
        usersAdapter.updateUsers(opponents);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (usersAdapter != null && !usersAdapter.getSelectedUsers().isEmpty()) {
            getMenuInflater().inflate(R.menu.activity_selected_opponents, menu);
        } else {
            getMenuInflater().inflate(R.menu.activity_opponents, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.update_opponents_list:
                currentPage = 0;
                loadUsers();
                return true;

            case R.id.log_out:
                unsubscribeFromPushes(this::logout);
                return true;

            case R.id.start_video_call:
                if (checkIsLoggedInChat()) {
                    startCall(true);
                }
                if (checker.lacksPermissions(Consts.PERMISSIONS)) {
                    startPermissionsActivity(false);
                }
                return true;

            case R.id.start_audio_call:
                if (checkIsLoggedInChat()) {
                    startCall(false);
                }
                if (checker.lacksPermissions(Consts.PERMISSIONS[1])) {
                    startPermissionsActivity(true);
                }
                return true;
            case R.id.menu_appinfo:
                AppInfoActivity.start(this);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkIsLoggedInChat() {
        if (!QBChatService.getInstance().isLoggedIn()) {
            startLoginService();
            ToastUtils.shortToast(R.string.dlg_relogin_wait);
            return false;
        }
        return true;
    }

    private void startLoginService() {
        if (sharedPrefsHelper.hasUser()) {
            QBUser qbUser = sharedPrefsHelper.getUser();
            LoginService.start(this, qbUser);
        }
    }

    private void startCall(boolean isVideoCall) {
        Log.d(TAG, "Starting Call");
        if (usersAdapter.getSelectedUsers().size() > Consts.MAX_OPPONENTS_COUNT) {
            ToastUtils.longToast(String.format(getString(R.string.error_max_opponents_count),
                    Consts.MAX_OPPONENTS_COUNT));
            return;
        }

        ArrayList<Integer> opponents = CollectionsUtils.getIdsSelectedOpponents(usersAdapter.getSelectedUsers());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
        Log.d(TAG, "conferenceType = " + conferenceType);

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());
        QBRTCSession session = qbrtcClient.createNewSessionWithOpponents(opponents, conferenceType);
        WebRtcSessionManager.getInstance(this).setCurrentSession(session);

        String sessionId = session.getSessionID();
        ArrayList<String> opponentIds = new ArrayList<>();
        ArrayList<String> opponentNames = new ArrayList<>();
        List<QBUser> selectedUsers = usersAdapter.getSelectedUsers();
        // the Caller in exactly first position is needed regarding to iOS 13 functionality
        selectedUsers.add(0, currentUser);

        for (QBUser user : selectedUsers) {
            String userId = user.getId().toString();
            String userName = TextUtils.isEmpty(user.getFullName()) ? user.getLogin() : user.getFullName();
            opponentIds.add(userId);
            opponentNames.add(userName);
        }

        String idsInLine = TextUtils.join(",", opponentIds);
        String namesInLine = TextUtils.join(",", opponentNames);

        Log.d(TAG, "New Session with Id: " + sessionId + "\n Users in Call: " + "\n" + idsInLine + "\n" + namesInLine);

        for (Integer opponentId : opponents) {
            Executor.addTask(new ExecutorTask<Boolean>() {
                @Override
                public Boolean onBackground() throws Exception {
                    long TIMEOUT_3_SECONDS = 3000L;
                    return QBChatService.getInstance().getPingManager().pingUser(opponentId, TIMEOUT_3_SECONDS);
                }

                @Override
                public void onForeground(Boolean result) {
                    if (result) {
                        Log.d(TAG, "Participant with id: " + opponentId + " is online. There is no need to send a VoIP notification.");
                    } else {
                        String name = TextUtils.isEmpty(currentUser.getFullName()) ? currentUser.getLogin() : currentUser.getFullName();
                        PushNotificationSender.sendPushMessage(opponentId, name, sessionId,
                                idsInLine, namesInLine, isVideoCall);
                    }
                }

                @Override
                public void onError(Exception exception) {
                    Log.d(TAG, "onError: " + exception);
                }
            });
        }
        CallActivity.start(this, false);
    }

    private void updateActionBar(int countSelectedUsers) {
        if (countSelectedUsers < 1) {
            initDefaultActionBar();
        } else {
            removeActionbarSubTitle();
            setActionBarTitle(String.format(getString(
                    countSelectedUsers > 1
                            ? R.string.tile_many_users_selected
                            : R.string.title_one_user_selected),
                    countSelectedUsers));
        }

        invalidateOptionsMenu();
    }

    private void logout() {
        Log.d(TAG, "Removing User data, and Logout");
        LoginService.logout(this);
        requestExecutor.signOut(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                UsersUtils.removeUserData();
                startLoginActivity();
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.error, e, v -> unsubscribeFromPushes(() -> logout()));
            }
        });
    }

    private void unsubscribeFromPushes(Callback callback) {
        if (QBPushManager.getInstance().isSubscribedToPushes()) {
            QBPushManager.getInstance().addListener(new SubscribeListener(TAG, callback));
            SubscribeService.unSubscribeFromPushes(OpponentsActivity.this);
        } else {
            callback.notifyCallback();
        }
    }

    private void startLoginActivity() {
        LoginActivity.start(this);
        finish();
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        LinearLayoutManager layoutManager;

        ScrollListener(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            if (!isLoading && hasNextPage && dy > 0) {
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                boolean needToLoadMore = ((visibleItemCount * 2) + firstVisibleItem) >= totalItemCount;
                if (needToLoadMore) {
                    loadUsers();
                }
            }
        }
    }

    private class SubscribeListener implements QBPushManager.QBSubscribeListener {
        private final String tag;
        private final Callback callback;

        public SubscribeListener(String tag, Callback callback) {
            this.tag = tag;
            this.callback = callback;
        }

        @Override
        public void onSubscriptionDeleted(boolean success) {
            QBPushManager.getInstance().removeListener(this);
            callback.notifyCallback();
        }

        @Override
        public void onSubscriptionCreated() {
            // empty
        }

        @Override
        public void onSubscriptionError(Exception e, int i) {
            // empty
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof SubscribeListener) {
                return tag.equals(((SubscribeListener) obj).tag);
            }
            return false;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + tag.hashCode();
            return hash;
        }
    }

    private interface Callback {
        void notifyCallback();
    }
}