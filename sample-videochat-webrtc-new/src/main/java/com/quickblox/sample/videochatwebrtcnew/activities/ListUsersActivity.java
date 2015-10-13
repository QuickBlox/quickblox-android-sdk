package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.quickblox.chat.QBChatService;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.UsersAdapter;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;


/**
 * Created by tereha on 25.01.15.
 */
public class ListUsersActivity extends BaseLogginedUserActivity {

    private static final String TAG = ListUsersActivity.class.getSimpleName();
    private UsersAdapter usersListAdapter;
    private ListView usersList;
    private static ArrayList<User> users = DataHolder.getUsersList();
    private ProgressDialog progressDialog;
    private boolean isWifiConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (QBChatService.isInitialized() && QBChatService.getInstance().isLoggedIn()) {
            startOpponentsActivity();
            finish();
        } else {
            Fabric.with(this, new Crashlytics());
            setContentView(R.layout.activity_login);

            initUI();
            initUsersList();
        }
    }

    private void initUI() {
        usersList = (ListView) findViewById(R.id.usersListView);
    }

    private void initUsersList() {
        usersListAdapter = new UsersAdapter(this, users);
        usersList.setAdapter(usersListAdapter);
        usersList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isWifiConnected) {
                    String login = usersListAdapter.getItem(position).getLogin();
                    String password = usersListAdapter.getItem(position).getPassword();
                    initProgressDialog();
                    startIncomeCallListenerService(login, password, Consts.LOGIN);
                }else {
                    showToast(R.string.internet_not_connected);
                }

            }
        });
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                Toast.makeText(ListUsersActivity.this, getString(R.string.wait_until_login_finish), Toast.LENGTH_SHORT).show();
            }
        };
        progressDialog.setMessage(getString(R.string.processes_login));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void hideProgressDialog(boolean isLoginSuccess) {
        if (progressDialog != null){
            progressDialog.dismiss();
            if (isLoginSuccess) {
                finish();
            }
        }
    }

    private void startOpponentsActivity(){
        Intent intent = new Intent(ListUsersActivity.this, OpponentsActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    void processCurrentConnectionState(boolean isConnected) {
        if (!isConnected) {
            isWifiConnected = false;
            Log.d(TAG, "Internet is turned off");
        } else {
            isWifiConnected = true;
            Log.d(TAG, "Internet is turned on");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Consts.CALL_ACTIVITY_CLOSE){
            if (resultCode == Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED) {
                Toast.makeText(this, getString(R.string.WIFI_DISABLED),Toast.LENGTH_LONG).show();
            }
        } else if (resultCode == Consts.LOGIN_RESULT_CODE){
            boolean isLoginSuccess = data.getBooleanExtra(Consts.LOGIN_RESULT, false);
            hideProgressDialog(isLoginSuccess);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
