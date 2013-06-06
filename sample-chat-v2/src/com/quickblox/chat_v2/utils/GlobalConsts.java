package com.quickblox.chat_v2.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/9/13
 * Time: 9:05 AM
 */
public class GlobalConsts {


    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    
    public static final String FRIEND_ID = "friend_id";

    public static final String ATTACH_INDICATOR = "<Attach file>";
    public static final String REGEX_MESSAGE_AUTHOR_ID = "^[^_]*_([^@]*)@.*$";

    public static final byte DOWNLOAD_LIST_FOR_DIALOG = 0;
    public static final byte DOWNLOAD_LIST_FOR_CONTACTS = 1;
    public static final byte DOWNLOAD_LIST_FOR_CONTACTS_CANDIDATE = 2;

    //dialog section
    public static final String DIALOGS_CLASS = "dialogs";

    public static final String ARRAY_TYPE = "array_type";
    public static final String CONTACTS_ARRAY = "contacts_array";
    public static final String CONTACTS_CANDIDATE_ARRAY = "candidate_array";
    public static final String ARRAY_POSITION = "array_position";

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
    public static final String MSG_TEXT = "message";
    public static final String MESSAGES = "messages";
    public static final String ROOM_NAME = "name";

    public static final String PREVIOUS_ACTIVITY = "previous_activity";
    public static final byte ROOM_ACTIVITY = 0;
    public static final byte DIALOG_ACTIVITY = 1;
    public static final byte CONTACTS_ACTIVITY = 2;
    public static final String IS_NEW_ROOM = "is_new_room";

    public static final String ATTACH_URL = "attach_url";
}
