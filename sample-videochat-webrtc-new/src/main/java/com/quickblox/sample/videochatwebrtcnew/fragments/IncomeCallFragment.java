package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.content.Context;
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

import com.quickblox.core.QBSettings;
import com.quickblox.sample.videochatwebrtcnew.ApplicationSingleton;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.helper.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCSessionDescription;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment implements Serializable {

    private TextView incVideoCall;
    private TextView incAudioCall;
    private TextView callerName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn ;
    private ImageButton takeBtn;

    private ArrayList<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private QBRTCSessionDescription sessionDescription;
    private MediaPlayer ringtone;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private int qbConferenceType;
    private View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList("opponents");
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
            qbConferenceType = getArguments().getInt(ApplicationSingleton.CONFERENCE_TYPE);


            conferenceType =
                    qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            Log.d("Track", conferenceType.toString() + "From onCreateView()");
        }

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            ((CallActivity) getActivity()).initActionBar();

            initUI(view);
            setDisplayedTypeCall(conferenceType);
            initButtonsListener();
            startCallNotification();

        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        QBSettings.getInstance().fastConfigInit(Consts.APP_ID, Consts.AUTH_KEY, Consts.AUTH_SECRET);

        Log.d("Track", "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    public void onDestroy() {
        stopCallNotification();
        super.onDestroy();
        Log.d("Track", "onDestroy() from IncomeCallFragment");
    }

    private void initButtonsListener() {

        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Track", "Call is rejected");

                stopCallNotification();

                ((CallActivity)getActivity()).getSession(sessionDescription.getSessionId())
                        .rejectCall(sessionDescription.getUserInfo());
                ((CallActivity)getActivity()).removeIncomeCallFragment();
                ((CallActivity)getActivity()).addOpponentsFragment();

            }
        });

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                stopCallNotification();

                    ((CallActivity) getActivity())
                            .addConversationFragmentReceiveCall(sessionDescription.getSessionId());
                    ((CallActivity) getActivity()).removeIncomeCallFragment();

                Log.d("Track", "Call is started");
            }
        });
    }

    private void initUI(View view) {

        incAudioCall = (TextView)view.findViewById(R.id.incAudioCall);
        incVideoCall = (TextView)view.findViewById(R.id.incVideoCall);

        callerName = (TextView)view.findViewById(R.id.callerName);
        callerName.setText(getCallerName(((CallActivity)getActivity()).getSession(sessionDescription.getSessionId())));

        otherIncUsers = (TextView)view.findViewById(R.id.otherIncUsers);
        otherIncUsers.setText(getOtherIncUsersNames(opponents));

        rejectBtn = (ImageButton)view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton)view.findViewById(R.id.takeBtn);
    }

    public void startCallNotification(){

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = MediaPlayer.create(getActivity(), notification);

//        ringtone.setLooping(true);
        ringtone.start();

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification(){
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e){
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
            ringtone.release();
            ringtone = null;
        }

        if (vibrator != null){
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames (ArrayList<Integer> opponents){
        StringBuffer s = new StringBuffer("");
        opponentsFromCall.addAll(DataHolder.createUsersList());

        for (Integer i : opponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)){
                    if (opponents.indexOf(i) == (opponents.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }

    private String getCallerName (QBRTCSession session){
        String s = new String();
        int i = session.getCallerID();

        opponentsFromCall.addAll(DataHolder.createUsersList());

        for (QBUser usr : opponentsFromCall){
            if (usr.getId().equals(i)){
                s = usr.getFullName();
            }
        }
        return s;
    }

    private void setDisplayedTypeCall(QBRTCTypes.QBConferenceType conferenceType){
        if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO) {
            incVideoCall.setVisibility(View.VISIBLE);
            incAudioCall.setVisibility(View.INVISIBLE);
        } else if (conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO){
            incVideoCall.setVisibility(View.INVISIBLE);
            incAudioCall.setVisibility(View.VISIBLE);
        }
    }
}
