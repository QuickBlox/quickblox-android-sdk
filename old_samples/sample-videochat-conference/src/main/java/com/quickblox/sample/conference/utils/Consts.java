package com.quickblox.sample.conference.utils;

import android.Manifest;

/**
 * QuickBlox team
 */
public interface Consts {

    String DEFAULT_USER_PASSWORD = "x6Bt0VDy5";

    String VERSION_NUMBER = "1.0";

    int CALL_ACTIVITY_CLOSE = 1000;

    int ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422;
    int ERR_MSG_DELETING_HTTP_STATUS = 401;

    //CALL ACTIVITY CLOSE REASONS
    int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    String WIFI_DISABLED = "wifi_disabled";

    String OPPONENTS = "opponents";
    String CONFERENCE_TYPE = "conference_type";
    String EXTRA_TAG = "currentRoomName";
    int MAX_OPPONENTS_COUNT = 6;

    String PREF_CURREN_ROOM_NAME = "current_room_name";

    String EXTRA_USER_ID = "user_id";
    String EXTRA_USER_LOGIN = "user_login";
    String EXTRA_USER_PASSWORD = "user_password";
    String EXTRA_DIALOG_ID = "dialog_id";
    String EXTRA_DIALOG_OCCUPANTS = "dialog_occupants";
    String EXTRA_AS_LISTENER = "as_listener";
    String EXTRA_DIALOG_IS_VIDEO = "dialog_is_video";
    String EXTRA_PENDING_INTENT = "pending_Intent";

    String EXTRA_LOGIN_RESULT = "login_result";
    String EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message";
    int EXTRA_LOGIN_RESULT_CODE = 1002;

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
}