package com.quickblox.sample.groupchatwebrtc.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.adapters.OpponentsAdapter;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;

/**
 * QuickBlox team
 */
public class OpponentsActivity extends BaseLogginedUserActivity {
    private static final String TAG = OpponentsActivity.class.getSimpleName();

    private static final long ON_ITEM_CLICK_DELAY = TimeUnit.SECONDS.toMillis(10);

    private static ArrayList<QBUser> opponentsList = new ArrayList<>();

    private OpponentsAdapter opponentsAdapter;
    private ListView opponentsListView;
    private QBUser currenUser;
    private String currentRoomName;
    private SharedPrefsHelper sharedPrefsHelper;

    public static void start(Context context){
        Intent intent = new Intent(context, OpponentsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.fragment_opponents);

        initFields();

        initDefaultActionBar();

        initUi();

        startLoadUsers();
    }

    private void initFields() {
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
        currenUser = sharedPrefsHelper.getQbUser();
        currentRoomName = sharedPrefsHelper.get(Consts.PREF_CURREN_ROOM_NAME);
    }

    private void startLoadUsers() {
        showProgressDialog(R.string.dlg_loading_opponents);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);
        App.getInstance().getQbResRequestExecutor().loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                hideProgressDialog();
                setOpponentsList(result);
                initUsersList();
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                ErrorUtils.showSnackbar(getCurrentFocus(), R.string.loading_users_error, responseException,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startLoadUsers();
                            }
                        });
            }
        });
    }

    public static ArrayList<QBUser> getOpponentsList() {
        return opponentsList;
    }

    public static void setOpponentsList(ArrayList<QBUser> opponentsList) {
        OpponentsActivity.opponentsList = opponentsList;
    }

    private void initUi() {
        opponentsListView = (ListView) findViewById(R.id.list_opponents);
    }

    private void initUsersList() {
        opponentsAdapter = new OpponentsAdapter(this, getOpponentsList());

        opponentsListView.setAdapter(opponentsAdapter);
        opponentsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                opponentsAdapter.toggleSelection(position);
                view.invalidate();
                updateActionBar(opponentsAdapter.getSelectedItems().size());
                Log.d(TAG, "item " + position + " clicked");
            }
        });
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
                //start video call
                return true;

            case R.id.start_audio_call:
                //start audio call
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initDefaultActionBar(){
        setActionbarTitle(currentRoomName);
        setActionbarSubTitle(String.format(getString(R.string.logged_in_as), currenUser.getFullName()));
    }

    private void initActionBarWithSelectedUsers(int countSelectedUsers){
        setActionbarTitle(String.format(getString(
                countSelectedUsers > 1
                    ? R.string.users_selected
                    : R.string.user_selected),
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
        showProgressDialog(R.string.dlg_logout);
        QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle params) {
                removeUserData();
                finish();
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                hideProgressDialog();
                Toaster.longToast(R.string.sign_up_error);
            }
        });
    }

    @Override
    public void onBackPressed() {
        logOut();
        removeUserData();
        super.onBackPressed();
    }

    private void removeUserData(){
        if (sharedPrefsHelper == null) {
            sharedPrefsHelper = SharedPrefsHelper.getInstance();
        }

        sharedPrefsHelper.removeQbUser();
        sharedPrefsHelper.delete(Consts.PREF_CURREN_ROOM_NAME);
    }

    public void showSettings() {
        SettingsActivity.start(this);
    }
}
