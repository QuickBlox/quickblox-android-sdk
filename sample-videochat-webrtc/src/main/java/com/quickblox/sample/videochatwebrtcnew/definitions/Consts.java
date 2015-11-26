package com.quickblox.sample.videochatwebrtcnew.definitions;

public class Consts {
    public static final String APP_ID = "92";
    public static final String AUTH_KEY = "wJHdOcQSxXQGWx5";
    public static final String AUTH_SECRET = "BTFsj7Rtt27DAmT";

    public static final int NOTIFICATION_FORAGROUND = 1004;
    public static final int NOTIFICATION_CONNECTION_LOST = 1005;

    public static final int CALL_ACTIVITY_CLOSE = 1000;
    public final static int LOGIN_TASK_CODE = 1002;
    public final static int LOGIN_RESULT_CODE = 1003;

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

    public static final long ANSWER_TIME_INTERVAL = 45l;

    //CALL ACTIVITY CLOSE REASONS
    public static final int CALL_ACTIVITY_CLOSE_WIFI_DISABLED = 1001;
    public static final String WIFI_DISABLED = "wifi_disabled";

    public static final String OPPONENTS_LIST_EXTRAS = "opponents_list";
    public static final String CALL_DIRECTION_TYPE_EXTRAS = "call_direction_type";
    public static final String CALL_TYPE_EXTRAS = "call_type";
    public static final String USER_INFO_EXTRAS = "user_info";
    public static final String SHARED_PREFERENCES = "preferences";
    public static final String QB_EXCEPTION_EXTRAS = "exception";

    public enum CALL_DIRECTION_TYPE {
        INCOMING,
        OUTGOING
    }
}
