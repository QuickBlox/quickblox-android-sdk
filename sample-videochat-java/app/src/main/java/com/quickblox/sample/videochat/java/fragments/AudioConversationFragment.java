package com.quickblox.sample.videochat.java.fragments;

import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.activities.CallActivity;
import com.quickblox.sample.videochat.java.adapters.AudioCallAdapter;
import com.quickblox.sample.videochat.java.adapters.ReconnectingUserModel;
import com.quickblox.sample.videochat.java.utils.CollectionsUtils;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.java.utils.UiUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.audio.QBAudioManager;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionEventsCallback;

import java.util.ArrayList;
import java.util.Map;


public class AudioConversationFragment extends BaseConversationFragment implements CallActivity.OnChangeAudioDevice, QBRTCSessionEventsCallback {
    private static final String TAG = AudioConversationFragment.class.getSimpleName();

    public static final String SPEAKER_ENABLED = "is_speaker_enabled";

    private ToggleButton audioSwitchToggleButton;
    private TextView alsoOnCallText;
    private TextView firstOpponentNameTextView;
    private TextView otherOpponentsTextView;
    private AudioCallAdapter adapter;

    @Override
    public void onStart() {
        super.onStart();
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.addOnChangeAudioDeviceListener(this);
            conversationFragmentCallback.addSessionEventsListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        conversationFragmentCallback.removeSessionEventsListener(this);
    }

    @Override
    protected void configureOutgoingScreen() {
        outgoingOpponentsRelativeLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        allOpponentsTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_outgoing_opponents_names_audio_call));
        ringingTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_color_call_type));
    }

    @Override
    protected void configureToolbar() {
        toolbar.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        toolbar.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.toolbar_title_color));
        toolbar.setSubtitleTextColor(ContextCompat.getColor(getActivity(), R.color.toolbar_subtitle_color));
    }

    @Override
    protected void configureActionBar() {
        String name;
        if (TextUtils.isEmpty(currentUser.getFullName())) {
            name = currentUser.getLogin();
        } else {
            name = currentUser.getFullName();
        }
        actionBar.setSubtitle(String.format(getString(R.string.subtitle_text_logged_in_as), name));
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        if (view == null) {
            return;
        }
        timerCallText = view.findViewById(R.id.timer_call);

        ImageView firstOpponentAvatarImageView = (ImageView) view.findViewById(R.id.image_caller_avatar);
        firstOpponentAvatarImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(opponents.get(0).getId()));

        alsoOnCallText = (TextView) view.findViewById(R.id.text_also_on_call);
        setVisibilityAlsoOnCallTextView();

        firstOpponentNameTextView = (TextView) view.findViewById(R.id.text_caller_name);

        QBUser user = opponents.get(0);
        String name;
        if (TextUtils.isEmpty(user.getFullName())) {
            name = user.getLogin();
        } else {
            name = user.getFullName();
        }
        firstOpponentNameTextView.setText(name);

        otherOpponentsTextView = (TextView) view.findViewById(R.id.text_other_inc_users);
        otherOpponentsTextView.setText(getOtherOpponentsNames());

        audioSwitchToggleButton = (ToggleButton) view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);
        audioSwitchToggleButton.setChecked(SharedPrefsHelper.getInstance().get(SPEAKER_ENABLED, true));

        actionButtonsEnabled(false);

        if (conversationFragmentCallback != null && conversationFragmentCallback.isCallState()) {
            onCallStarted();
        }
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvUsers);
        ArrayList<ReconnectingUserModel> users = new ArrayList<>();
        for (QBUser item : opponents) {
            QBRTCTypes.QBRTCReconnectionState state = conversationFragmentCallback.getState(item.getId());
            if (state != null) {
                switch (state) {
                    case QB_RTC_RECONNECTION_STATE_RECONNECTING:
                        users.add(new ReconnectingUserModel(item, "Reconnecting"));
                        break;
                    case QB_RTC_RECONNECTION_STATE_RECONNECTED:
                        users.add(new ReconnectingUserModel(item, "Reconnected"));
                        break;
                    case QB_RTC_RECONNECTION_STATE_FAILED:
                        users.add(new ReconnectingUserModel(item, "Reconnection failed"));
                        break;
                }
            } else {
                users.add(new ReconnectingUserModel(item, ""));
            }
        }
        adapter = new AudioCallAdapter(getContext(), users);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setVisibilityAlsoOnCallTextView() {
        if (opponents.size() < 2) {
            alsoOnCallText.setVisibility(View.INVISIBLE);
        }
    }

    private String getOtherOpponentsNames() {
        ArrayList<QBUser> otherOpponents = new ArrayList<>();
        otherOpponents.addAll(opponents);
        otherOpponents.remove(0);
        return CollectionsUtils.makeStringFromUsersFullNames(otherOpponents);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (conversationFragmentCallback != null) {
            conversationFragmentCallback.removeOnChangeAudioDeviceListener(this);
        }
    }

    @Override
    protected void initButtonsListener() {
        super.initButtonsListener();
        audioSwitchToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPrefsHelper.getInstance().save(SPEAKER_ENABLED, isChecked);
                if (conversationFragmentCallback != null) {
                    conversationFragmentCallback.onSwitchAudio();
                }
            }
        });
    }

    @Override
    protected void actionButtonsEnabled(boolean enabled) {
        super.actionButtonsEnabled(enabled);
        audioSwitchToggleButton.setActivated(enabled);
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_audio_conversation;
    }

    @Override
    public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {
        super.onOpponentsListUpdated(newUsers);
        QBUser user = opponents.get(0);
        String name;
        if (TextUtils.isEmpty(user.getFullName())) {
            name = user.getLogin();
        } else {
            name = user.getFullName();
        }
        firstOpponentNameTextView.setText(name);
        otherOpponentsTextView.setText(getOtherOpponentsNames());
    }

    @Override
    public void onCallTimeUpdate(String time) {
        timerCallText.setText(time);
    }

    @Override
    public void audioDeviceChanged(QBAudioManager.AudioDevice newAudioDevice) {
        audioSwitchToggleButton.setChecked(newAudioDevice != QBAudioManager.AudioDevice.SPEAKER_PHONE);
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onCallAcceptByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {

    }

    @Override
    public void onChangeReconnectionState(QBRTCSession qbrtcSession, Integer integer, QBRTCTypes.QBRTCReconnectionState qbrtcReconnectionState) {
        ReconnectingUserModel user = adapter.getItemByUserId(integer);
        if (user != null) {
            switch (qbrtcReconnectionState) {
                case QB_RTC_RECONNECTION_STATE_RECONNECTING:
                    user.setReconnectingState("Reconnecting");
                    break;
                case QB_RTC_RECONNECTION_STATE_RECONNECTED:
                    user.setReconnectingState("Reconnected");
                    break;
                case QB_RTC_RECONNECTION_STATE_FAILED:
                    user.setReconnectingState("Reconnection failed");
                    break;
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {

    }
}