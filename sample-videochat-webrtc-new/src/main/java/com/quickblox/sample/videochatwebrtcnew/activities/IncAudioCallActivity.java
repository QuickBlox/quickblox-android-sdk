package com.quickblox.sample.videochatwebrtcnew.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.R;

/**
 * Created by tereha on 05.02.15.
 */
public class IncAudioCallActivity extends LogginedUserABActivity {

    private TextView timer;
    private TextView incUserName;
    private TextView otherIncUsers;
    private ImageButton callEndBtn;
    private ToggleButton dynamicToggle;
    private ToggleButton micToggle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inc_audio_call);

        super.initActionBar();

        initUI();
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

        callEndBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");
            }
        });
    }

    private void initUI() {
        timer = (TextView) findViewById(R.id.timer);
        incUserName = (TextView) findViewById(R.id.incUserName);
        otherIncUsers = (TextView) findViewById(R.id.otherIncUsers);
        callEndBtn = (ImageButton) findViewById(R.id.callEndBtn);
        dynamicToggle = (ToggleButton) findViewById(R.id.dynamicToggle);
        micToggle = (ToggleButton) findViewById(R.id.micToggle);
    }



}
