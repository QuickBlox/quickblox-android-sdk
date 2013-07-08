package com.quickblox.chat_v2.utils;

public class GlobalConsts {

    public static final String DIALOG_CREATED_ACTION = "com.quickblox.chat_v2.intent.action.DIALOG_CREATED";
    public static final String INCOMING_MESSAGE_ACTION = "com.quickblox.chat_v2.intent.action.INCOMING_MESSAGE";
    public static final String EXTRA_MESSAGE = "message";
    public static final String DIALOG_REFRESHED_ACTION = "com.quickblox.chat_v2.intent.action.DIALOG_REFRESHED";

    private GlobalConsts() {

    }

    //  public static final long GCM_CLIENT_ID = 328124915270L;
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";

    public static final String FRIEND_ID = "friend_id";

    public static final String ATTACH_INDICATOR = "<Attach file>";
    public static final String ATTACH_TEXT_FOR_DIALOGS = "Attach";


    //dialog section
    public static final String DIALOGS_CLASS = "dialogs";
    public static final String NAME_FIELD = "name";
    public static final String RECEPIENT_ID_FIELD = "recepient_id";

    public static final String DIALOGS = "dialogs";
    public static final String USER_ID_FIELD = "user_id";

    public static final String ROOM_LIST_CLASS = "roomlist";
    public static final String ROOM_LIST_NAME = "name";
    public static final String ROOM_LIST_JID = "jid";
    public static final String ROOM_LIST_USERS_POOL = "users_pool";

    public static final String USER_ID = "user_id";
    public static final String LAST_MSG = "last_msg";
    public static final String DIALOG_ID = "dialog_id";
    public static final String OPPONENT_ID = "opponent_id";
    public static final String AUTHOR_ID = "author_id";
    public static final String MESSAGE = "message";
    public static final String MESSAGES = "messages";
    public static final String ROOM_NAME = "name";

    public static final String PREVIOUS_ACTIVITY = "previous_activity";
    public static final byte ROOM_ACTIVITY = 0;
    public static final byte DIALOG_ACTIVITY = 1;
    public static final byte CONTACTS_ACTIVITY = 2;

    public static final String ATTACH_URL = "attach_url";
    public static final String PRESENCE_TYPE_AVAIABLE = "available";
    public static final String PRESENCE_TYPE_UNAVAIABLE = "unavailable";

}
