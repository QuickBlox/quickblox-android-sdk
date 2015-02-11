package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.User;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import static com.quickblox.sample.videochatwebrtcnew.activities.OpponentsActivity.getQBPagedRequestBuilder;

/**
 * Created by tereha on 26.01.15.
 */
public class LogginedUserABActivity extends Activity {

    static android.app.ActionBar mActionBar;

    public void initActionBar() {

        mActionBar = getActionBar();
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);

        LayoutInflater mInflater = LayoutInflater.from(this);

        View mCustomView = mInflater.inflate(R.layout.actionbar_view, null);
        TextView numberOfListAB = (TextView) mCustomView.findViewById(R.id.numberOfListAB);
        numberOfListAB.setBackgroundResource(ListUsersActivity.resourceSelector((Integer) searchIndexLogginedUser(DataHolder.createUsersList()) +1));
        numberOfListAB.setText(String.valueOf((Integer) searchIndexLogginedUser(DataHolder.createUsersList()) +1));
        TextView loginAsAB = (TextView) mCustomView.findViewById(R.id.loginAsAB);
        loginAsAB.setText(R.string.logged_in_as);
        TextView userNameAB = (TextView) mCustomView.findViewById(R.id.userNameAB);
        userNameAB.setText(DataHolder.createUsersList().get(((Integer) searchIndexLogginedUser(DataHolder.createUsersList()))).getFullName());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);

    }

    private static Object searchIndexLogginedUser(ArrayList<User> usersList) {
        int indexLogginedUser = -1;

        for (User usr : usersList) {
            if (usr.getLogin().equals(OpponentsActivity.login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }
}




