package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.errors.QBChatErrorsConstants;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.gcm.GooglePlayServicesHelper;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsAdapter;
import com.quickblox.sample.groupchatwebrtc.db.QbUsersDbManager;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.sample.groupchatwebrtc.utils.CollectionsUtils;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.PushNotificationSender;
import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * QuickBlox team
 */
public class OpponentsActivity extends BaseActivity {
    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private QBUser currentUser;
    private GooglePlayServicesHelper googlePlayServicesHelper;
    private ArrayList<QBUser> currentOpponentsList;
    private QbUsersDbManager dbManager;
    private boolean isRunedForCall;
    private WebRtcSessionManager webRtcSessionManager;


    public static void start(Context context, boolean isRunForCall){
        Intent intent = new Intent(context, OpponentsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(Consts.EXTRA_IS_STARTED_FOR_CALL, isRunForCall);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_opponents);

        initFields();

        subscribeToPushes();

        initDefaultActionBar();

        initUi();

        startLoginToChat();

        if (isRunedForCall && webRtcSessionManager.getCurrentSession() != null) {
            CallActivity.start(OpponentsActivity.this, true);
        }
    }

    private void startLoginToChat() {
        showProgressDialog(R.string.dlg_login);
        startLoginService(currentUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess || QBChatErrorsConstants.ALREADY_LOGGED_IN.equals(errorMessage)) {
                startLoadUsers();
            } else {
                showErrorSnackbar(R.string.login_chat_login_error, new Exception(errorMessage), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startLoginToChat();
                    }
                });
            }
        }
    }

    private void startLoginService(QBUser qbUser){
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null){
            isRunedForCall = intent.getExtras().getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
            if (isRunedForCall && webRtcSessionManager.getCurrentSession() != null){
                CallActivity.start(OpponentsActivity.this, true);
            }
        }
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_opponents);
    }

    private void initFields() {
        Bundle extras = getIntent().getExtras();
        if (extras != null){
            isRunedForCall = extras.getBoolean(Consts.EXTRA_IS_STARTED_FOR_CALL);
        }

        googlePlayServicesHelper = new GooglePlayServicesHelper();
        currentUser = sharedPrefsHelper.getQbUser();
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }

    private void subscribeToPushes() {
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            Log.d(TAG, "subscribeToPushes()");
            googlePlayServicesHelper.registerForGcm(Consts.GCM_SENDER_ID);
        }
    }

    private void startLoadUsers() {
        showProgressDialog(R.string.dlg_loading_opponents);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);
        App.getInstance().getQbResRequestExecutor().loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                hideProgressDialog();
                dbManager.saveAllUsers(result, true);
                initUsersList();
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                showErrorSnackbar(R.string.loading_users_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startLoadUsers();
                    }
                });
            }
        });
    }

    private void initUi() {
        opponentsListView = (ListView) findViewById(R.id.list_opponents);
    }

    private void initUsersList() {
        currentOpponentsList = dbManager.getAllUsers();
        currentOpponentsList.remove(QBChatService.getInstance().getUser());

        opponentsAdapter = new OpponentsAdapter(this, currentOpponentsList);
        opponentsAdapter.setSelectedItemsCountsChangedListener(new OpponentsAdapter.SelectedItemsCountsChangedListener() {
            @Override
            public void onCountSelectedItemsChanged(int count) {
                updateActionBar(count);
            }
        });

        opponentsListView.setAdapter(opponentsAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (opponentsAdapter != null && !opponentsAdapter.getSelectedItems().isEmpty()){
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
                startLoadUsers();
                return true;

            case R.id.settings:
                showSettings();
                return true;

            case R.id.log_out:
                logOut();
                return true;

            case R.id.start_video_call:
                startCall(true);
                return true;

            case R.id.start_audio_call:
                startCall(false);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSettings() {
        SettingsActivity.start(this);
    }

    private void startCall(boolean isVideoCall) {
        ArrayList<Integer> opponentsList = CollectionsUtils.getIdsSelectedOpponents(opponentsAdapter.getSelectedItems());
        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        QBRTCClient qbrtcClient = QBRTCClient.getInstance(getApplicationContext());

        QBRTCSession newQbRtcSession = qbrtcClient.createNewSessionWithOpponents(opponentsList, conferenceType);

        WebRtcSessionManager.getInstance(this).setCurrentSession(newQbRtcSession);

        PushNotificationSender.sendPushMessage(opponentsList, currentUser.getFullName());

        CallActivity.start(this, false);
    }

    private void initActionBarWithSelectedUsers(int countSelectedUsers){
        setActionBarTitle(String.format(getString(
                countSelectedUsers > 1
                    ? R.string.tile_many_users_selected
                    : R.string.title_one_user_selected),
                countSelectedUsers));
    }

    private void updateActionBar(int countSelectedUsers){
        if (countSelectedUsers < 1 ){
            initDefaultActionBar();
        } else {
            removeActionbarSubTitle();
            initActionBarWithSelectedUsers(countSelectedUsers);
        }

        invalidateOptionsMenu();
    }

    private void logOut() {
        startLogoutCommand();
        unsubscribeFromPushes();
        removeUserData();
        startLoginActivity();
    }

    private void startLogoutCommand(){
        CallService.logout(this);
    }

    private void unsubscribeFromPushes() {
        if (googlePlayServicesHelper.checkPlayServicesAvailable(this)) {
            Log.d(TAG, "unsubscribeFromPushes()");
            googlePlayServicesHelper.unregisterFromGcm(Consts.GCM_SENDER_ID);
        }
    }

    private void removeUserData(){
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }

        sharedPrefsHelper.removeQbUser();
        sharedPrefsHelper.delete(Consts.PREF_CURREN_ROOM_NAME);
        sharedPrefsHelper.delete(Consts.PREF_CURRENT_TOKEN);
        sharedPrefsHelper.delete(Consts.PREF_TOKEN_EXPIRATION_DATE);
        dbManager.clearDB();
    }

    private void startLoginActivity(){
        LoginActivity.start(this);
        finish();
    }
}
