package com.quickblox.sample.conference.fragments;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.conference.ConferenceSession;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.activities.CallActivity;
import com.quickblox.sample.conference.adapters.OpponentsFromCallAdapter;
import com.quickblox.videochat.webrtc.QBRTCAudioTrack;

import java.io.Serializable;

/**
 * Created by roman on 6/9/17.
 */

public class AudioConversationFragment extends BaseConversationFragment implements Serializable, OpponentsFromCallAdapter.OnAdapterEventListener,
        CallActivity.OnChangeDynamicToggle {
    private String TAG = getClass().getSimpleName();

    private TextView localName;
    private ToggleButton audioSwitchToggleButton;

    private boolean headsetPlugged;

    @Override
    public void onStart() {
        super.onStart();
        conversationFragmentCallbackListener.addOnChangeDynamicToggle(this);
    }

    @Override
    protected void initViews(View view) {
        localName = (TextView) view.findViewById(R.id.localName);
        localName.setVisibility(View.VISIBLE);
        audioSwitchToggleButton = (ToggleButton) view.findViewById(R.id.toggle_speaker);
        audioSwitchToggleButton.setVisibility(View.VISIBLE);
        super.initViews(view);
    }

    @Override
    public void onLocalAudioTrackReceive(ConferenceSession session, QBRTCAudioTrack audioTrack) {
        Log.d(TAG, "onLocalAudioTrackReceive() run");
        setStatusForCurrentUser(getString(R.string.text_status_connected));
        actionButtonsEnabled(true);
    }

    @Override
    public void onRemoteAudioTrackReceive(ConferenceSession session, QBRTCAudioTrack audioTrack, final Integer userID) {
        Log.d(TAG, "onRemoteAudioTrackReceive() run");
        setOpponentToAdapter(userID);

        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setRemoteViewMultiCall();
            }
        }, LOCAL_TRACk_INITIALIZE_DELAY);
    }

    private void setRemoteViewMultiCall() {
        if (currentSession.isDestroyed()) {
            Log.d(TAG, "setRemoteViewMultiCall currentSession.isDestroyed RETURN");
            return;
        }
        if (!isRemoteShown) {
            isRemoteShown = true;
            setRecyclerViewVisibleState();
            setDuringCallActionBar();
        }
        updateActionBar(opponentsAdapter.getItemCount());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem cameraSwitchItem = menu.findItem(R.id.camera_switch);
        cameraSwitchItem.setVisible(false);
    }

    @Override
    protected void initButtonsListener() {
        super.initButtonsListener();

        audioSwitchToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationFragmentCallbackListener.onSwitchAudio();
            }
        });
    }

    @Override
    protected void actionButtonsEnabled(boolean inability) {
        super.actionButtonsEnabled(inability);
        if (!headsetPlugged) {
            audioSwitchToggleButton.setEnabled(inability);
        }
        audioSwitchToggleButton.setActivated(inability);
    }

    @Override
    public void enableDynamicToggle(boolean plugged, boolean previousDeviceEarPiece) {
        headsetPlugged = plugged;
        audioSwitchToggleButton.setEnabled(!plugged);

        if (plugged) {
            audioSwitchToggleButton.setChecked(true);
        } else if (previousDeviceEarPiece) {
            audioSwitchToggleButton.setChecked(true);
        } else {
            audioSwitchToggleButton.setChecked(false);
        }
    }
}