package com.quickblox.sample.groupchatwebrtc.utils;

import android.Manifest;

/**
 * QuickBlox team
 */
public interface Consts {

    String APP_ID = "39854";
    String AUTH_KEY = "JtensAa9y4AM5Yk";
    String AUTH_SECRET = "AsDFwwwxpr3LN5w";
    String ACCOUNT_KEY = "7yvNe17TnjNUqDoPwfqp";

    // In GCM, the Sender ID is a project ID that you acquire from the API console
    String GCM_SENDER_ID = "761750217637";

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
    String PREF_CURRENT_TOKEN = "current_token";
    String PREF_TOKEN_EXPIRATION_DATE = "token_expiration_date";

    String EXTRA_QB_USER = "qb_user";

    String EXTRA_USER_ID = "user_id";
    String EXTRA_USER_LOGIN = "user_login";
    String EXTRA_USER_PASSWORD = "user_password";
    String EXTRA_PENDING_INTENT = "pending_Intent";

    String EXTRA_CONTEXT = "context";
    String EXTRA_OPPONENTS_LIST = "opponents_list";
    String EXTRA_CONFERENCE_TYPE = "conference_type";
    String EXTRA_IS_INCOMING_CALL = "conversation_reason";

    String EXTRA_LOGIN_RESULT = "login_result";
    String EXTRA_LOGIN_ERROR_MESSAGE = "login_error_message";
    int EXTRA_LOGIN_RESULT_CODE = 1002;

    String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

    String EXTRA_COMMAND_TO_SERVICE = "command_for_service";
    int COMMAND_NOT_FOUND = 0;
    int COMMAND_LOGIN = 1;
    int COMMAND_LOGOUT = 2;
    String EXTRA_IS_STARTED_FOR_CALL = "isRunForCall";
    String ALREADY_LOGGED_IN = "You have already logged in chat";

    enum StartConversationReason {
        INCOME_CALL_FOR_ACCEPTION,
        OUTCOME_CALL_MADE
    }
}
