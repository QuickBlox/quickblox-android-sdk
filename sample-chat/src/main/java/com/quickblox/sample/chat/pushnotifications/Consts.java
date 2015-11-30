package com.quickblox.sample.chat.pushnotifications;

/**
 * Created by igorkhomenko on 4/28/15.
 */
public class Consts {
    // In GCM, the Sender ID is a project ID that you acquire from the API console
    public static final String PROJECT_NUMBER = "761750217637";

    public static final String EXTRA_MESSAGE = "message";

    public static final String GCM_NOTIFICATION = "GCM Notification";
    public static final String GCM_DELETED_MESSAGE = "Deleted messages on server: ";
    public static final String GCM_INTENT_SERVICE = "GcmIntentService";
    public static final String GCM_SEND_ERROR = "Send error: ";
    public static final String GCM_RECEIVED = "Received: ";

    public static final String NEW_PUSH_EVENT = "new-push-event";
}
