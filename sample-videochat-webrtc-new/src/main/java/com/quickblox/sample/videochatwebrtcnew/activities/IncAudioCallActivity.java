package com.quickblox.sample.videochatwebrtcnew.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.R;

/**
 * Created by tereha on 05.02.15.
 */
public class IncAudioCallActivity extends LogginedUserABActivity {

    private Chronometer timer;
    private TextView incUserName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn ;
    private ImageButton handUpBtn;
    private ImageButton takeBtn;
    private ToggleButton dynamicToggle;
    private ToggleButton micToggle;
    private RelativeLayout incomingCall;
    private RelativeLayout answeredCall;
    private RelativeLayout infoAboutCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_status);

        super.initActionBar();

        initUI();
        answeredCall.setVisibility(View.INVISIBLE);

        initButtonsListener();
    }

    private void initButtonsListener() {

        dynamicToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Track", "Dynamic is on!");
                } else {
                    Log.d("Track", "Dynamic is off!");
                }
            }
        });

        micToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d("Track", "Mic is on!");
                } else {
                    Log.d("Track", "Mic is off!");
                }
            }
        });

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is rejected");

            }
        });

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                incomingCall.setVisibility(View.INVISIBLE);
                answeredCall.setVisibility(View.VISIBLE);

                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();


                Log.d("Track", "Call is started");
            }
        });

        handUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");
                Intent intent = new Intent(IncAudioCallActivity.this, VideoChatActivity.class);//
                startActivity(intent);
            }
        });

    }

    private void initUI() {
        timer = (Chronometer) findViewById(R.id.timer);
        incUserName = (TextView) findViewById(R.id.incUserName);
        otherIncUsers = (TextView) findViewById(R.id.otherIncUsers);

        rejectBtn = (ImageButton) findViewById(R.id.rejectBtn);
        handUpBtn = (ImageButton) findViewById(R.id.handUpBtn);
        takeBtn = (ImageButton) findViewById(R.id.takeBtn);

        dynamicToggle = (ToggleButton) findViewById(R.id.dynamicToggle);
        micToggle = (ToggleButton) findViewById(R.id.micToggle);

        infoAboutCall = (RelativeLayout) findViewById(R.id.infoAboutCall);
        incomingCall = (RelativeLayout) findViewById(R.id.incomingCall);
        answeredCall = (RelativeLayout) findViewById(R.id.answeredCall);


    }

}
