package com.quickblox.sample.content.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;

import com.quickblox.sample.core.utils.DialogUtils;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;

public class BaseActivity extends CoreBaseActivity {

    // TODO Move to ShowImageActivity
    protected static final String EXTRA_QBFILE_ID = "id";

    protected ActionBar actionBar;
    protected ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();
        progressDialog = DialogUtils.getProgressDialog(this);
    }
}