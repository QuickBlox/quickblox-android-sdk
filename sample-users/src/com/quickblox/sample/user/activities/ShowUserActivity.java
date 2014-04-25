package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;

import static com.quickblox.sample.user.definitions.Consts.POSITION;

public class ShowUserActivity extends BaseActivity {

    private TextView loginTextView;
    private TextView emailTextView;
    private TextView fullNameTextView;
    private TextView phoneTextView;
    private TextView webSiteTextView;
    private TextView tagsTextView;

    private int position;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_show_user);
        initUI();
        fillAllFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        loginTextView = (TextView) findViewById(R.id.login_textview);
        emailTextView = (TextView) findViewById(R.id.email_textview);
        fullNameTextView = (TextView) findViewById(R.id.full_name_textview);
        phoneTextView = (TextView) findViewById(R.id.phone_textview);
        webSiteTextView = (TextView) findViewById(R.id.web_site_textview);
        tagsTextView = (TextView) findViewById(R.id.tags_textview);
    }

    private void fillAllFields() {
        position = getIntent().getIntExtra(POSITION, 0);
        fillField(loginTextView, DataHolder.getDataHolder().getQBUser(position).getLogin());
        fillField(emailTextView, DataHolder.getDataHolder().getQBUser(position).getEmail());
        fillField(fullNameTextView, DataHolder.getDataHolder().getQBUser(position).getFullName());
        fillField(phoneTextView, DataHolder.getDataHolder().getQBUser(position).getPhone());
        fillField(webSiteTextView, DataHolder.getDataHolder().getQBUser(position).getWebsite());
        fillField(tagsTextView, DataHolder.getDataHolder().getQBUser(position).getTags().toString());
    }
}