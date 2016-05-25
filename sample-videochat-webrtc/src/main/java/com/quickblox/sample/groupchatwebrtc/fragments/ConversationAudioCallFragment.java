package com.quickblox.sample.groupchatwebrtc.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.sample.core.utils.UiUtils;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.CollectionsUtils;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by tereha on 25.05.16.
 */
public class ConversationAudioCallFragment extends BaseConversationFragment {
    private static final String TAG = ConversationAudioCallFragment.class.getSimpleName();
    private ImageView firstOpponentAvatarImageView;
    private TextView firsrOpponentNameTextView;
    private Chronometer callTimeChronometer;

    private ToggleButton audioSwichToggleButton;
    private TextView otherOpponentsTextView;


    public static ConversationFragment newInstance(boolean isIncomingCall){
        ConversationFragment fragment = new ConversationFragment();

        Bundle args = new Bundle();
        args.putBoolean(Consts.EXTRA_IS_INCOMING_CALL, isIncomingCall);

        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

//        initViews(super.onCreateView(inflater, container, savedInstanceState));



        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initViews(View view) {
        super.initViews(view);
        firstOpponentAvatarImageView = (ImageView) view.findViewById(R.id.caller_avatar);
        firstOpponentAvatarImageView.setBackgroundDrawable(UiUtils.getColorCircleDrawable(opponents.get(0).getId()));

        firsrOpponentNameTextView = (TextView) view.findViewById(R.id.caller_name);
        firsrOpponentNameTextView.setText(opponents.get(0).getFullName());

        callTimeChronometer = (Chronometer) view.findViewById(R.id.timer_chronometer);

        otherOpponentsTextView = (TextView) view.findViewById(R.id.other_inc_users);
        otherOpponentsTextView.setText(getOtherOpponentsNames());

        audioSwichToggleButton = (ToggleButton) view.findViewById(R.id.speckerToggle);
        audioSwichToggleButton.setVisibility(View.VISIBLE);
    }

    private String getOtherOpponentsNames() {
        ArrayList<QBUser> otherOpponents = opponents;
        otherOpponents.remove(0);

        return CollectionsUtils.makeStringFromUsersFullNames(otherOpponents);
    }


    @Override
    protected void initButtonsListener(){
        super.initButtonsListener();

        audioSwichToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationFragmentCallbackListener.onSwitchAudio();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_audio_conversation;
    }
}
