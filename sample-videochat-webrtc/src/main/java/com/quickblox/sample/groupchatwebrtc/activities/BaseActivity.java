package com.quickblox.sample.groupchatwebrtc.activities;

import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.users.model.QBUser;


/**
 * QuickBlox team
 */
public class BaseActivity extends CoreBaseActivity {

    private static final String APP_VERSION = "App version";
    private Chronometer timerABWithTimer;
    private boolean isStarted = false;
    SharedPrefsHelper sharedPrefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefsHelper = SharedPrefsHelper.getInstance();
    }

    public void initActionBarWithTimer() {
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);

            LayoutInflater mInflater = LayoutInflater.from(this);

            View mCustomView = mInflater.inflate(R.layout.actionbar_with_timer, null);

            timerABWithTimer = (Chronometer) mCustomView.findViewById(R.id.timerABWithTimer);

            TextView loginAsABWithTimer = (TextView) mCustomView.findViewById(R.id.loginAsABWithTimer);
            loginAsABWithTimer.setText(R.string.logged_in_as);

            TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameABWithTimer);
            QBUser user = QBChatService.getInstance().getUser();
            if (user != null) {
                userNameAB.setText(user.getFullName());
            }

            actionBar.setCustomView(mCustomView);
            actionBar.setDisplayShowCustomEnabled(true);
        }
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

    public void initDefaultActionBar(){
        String currentUserFullName = "";
        String currentRoomName = sharedPrefsHelper.get(Consts.PREF_CURREN_ROOM_NAME, "");

        if (sharedPrefsHelper.getQbUser() != null){
            currentUserFullName = sharedPrefsHelper.getQbUser().getFullName();
        }

        setActionBarTitle(currentRoomName);
        setActionbarSubTitle(String.format(getString(R.string.logged_in_as), currentUserFullName));
    }


    public void setActionbarSubTitle(String subTitle){
        if (actionBar != null)
            actionBar.setSubtitle(subTitle);
    }

    public void removeActionbarSubTitle(){
        if (actionBar != null)
            actionBar.setSubtitle(null);
    }

    void showProgressDialog(@StringRes int messageId){
        ProgressDialogFragment.show(getSupportFragmentManager(), messageId);
    }

    void hideProgressDialog(){
        ProgressDialogFragment.hide(getSupportFragmentManager());
    }
}




