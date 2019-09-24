package com.quickblox.sample.chat.ui.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.utils.ErrorUtils;

import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;

public abstract class BaseActivity extends CoreBaseActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    protected ActionBar actionBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actionBar = getSupportActionBar();

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt("dummy_value", 0);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    protected abstract View getSnackbarAnchorView();

    protected Snackbar showErrorSnackbar(@StringRes int resId, Exception e,
                                         View.OnClickListener clickListener) {
        return ErrorUtils.showSnackbar(getSnackbarAnchorView(), resId, e,
                com.quickblox.sample.core.R.string.dlg_retry, clickListener);
    }
}