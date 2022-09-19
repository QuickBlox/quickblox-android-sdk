package com.quickblox.sample.videochat.java.utils;

import android.Manifest;

/**
 * QuickBlox team
 */
public interface Consts {
    int ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS = 422;

    int MAX_OPPONENTS_COUNT = 3;
    int MAX_LOGIN_LENGTH = 50;
    int MAX_DISPLAY_NAME_LENGTH = 20;

    String EXTRA_IS_INCOMING_CALL = "conversation_reason";

    String EXTRA_LOGIN_RESULT = "login_result";
    String EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message";
    int EXTRA_LOGIN_RESULT_CODE = 1002;

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
}