package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

import com.quickblox.sample.content.utils.DialogUtils;

public class BaseActivity extends ActionBarActivity {

    protected final String POSITION = "position";

    protected BaseActivity baseActivity;
    protected Resources resources;
    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = this;
        resources = getResources();
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