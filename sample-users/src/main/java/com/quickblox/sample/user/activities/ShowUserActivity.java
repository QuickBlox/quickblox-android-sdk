package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.quickblox.sample.user.R;
import com.quickblox.sample.user.databinding.ActivityShowUserBinding;
import com.quickblox.users.model.QBUser;

public class ShowUserActivity extends BaseActivity {

    private static final String QB_USER = "qbUser";

    private ActivityShowUserBinding showUserBinding;

    public static void start(Context context, QBUser qbUser) {
        Intent intent = new Intent(context, ShowUserActivity.class);
        intent.putExtra(QB_USER, qbUser);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        showUserBinding = DataBindingUtil.setContentView(this, R.layout.activity_show_user);
        actionBar.setDisplayHomeAsUpEnabled(true);
        fillAllFields();
    }

    private void fillAllFields() {
        QBUser qbUser = (QBUser) getIntent().getSerializableExtra(QB_USER);
        showUserBinding.setUserFull(qbUser);
    }
}