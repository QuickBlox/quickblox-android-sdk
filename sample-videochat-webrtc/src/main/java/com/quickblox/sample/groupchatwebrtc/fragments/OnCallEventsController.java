package com.quickblox.sample.groupchatwebrtc.fragments;


public interface OnCallEventsController {

    void onSwitchAudio();

    void onUseHeadSet(boolean use);

    void onAcceptCurrentSession();

    void onRejectCurrentSession();

    void onHangUpCurrentSession();

    void onSetAudioEnabled(boolean isAudioEnabled);




}
