package com.quickblox.sample.chat.ui.activity.base;

import android.support.v7.widget.Toolbar;

import com.quickblox.sample.chat.R;

public abstract class BaseToolbarActivity extends BaseActivity {

    protected Toolbar toolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

}
