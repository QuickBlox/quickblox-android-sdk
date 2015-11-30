package com.quickblox.sample.user.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.quickblox.sample.user.R;
import com.quickblox.sample.user.utils.DialogUtils;

public class BaseActivity extends ActionBarActivity {

    protected Context context;
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        context = this;
        actionBar = getSupportActionBar();
        progressDialog = DialogUtils.getProgressDialog(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void fillField(TextView textView, String value) {
        if (!TextUtils.isEmpty(value)) {
            textView.setText(value);
        }
    }
}