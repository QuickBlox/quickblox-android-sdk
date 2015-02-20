package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.users.QBUsers;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;

import java.util.ArrayList;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment {

    private TextView incUserName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn ;
    private ImageButton takeBtn;
    private ArrayList<Integer> opponents;
    private QBRTCSessionDescription sessionDescription;
    private MediaPlayer ringtone;
    private Vibrator vibrator;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_income_call, container,false);

        ((NewDialogActivity)getActivity()).initActionBar();

        initUI(view);

        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList("opponents");
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
        }

//        incUserName.setText(getIncUserName());
//        otherIncUsers.setText(getOtherIncUsersNames(opponents));

        initButtonsListener();

        startRingtone();

        return view;
    }

    private void initButtonsListener() {

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is rejected");

                QBRTCClient.getInstance().setBusiness(false);
                ((NewDialogActivity)getActivity()).removeIncomeCallFragment();
                ((NewDialogActivity)getActivity()).getSession(sessionDescription.getSessionId())
                        .rejectCall(sessionDescription.getUserInfo());
                stopRingtone();

            }
        });

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QBRTCClient.getInstance().setBusiness(false);
                ((NewDialogActivity)getActivity())
                        .addCanversationFragmentOnSession(sessionDescription.getSessionId(),
                                ConversationFragment.StartConversetionReason.INCOME_CALL_FOR_ACCEPTION);
                ((NewDialogActivity)getActivity()).removeIncomeCallFragment();
                stopRingtone();



                Log.d("Track", "Call is started");
            }
        });
    }

    private void initUI(View view) {

        incUserName = (TextView)view.findViewById(R.id.incUserName);
        otherIncUsers = (TextView)view.findViewById(R.id.otherIncUsers);

        rejectBtn = (ImageButton)view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton)view.findViewById(R.id.takeBtn);
    }

    private void startRingtone(){

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = MediaPlayer.create(getActivity(), notification);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        ringtone.setLooping(true);
        ringtone.start();

        if (ringtone.isPlaying()) {
            long [] vibrationCycle = {0, 1000, 1000};
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(vibrationCycle, 1);
            }
        }
    }

    private void stopRingtone(){
        ringtone.stop();
        vibrator.cancel();
    }

    private String getOtherIncUsersNames (ArrayList<Integer> opponents){
        StringBuffer s = new StringBuffer("");


        for (Integer i : opponents){

            try {
                s.append(QBUsers.getUser(i).getFullName() + ", ");
            } catch (QBResponseException e) {
                e.printStackTrace();
            }
        }
        return s.toString();
    }

    private String getIncUserName (){
        String s = "";

        return s;
    }

}
