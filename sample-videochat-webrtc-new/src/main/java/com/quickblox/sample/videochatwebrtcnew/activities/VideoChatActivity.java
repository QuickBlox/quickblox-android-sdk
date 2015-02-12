package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;


import com.quickblox.sample.videochatwebrtcnew.R;

import java.util.ArrayList;

/**
 * Created by tereha on 03.02.15.
 */
public class VideoChatActivity extends LogginedUserABActivity {

    //private ArrayList <Opponent> opponentsToCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        super.initActionBar();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_video_chat, menu);
        return true;
    }
}
