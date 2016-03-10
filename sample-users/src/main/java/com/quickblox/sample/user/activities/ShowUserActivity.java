package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.model.QBUser;

public class ShowUserActivity extends BaseActivity {

    private static final String QB_USER_ID = "qbUserId";
    private static final int NO_ID = -1;

    private EditText loginTextView;
    private TextView emailTextView;
    private TextView fullNameTextView;
    private TextView phoneTextView;
    private TextView tagsTextView;

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, ShowUserActivity.class);
        intent.putExtra(QB_USER_ID, id);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_show_user);
        initUI();
        fillAllFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        loginTextView = _findViewById(R.id.login_textview);
        fullNameTextView = _findViewById(R.id.full_name_textview);
        emailTextView = _findViewById(R.id.email_textview);
        phoneTextView = _findViewById(R.id.phone_textview);
        tagsTextView = _findViewById(R.id.tag_textview);
    }

    //TODO seems like we can move this method to baseActivity or even core
    protected void fillField(TextView textView, String value) {
        //TODO it's ok, but TextView already has check if value is null inside setText(value) method
        // It can be meaningful if we don't want to reset value if it's empty
        if (!TextUtils.isEmpty(value)) {
            textView.setText(value);
        }
    }

    private void fillAllFields() {
        int id = getIntent().getIntExtra(QB_USER_ID, NO_ID);
        QBUser qbUser = DataHolder.getInstance().getQBUser(id);
        fillField(loginTextView, qbUser.getLogin());
        fillField(fullNameTextView, qbUser.getFullName());
        fillField(emailTextView, qbUser.getEmail());
        fillField(phoneTextView, qbUser.getPhone());
        fillField(tagsTextView, qbUser.getTags().toString());
    }
}