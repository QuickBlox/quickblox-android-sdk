package com.quickblox.sample.customobjects.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.DialogUtils;

import androidx.appcompat.app.ActionBar;

public abstract class BaseActivity extends CoreBaseActivity {

    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }
}