package com.quickblox.sample.user.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.DialogUtils;

public abstract class BaseActivity extends CoreBaseActivity {

    protected ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = DialogUtils.getProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    protected abstract void initUI();
}