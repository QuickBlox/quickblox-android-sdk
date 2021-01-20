package com.quickblox.sample.videochat.conference.java.utils;

import android.Manifest;

public interface Consts {

    String PREF_SWAP_CAM_TOGGLE_CHECKED = "pref_swap_cam_toggle_checked";
    String PREF_SCREEN_SHARING_TOGGLE_CHECKED = "pref_screen_sharing_toggle_checked";
    String PREF_CAM_ENABLED = "pref_cam_enabled";
    String PREF_MIC_ENABLED = "pref_mic_enabled";

    String EXTRA_DIALOG_ID = "dialog_id";
    String EXTRA_CERTAIN_DIALOG_ID = "certain_dialog_id";
    String EXTRA_ROOM_ID = "room_id";
    String EXTRA_ROOM_TITLE = "room_title";
    String EXTRA_DIALOG_OCCUPANTS = "dialog_occupants";
    String EXTRA_AS_LISTENER = "as_listener";
    String EXTRA_SETTINGS_TYPE = "extra_settings_type";

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
}