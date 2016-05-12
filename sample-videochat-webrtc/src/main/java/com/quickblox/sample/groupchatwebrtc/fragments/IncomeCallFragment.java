package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.groupchatwebrtc.activities.CallActivity;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.RingtonePlayer;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * QuickBlox team
 */
public class IncomeCallFragment extends Fragment implements Serializable, View.OnClickListener {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private TextView callTypeTextView;
    private TextView callerNameTextView;
    private TextView otherIncUsersTextView;
    private ImageButton rejectButton;
    private ImageButton takeButton;

    private ArrayList<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private QBRTCSessionDescription sessionDescription;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private int qbConferenceType;
    private View view;
    private long lastCliclTime = 0l;
    private RingtonePlayer ringtonePlayer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList("opponents");
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
            qbConferenceType = getArguments().getInt(Consts.CONFERENCE_TYPE);


            conferenceType =
                    qbConferenceType == 1 ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO :
                            QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

            Log.d(TAG, conferenceType.toString() + "From onCreateView()");
        }

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            initUI(view);
            setDisplayedTypeCall(conferenceType);
            initButtonsListener();

        }
        ringtonePlayer = new RingtonePlayer(getActivity());
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initButtonsListener() {
        rejectButton.setOnClickListener(this);
        takeButton.setOnClickListener(this);
    }

    private void initUI(View view) {

        callTypeTextView = (TextView) view.findViewById(R.id.call_type);

        callerNameTextView = (TextView) view.findViewById(R.id.caller_name);
        callerNameTextView.setText(getCallerName(((CallActivity) getActivity()).getCurrentSession()));

        //TODO VT need fix after merge with branch master.refactoring.videochat_webrtc.lead_reviewed
        //callerNameTextView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(opponentIndexInList));

        otherIncUsersTextView = (TextView) view.findViewById(R.id.other_inc_users);
        //TODO VT need fix after merge with branch master.refactoring.videochat_webrtc.lead_reviewed
        //otherIncUsersTextView.setText(getOtherIncUsersNames(opponents));

        rejectButton = (ImageButton) view.findViewById(R.id.reject_call);
        takeButton = (ImageButton) view.findViewById(R.id.take_call);
    }

    private void enableButtons(boolean enable) {
        takeButton.setEnabled(enable);
        rejectButton.setEnabled(enable);
    }

    public void startCallNotification() {
        Log.d(TAG, "startCallNotification()");

        ringtonePlayer.play(false);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }

    }

    private void stopCallNotification() {
        Log.d(TAG, "stopCallNotification()");

        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames(ArrayList<Integer> opponents) {
        StringBuffer s = new StringBuffer("");
        opponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : opponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
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

    private String getCallerName(QBRTCSession session) {
        String s = new String();
        int i = session.getCallerID();

        opponentsFromCall.addAll(DataHolder.getUsersList());

        for (QBUser usr : opponentsFromCall) {
            if (usr.getId().equals(i)) {
                s = usr.getFullName();
            }
        }
        return s;
    }

    private void setDisplayedTypeCall(QBRTCTypes.QBConferenceType conferenceType) {
        boolean isVideoCall = conferenceType == QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;

        callTypeTextView.setText(isVideoCall ? R.string.incoming_video_call : R.string.incoming_audio_call);
        takeButton.setImageResource(isVideoCall ? R.drawable.ic_video_white : R.drawable.ic_call);
    }

    public void onStop() {
        stopCallNotification();
        super.onDestroy();
        Log.d(TAG, "onDestroy() from IncomeCallFragment");
    }

    @Override
    public void onClick(View v) {

        if ((SystemClock.uptimeMillis() - lastCliclTime) < CLICK_DELAY) {
            return;
        }
        lastCliclTime = SystemClock.uptimeMillis();

        switch (v.getId()) {
            case R.id.reject_call:
                reject();
                break;

            case R.id.take_call:
                accept();
                break;

            default:
                break;
        }
    }

    private void accept() {
        takeButton.setClickable(false);
        stopCallNotification();

        ((CallActivity) getActivity())
                .addConversationFragmentReceiveCall();

        Log.d(TAG, "Call is started");
    }

    private void reject() {
        rejectButton.setClickable(false);
        Log.d(TAG, "Call is rejected");

        stopCallNotification();

        ((CallActivity) getActivity()).rejectCurrentSession();
        ((CallActivity) getActivity()).removeIncomeCallFragment();
        ((CallActivity) getActivity()).addOpponentsFragment();
    }
}
