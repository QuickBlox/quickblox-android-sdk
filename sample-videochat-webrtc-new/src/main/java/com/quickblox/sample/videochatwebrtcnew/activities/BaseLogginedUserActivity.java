package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;

import java.util.ArrayList;


/**
 * Created by tereha on 26.01.15.
 */
public class BaseLogginedUserActivity extends Activity {

    static android.app.ActionBar mActionBar;
    private Chronometer timerABWithTimer;

    public void initActionBar() {

        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);

        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector((Integer) searchIndexLogginedUser(DataHolder.createUsersList()) + 1));
        numberOfListAB.setText(String.valueOf((Integer) searchIndexLogginedUser(DataHolder.createUsersList()) + 1));

        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);

        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
        Integer index = (Integer) searchIndexLogginedUser(DataHolder.createUsersList());

        if (index >= 0) {
            userNameAB.setText(DataHolder.createUsersList().get(index).getFullName());
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
        userNameAB.setText(DataHolder.createUsersList().get(((Integer) searchIndexLogginedUser(DataHolder.createUsersList()))).getFullName());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);
    }

    private static Object searchIndexLogginedUser(ArrayList<User> usersList) {
        int indexLogginedUser = -1;

        for (User usr : usersList) {
            if (usr.getLogin().equals(CallActivity.login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }
        return indexLogginedUser;
    }

    public void startTimer() {
        timerABWithTimer.setBase(SystemClock.elapsedRealtime());
        timerABWithTimer.start();
    }
}




