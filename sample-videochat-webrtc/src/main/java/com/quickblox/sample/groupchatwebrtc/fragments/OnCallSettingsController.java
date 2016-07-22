package com.quickblox.sample.groupchatwebrtc.fragments;


public interface OnCallSettingsController {

    void onSwitchAudio();

    void onCaptureFormatChange(int width, int height, int framerate);
}
