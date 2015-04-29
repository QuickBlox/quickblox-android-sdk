package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;

import com.quickblox.sample.chat.core.ChatService;

/**
 * Created by igorkhomenko on 4/29/15.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onResume() {
        super.onResume();

        ChatService.init(this);
    }
}
