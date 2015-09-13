package com.quickblox.sample.videochatwebrtcnew.definitions;

public class Consts {
    public static final String APP_ID = "92";
    public static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
    public static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";

//    public static final String APP_ID = "18846";
//    public static final String AUTH_KEY = "64JzC2cuLkSMUq7";
//    public static final String AUTH_SECRET = "s4VCJZq4uWNer7H";
    public static final String EMPTY_STRING = "";

    public final static String OPPONENTS = "opponents";
    public static final String CONFERENCE_TYPE = "conference_type";
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";

    public static final int CONNECTION_TYPE_WIFI = 1;
    public static final int CONNECTION_TYPE_MOBILE = 2;
    public static final int CONNECTION_TYPE_NOT_CONNECTED = 0;

    public static final int NOTIFICATION_FORAGROUND = 1004;
    public static final int NOTIFICATION_CONNECTION_LOST = 1005;

    public static final int CALL_ACTIVITY_CLOSE = 1000;
    public final static int LOGIN_TASK_CODE = 1002;
    public final static int LOGIN_RESULT_CODE = 1003;

    public final static int CALL_TASK_CODE = 1009;
    public final static int CALL_RESULT_CODE = 1010;
    public final static String CALL_RESULT = "call_result";
    public final static String CALL_ACTION_VALUE = "call_action_value";


    public final static int RECEIVE_NEW_SESSION = 11110;
    public final static int USER_NOT_ANSWER = 11111;
    public final static int CALL_REJECT_BY_USER = 11112;
    public final static int RECEIVE_HANG_UP_FROM_USER = 11113;
    public final static int SESSION_CLOSED = 11114;
    public final static int SESSION_START_CLOSE = 11115;

    public final static int START_CONNECT_TO_USER = 22220;
    public final static int CONNECTED_TO_USER = 22221;
    public final static int CONNECTION_CLOSED_FOR_USER = 22222;
    public final static int DISCONNECTED_FROM_USER = 22223;
    public final static int DISCONNECTED_TIMEOUT_FROM_USER = 22224;
    public final static int CONNECTION_FAILED_WITH_USER = 22225;
    public final static int ERROR = 22226;

    public final static int LOCAL_VIDEO_TRACK_RECEIVE = 33330;
    public final static int REMOTE_VIDEO_TRACK_RECEIVE = 33331;

    public final static int RESULT_CODE_1 = 1;
    public final static int RESULT_CODE_2 = 2;
    public final static int RESULT_CODE_3 = 3;

    //Start service variant
    public final static String START_SERVICE_VARIANT = "start_service_variant";
    public final static int AUTOSTART = 1006;
    public final static int RELOGIN = 1007;
    public final static int LOGIN = 1008;



    public final static String PARAM_PINTENT = "pendingIntent";
    public final static String LOGIN_RESULT = "result";

    //Shared Preferences constants
    public static final String USER_LOGIN = "user_login";
    public static final String USER_PASSWORD = "user_password";
    public static final String USER_ID = "user_id";

    public static final String USER_IS_LOGINED = "is_logined";
    public static final long ANSWER_TIME_INTERVAL = 45l;
    public static final long HUNG_UP_TIME_OUT = 3000l;

    //CALL ACTIVITY CLOSE REASONS
    public static final int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    public static final String WIFI_DISABLED = "wifi_disabled";

    public static final String CALLER_NAME = "caller_name";
    public static final String SESSION_ID = "sessionID";
    public static final String START_CONVERSATION_REASON = "start_conversation_reason";
    private static final String ADD_OPPONENTS_FRAGMENT_HANDLER = "opponentHandlerTask";
    private static final long TIME_BEGORE_CLOSE_CONVERSATION_FRAGMENT = 3;
    private static final String INCOME_WINDOW_SHOW_TASK_THREAD = "INCOME_WINDOW_SHOW";
    public static final String OPPONENTS_CALL_FRAGMENT = "opponents_call_fragment";
    public static final String OPPONENTS_LIST_EXTRAS = "opponents_list";
    public static final String CALL_DIRECTION_TYPE_EXTRAS = "call_direction_type";
    public static final String CALL_TYPE_EXTRAS = "call_type";
    public static final String QBRTCSESSION_EXTRAS = "qbrtcsession";
    public static final String USER_INFO_EXTRAS = "user_info";
    public static final String IS_SERVICE_AUTOSTARTED = "autostart";
    public static final String SHARED_PREFERENCES = "preferences";
    public static final String QB_EXCEPTION_EXTRAS = "exception";

    public enum CALL_DIRECTION_TYPE {
        INCOMING,
        OUTGOING
    }
}
