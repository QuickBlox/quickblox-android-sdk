package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;

import com.quickblox.sample.videochatwebrtcnew.R;

/**
 * Created by tereha on 26.01.15.
 */
public class BaseActivity extends ActionBarActivity {

    protected Context context;
    //protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

@Override
public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;
        actionBar = getSupportActionBar();
        //progressDialog = DialogUtils.getProgressDialog(this);
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