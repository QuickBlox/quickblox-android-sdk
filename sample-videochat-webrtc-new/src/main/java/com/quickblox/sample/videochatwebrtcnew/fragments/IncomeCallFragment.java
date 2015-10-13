package com.quickblox.sample.videochatwebrtcnew.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.SessionManager;
import com.quickblox.sample.videochatwebrtcnew.activities.BaseLogginedUserActivity;
import com.quickblox.sample.videochatwebrtcnew.activities.CallActivity;
import com.quickblox.sample.videochatwebrtcnew.definitions.Consts;
import com.quickblox.sample.videochatwebrtcnew.holder.DataHolder;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment implements Serializable {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private TextView typeIncCallView;
    private TextView callerName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn;
    private ImageButton takeBtn;

    private List<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private MediaPlayer ringtone;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType conferenceType;
    private View view;
    private boolean isVideoCall;
    private Map<String, String> userInfo;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            ((CallActivity) getActivity()).initActionBar();

            initCallData();
            initUI(view);
            initButtonsListener();
        }

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

    private void initCallData(){
        QBRTCSession currentSession = SessionManager.getCurrentSession();
        if ( currentSession != null){
            opponents = currentSession.getOpponents();
            conferenceType = currentSession.getConferenceType();
            userInfo = currentSession.getUserInfo();
        }
    }

    private void initUI(View view) {
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(conferenceType);

        typeIncCallView = (TextView) view.findViewById(R.id.typeIncCallView);
        typeIncCallView.setText(isVideoCall ? R.string.incoming_video_call : R.string.incoming_audio_call);

        callerName = (TextView) view.findViewById(R.id.callerName);
        callerName.setText(DataHolder.getUserNameByID(SessionManager.getCurrentSession().getCallerID()));
        callerName.setBackgroundResource(BaseLogginedUserActivity.selectBackgrounForOpponent((DataHolder.getUserIndexByID((
                SessionManager.getCurrentSession().getCallerID()))) + 1));

        otherIncUsers = (TextView) view.findViewById(R.id.otherIncUsers);
        otherIncUsers.setText(getOtherIncUsersNames(opponents));

        rejectBtn = (ImageButton) view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton) view.findViewById(R.id.takeBtn);
    }

    private void initButtonsListener() {
            rejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rejectBtn.setClickable(false);
                    takeBtn.setClickable(false);
                    Log.d(TAG, "Call is rejected");
                    stopCallNotification();
                    ((CallActivity) getActivity()).rejectCurrentSession();
                    getActivity().finish();
                }
            });

            takeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takeBtn.setClickable(false);
                    rejectBtn.setClickable(false);
                    stopCallNotification();
                    ((CallActivity) getActivity())
                            .addConversationFragment(
                                    opponents, conferenceType, Consts.CALL_DIRECTION_TYPE.INCOMING);

                    Log.d(TAG, "Call is started");
                }
            });
    }

    public void startCallNotification() {

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

    private void stopCallNotification() {
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ringtone.release();
            ringtone = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames(List<Integer> opponents) {
        List<Integer> otherOpponents = new ArrayList<>(opponents);
        StringBuffer s = new StringBuffer("");
        opponentsFromCall.addAll(DataHolder.getUsersList());
        otherOpponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : otherOpponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
                    if (otherOpponents.indexOf(i) == (otherOpponents.size() - 1)) {
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

    public void onStop() {
        stopCallNotification();
        super.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
