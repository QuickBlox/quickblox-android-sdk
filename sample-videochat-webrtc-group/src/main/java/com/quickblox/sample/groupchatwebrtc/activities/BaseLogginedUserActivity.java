package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.User;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;


/**
 * Created by tereha on 26.01.15.
 */
public class BaseLogginedUserActivity extends Activity {

    private static final String APP_VERSION = "App version";
    static android.app.ActionBar mActionBar;
    private Chronometer timerABWithTimer;
    private boolean isStarted = false;

    public void initActionBar() {

        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);

        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        User loggedUser = DataHolder.getLoggedUser();
        if (loggedUser != null ) {
            numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector(loggedUser.getUserNumber()));
            numberOfListAB.setText(String.valueOf(loggedUser.getUserNumber()));
        }

        numberOfListAB.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(BaseLogginedUserActivity.this);
                dialog.setTitle(APP_VERSION);
                dialog.setMessage(Consts.VERSION_NUMBER);
                dialog.show();
                return true;
            }});

        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);


        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);

        if (loggedUser != null ) {
            userNameAB.setText(loggedUser.getFullName());
        }

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
        User user = DataHolder.getLoggedUser();
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
}




