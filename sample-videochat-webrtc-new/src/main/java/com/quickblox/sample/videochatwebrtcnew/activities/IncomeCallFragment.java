package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;

import java.util.ArrayList;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment {

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
    private ArrayList<Integer> opponents;
    private QBRTCSessionDescription sessionDescription;
    boolean layoutMarker;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_income_call, container,false);




        initUI(view);
        setLayout(layoutMarker);

        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList("opponents");
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
        }

        initButtonsListener();

        return view;
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

                QBRTCClient.getInstance().setBusiness(false);
                ((NewDialogActivity)getActivity()).removeIncomeCallFragment();
                ((NewDialogActivity)getActivity()).getSession(sessionDescription.getSessionId()).rejectCall(sessionDescription.getUserInfo());

            }
        });

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timer.setBase(SystemClock.elapsedRealtime());
                timer.start();

                QBRTCClient.getInstance().setBusiness(false);
                ((NewDialogActivity)getActivity())
                        .addCanversationFragmentOnSession(sessionDescription.getSessionId(),
                                ConversationFragment.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION);
                ((NewDialogActivity)getActivity()).removeIncomeCallFragment();


                Log.d("Track", "Call is started");
            }
        });

        handUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is stopped");

            }
        });

    }

    private void initUI(View view) {
        timer = (Chronometer)view.findViewById(R.id.timer);
        incUserName = (TextView)view.findViewById(R.id.incUserName);
        otherIncUsers = (TextView)view.findViewById(R.id.otherIncUsers);

        rejectBtn = (ImageButton)view.findViewById(R.id.rejectBtn);
        handUpBtn = (ImageButton)view.findViewById(R.id.handUpBtn);
        takeBtn = (ImageButton)view.findViewById(R.id.takeBtn);

        dynamicToggle = (ToggleButton)view.findViewById(R.id.dynamicToggle);
        micToggle = (ToggleButton)view.findViewById(R.id.micToggle);

        infoAboutCall = (RelativeLayout)view.findViewById(R.id.infoAboutCall);
        incomingCall = (RelativeLayout)view.findViewById(R.id.incomingCall);
        answeredCall = (RelativeLayout)view.findViewById(R.id.answeredCall);


    }

    private void setLayout(boolean layoutMarker) {

        if (layoutMarker == false) {
            incomingCall.setVisibility(View.VISIBLE);
            answeredCall.setVisibility(View.INVISIBLE);

        } else {
            incomingCall.setVisibility(View.INVISIBLE);
            answeredCall.setVisibility(View.VISIBLE);
        }
    }

}
