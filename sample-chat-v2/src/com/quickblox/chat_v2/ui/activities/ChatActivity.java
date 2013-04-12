package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/11/13
 * Time: 12:53 PM
 */
public class ChatActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
    }
}
