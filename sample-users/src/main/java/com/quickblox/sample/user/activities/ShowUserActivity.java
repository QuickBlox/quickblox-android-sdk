package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.users.model.QBUser;

public class ShowUserActivity extends BaseActivity {

    private static final String QB_USER = "qbUser";

    private EditText loginTextView;
    private TextView emailTextView;
    private TextView fullNameTextView;
    private TextView phoneTextView;
    private TextView tagsTextView;

    public static void start(Context context, QBUser qbUser) {
        Intent intent = new Intent(context, ShowUserActivity.class);
        intent.putExtra(QB_USER, qbUser);
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

    private void fillAllFields() {
        QBUser qbUser = (QBUser) getIntent().getSerializableExtra(QB_USER);
        fillField(loginTextView, qbUser.getLogin());
        fillField(fullNameTextView, qbUser.getFullName());
        fillField(emailTextView, qbUser.getEmail());
        fillField(phoneTextView, qbUser.getPhone());
        fillField(tagsTextView, qbUser.getTags().toString());
    }
}