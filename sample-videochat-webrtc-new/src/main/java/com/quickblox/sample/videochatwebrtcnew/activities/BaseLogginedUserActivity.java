package com.quickblox.sample.videochatwebrtcnew.activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.sample.videochatwebrtcnew.services.IncomeCallListenerService;
import com.quickblox.users.model.QBUser;


/**
 * Created by tereha on 26.01.15.
 */
public abstract class BaseLogginedUserActivity extends AppCompatActivity {

    private static final String TAG = BaseLogginedUserActivity.class.getSimpleName();
    private static final String VERSION_NUMBER = "0.9.4.18062015";
    private static final String APP_VERSION = "App version";
    static ActionBar mActionBar;
    private Chronometer timerABWithTimer;
    private boolean isTimerStarted = false;
    protected QBUser loginedUser;
    private String login;
    private String password;
    protected NotificationManager notificationManager;
    private BroadcastReceiver wifiStateReceiver;
    private boolean isConnectivityExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initWiFiManagerListener();

        if (QBChatService.isInitialized()) {
            if (QBChatService.getInstance().isLoggedIn()) {
                loginedUser = QBChatService.getInstance().getUser();
            }
        }

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
        getSupportActionBar().setIcon(R.drawable.logo_qb);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void initActionBar() {
        if (loginedUser != null) {

            mActionBar = getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater mInflater = LayoutInflater.from(this);

            View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);

            TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
            numberOfListAB.setBackgroundResource(resourceSelector(
                    DataHolder.getUserIndexByID(loginedUser.getId()) + 1));
            numberOfListAB.setText(String.valueOf(
                    DataHolder.getUserIndexByID(loginedUser.getId()) + 1));

            numberOfListAB.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(BaseLogginedUserActivity.this);
                    dialog.setTitle(APP_VERSION);
                    dialog.setMessage(VERSION_NUMBER);
                    dialog.show();
                    return true;
                }
            });

            TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
            loginAsAB.setText(R.string.logged_in_as);


            TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
            userNameAB.setText(DataHolder.getUserNameByID(loginedUser.getId()));

            mActionBar.setCustomView(mCustomView);
            mActionBar.setDisplayShowCustomEnabled(true);
        }
    }

    public void initActionBarWithTimer() {
        if (loginedUser != null) {
            mActionBar = getSupportActionBar();
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater mInflater = LayoutInflater.from(this);

            View mCustomView = mInflater.inflate(R.layout.actionbar_with_timer, null);

            timerABWithTimer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

            TextView loginAsABWithTimer = (TextView) mCustomView.findViewById(R.id.loginAsABWithTimer);
            loginAsABWithTimer.setText(R.string.logged_in_as);

            TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameABWithTimer);
            userNameAB.setText(DataHolder.getUserNameByID(loginedUser.getId()));

            mActionBar.setCustomView(mCustomView);
            mActionBar.setDisplayShowCustomEnabled(true);
        }
    }

    public void startTimer() {
        if (!isTimerStarted && timerABWithTimer != null && loginedUser != null) {
            timerABWithTimer.setBase(SystemClock.elapsedRealtime());
            timerABWithTimer.start();
            isTimerStarted = true;
        }
    }

    public void stopTimer(){
        if (timerABWithTimer != null){
            timerABWithTimer.stop();
            isTimerStarted = false;
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

    public void stopIncomeCallListenerService(){
        stopService(new Intent(this, IncomeCallListenerService.class));
    }

    public static int resourceSelector(int number) {
        int resStr = -1;
        switch (number) {
            case 0:
                resStr = R.drawable.shape_oval_spring_bud;
                break;
            case 1:
                resStr = R.drawable.shape_oval_orange;
                break;
            case 2:
                resStr = R.drawable.shape_oval_water_bondi_beach;
                break;
            case 3:
                resStr = R.drawable.shape_oval_blue_green;
                break;
            case 4:
                resStr = R.drawable.shape_oval_lime;
                break;
            case 5:
                resStr = R.drawable.shape_oval_mauveine;
                break;
            case 6:
                resStr = R.drawable.shape_oval_gentianaceae_blue;
                break;
            case 7:
                resStr = R.drawable.shape_oval_blue;
                break;
            case 8:
                resStr = R.drawable.shape_oval_blue_krayola;
                break;
            case 9:
                resStr = R.drawable.shape_oval_coral;
                break;
            default:
                resStr = resourceSelector(number % 10);
        }
        return resStr;
    }

    public static int selectBackgrounForOpponent(int number) {
        int resStr = -1;
        switch (number) {
            case 0:
                resStr = R.drawable.rectangle_rounded_spring_bud;
                break;
            case 1:
                resStr = R.drawable.rectangle_rounded_orange;
                break;
            case 2:
                resStr = R.drawable.rectangle_rounded_water_bondi_beach;
                break;
            case 3:
                resStr = R.drawable.rectangle_rounded_blue_green;
                break;
            case 4:
                resStr = R.drawable.rectangle_rounded_lime;
                break;
            case 5:
                resStr = R.drawable.rectangle_rounded_mauveine;
                break;
            case 6:
                resStr = R.drawable.rectangle_rounded_gentianaceae_blue;
                break;
            case 7:
                resStr = R.drawable.rectangle_rounded_blue;
                break;
            case 8:
                resStr = R.drawable.rectangle_rounded_blue_krayola;
                break;
            case 9:
                resStr = R.drawable.rectangle_rounded_coral;
                break;
            default:
                resStr = selectBackgrounForOpponent(number % 10);
        }
        return resStr;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    protected void sendNotificationConnectionLost() {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, ListUsersActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setSmallIcon(R.drawable.logo_qb)
                .setContentIntent(contentIntent)
                .setTicker(getResources().getString(R.string.service_stopped))
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.service_stopped));

        Notification notification = notificationBuilder.build();
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Consts.NOTIFICATION_CONNECTION_LOST, notification);
    }

    protected void startListUsersActivity(){
        Intent intent = new Intent(this, ListUsersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    protected void showToast(final int message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), getString(message), Toast.LENGTH_SHORT).show();
            }
        });
    }


    protected String [] getUserDataFromPreferences(){
        String [] userData = new String[2];
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedPreferences.getString(Consts.USER_LOGIN, null);
        password = sharedPreferences.getString(Consts.USER_PASSWORD, null);

        userData[0] = login;
        userData[1] = password;

        return userData;
    }

    protected boolean isUserDataEmpty(){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        login = sharedPreferences.getString(Consts.USER_LOGIN, null);
        password = sharedPreferences.getString(Consts.USER_PASSWORD, null);

        return TextUtils.isEmpty(login) && TextUtils.isEmpty(password);
    }

    protected void saveUserDataToPreferences(String login, String password){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.putString(Consts.USER_LOGIN, login);
        ed.putString(Consts.USER_PASSWORD, password);
        ed.commit();
    }

    protected void clearUserDataFromPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor ed = sharedPreferences.edit();
        ed.remove(Consts.USER_LOGIN);
        ed.remove(Consts.USER_PASSWORD);
        ed.commit();
    }

    private void reloginToChat(){
        SharedPreferences sharedPreferences = getSharedPreferences(Consts.SHARED_PREFERENCES, Context.MODE_PRIVATE);
        String login = sharedPreferences.getString(Consts.USER_LOGIN, null);
        String password = sharedPreferences.getString(Consts.USER_PASSWORD, null);

        if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
            Intent serviceIntent = new Intent(this, IncomeCallListenerService.class);
            serviceIntent.putExtra(Consts.USER_LOGIN, login);
            serviceIntent.putExtra(Consts.USER_PASSWORD, password);
            serviceIntent.putExtra(Consts.START_SERVICE_VARIANT, Consts.RELOGIN);
            startService(serviceIntent);
        }
    }

    protected void minimizeApp(){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

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

    abstract void processCurrentConnectionState(boolean isConnected);

    @Override
    protected void onDestroy() {
        if (wifiStateReceiver != null) {
            unregisterReceiver(wifiStateReceiver);
        }
        super.onDestroy();
    }
}





