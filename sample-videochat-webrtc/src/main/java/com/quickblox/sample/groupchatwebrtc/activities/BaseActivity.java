package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.sample.groupchatwebrtc.services.IncomeCallListenerService;
import com.quickblox.users.model.QBUser;


/**
 * QuickBlox team
 */
public class BaseActivity extends Activity {

    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final String APP_VERSION = "App version";
    static android.app.ActionBar mActionBar;
    private Chronometer timerABWithTimer;
    private boolean isStarted = false;

    private BroadcastReceiver wifiStateReceiver;
    protected boolean isConnectivityExists;

    public void initActionBar() {

        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);

        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        QBUser loggedUser = DataHolder.getLoggedUser();
        if (loggedUser != null ) {
            int number = DataHolder.getUserIndexByID(loggedUser.getId());
            numberOfListAB.setBackgroundResource(LoginActivity.resourceSelector(number));
            numberOfListAB.setText(String.valueOf(number+1));

            TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
            loginAsAB.setText(R.string.logged_in_as);
            //
            TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
            userNameAB.setText(String.valueOf(number+1));
        }

        numberOfListAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(BaseActivity.this);
                dialog.setTitle(APP_VERSION);
                dialog.setMessage(Consts.VERSION_NUMBER);
                dialog.show();
                return true;
            }});


        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    public void initActionBarWithTimer() {
        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_with_timer, null);

        timerABWithTimer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

        TextView loginAsABWithTimer = (TextView) mCustomView.findViewById(R.id.loginAsABWithTimer);
        loginAsABWithTimer.setText(R.string.logged_in_as);

        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameABWithTimer);
        QBUser user = DataHolder.getLoggedUser();
        if (user != null) {
            userNameAB.setText(user.getFullName());
        }

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    public void startTimer() {
        if (!isStarted) {
            timerABWithTimer.setBase(SystemClock.elapsedRealtime());
            timerABWithTimer.start();
            isStarted = true;
        }
    }

    public void stopTimer(){
        if (timerABWithTimer != null){
            timerABWithTimer.stop();
            isStarted = false;
        }
    }

    public void startIncomeCallListenerService(String login, String password, int startServiceVariant){
        Intent tempIntent = new Intent(this, IncomeCallListenerService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.LOGIN_TASK_CODE, tempIntent, 0);
        Intent intent = new Intent(this, IncomeCallListenerService.class);
        intent.putExtra(Consts.USER_LOGIN, login);
        intent.putExtra(Consts.USER_PASSWORD, password);
        intent.putExtra(Consts.START_SERVICE_VARIANT, startServiceVariant);
        intent.putExtra(Consts.PARAM_PINTENT, pendingIntent);
        startService(intent);
    }


    // WiFi
    //
    private void initWiFiManagerListener() {
        wifiStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Connection state was changed");

                boolean isConnected = processConnectivityState(intent);
                updateStateIfNeed(isConnected);
            }

            private boolean processConnectivityState(Intent intent) {
                int connectivityType = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -1);
                // Check does connectivity equal mobile or wifi types
                boolean connectivityState = false;
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();

                if (networkInfo != null){
                    if (connectivityType == ConnectivityManager.TYPE_MOBILE
                            || connectivityType == ConnectivityManager.TYPE_WIFI
                            || networkInfo.getTypeName().equals("WIFI")
                            || networkInfo.getTypeName().equals("MOBILE")) {
                        //should check null because in air plan mode it will be null
                        if (networkInfo.isConnected()) {
                            // Check does connectivity EXISTS for connectivity type wifi or mobile internet
                            // Pay attention on "!" symbol  in line below
                            connectivityState = true;
                        }
                    }
                }
                return connectivityState;
            }

            private void updateStateIfNeed(boolean connectionState) {
                if (isConnectivityExists != connectionState) {
                    processCurrentConnectionState(connectionState);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateReceiver, intentFilter);
    }

    void processCurrentConnectionState(boolean isConnected){

    }

    protected void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(message), Toast.LENGTH_SHORT).show();
            }
        });
    }
}




