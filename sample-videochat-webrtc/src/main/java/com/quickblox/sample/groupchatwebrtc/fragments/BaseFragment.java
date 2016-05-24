package com.quickblox.sample.groupchatwebrtc.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.groupchatwebrtc.utils.WebRtcSessionManager;
import com.quickblox.videochat.webrtc.QBRTCSession;

/**
 * Created by tereha on 24.05.16.
 */
public abstract class BaseFragment extends Fragment {

    private WebRtcSessionManager sessionManager;

    private QBRTCSession currentSession;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getCurrentView(), container, false);

        initFields();
        initUi(view);

        return view;
    }


    private void initFields() {
        sessionManager = WebRtcSessionManager.getInstance(getActivity());
        currentSession = sessionManager.getCurrentSession();
    }

    protected QBRTCSession getCurrentSession() {
        return currentSession;
    }

    abstract int getCurrentView();
    protected abstract void initUi(View view);


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
}
