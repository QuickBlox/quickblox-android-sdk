package com.quickblox.sample.customobjects.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.quickblox.sample.customobjects.utils.DialogUtils;

public class BaseActivity extends ActionBarActivity {

    protected BaseActivity baseActivity;
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = this;
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
}