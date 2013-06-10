package com.quickblox.chat_v2.gcm;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created with IntelliJ IDEA.
// * User: Nikolay Dymura
// * Date: 5/27/13
// * E-mail: nikolay.dymura@gmail.com
// */
public class GCMMessage {
//
//    public static String TYPE = "type";
//
//    public static String TITLE = "title";
//
//    public static String MESSAGE = "message";
//
//    private GCMMessageType messageType;
//
//    private String title;
//
//    private String message;
//
//
//    private GCMMessage() {
//    }
//
//    public static GCMMessage valueOf(Bundle bundle) {
//        GCMMessage message = null;
//        String type = bundle.getString(TYPE);
//        if (!TextUtils.isEmpty(type)) {
//            GCMMessageType messageType = GCMMessageType.from(type);
//            if (messageType != null) {
//                message = new GCMMessage();
//                message.messageType = messageType;
//                message.title = bundle.getString(TITLE);
//                message.message = bundle.getString(MESSAGE);
//            }
//
//        }
//        return message;
//    }
//
//    public static GCMMessage newInstance(GCMMessageType type, String arg) {
//        GCMMessage message = new GCMMessage();
//        message.messageType = type;
//        message.message = arg;
//        return message;
//    }
//
//    public GCMMessageType getMessageType() {
//        return messageType;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public HashMap<String, String> toMap() {
//        HashMap<String, String> map = new HashMap<String, String>();
//
//        if (!TextUtils.isEmpty(message)) {
//            map.put("data." + MESSAGE, message);
//        }
//
//        if (messageType != null) {
//            map.put("data." + TYPE, messageType.value());
//        }
//
//        if (!TextUtils.isEmpty(title)) {
//            map.put("data." + TITLE, title);
//        }
//
//        return map;
//    }
////
////    public enum GCMMessageType {
////
////        MISSED_CALL("missed_call");
////
////        private static final Map<String, GCMMessageType> typesByValue = new HashMap<String, GCMMessageType>();
////
////        static {
////            for (GCMMessageType type : GCMMessageType.values()) {
////                typesByValue.put(type.type, type);
////            }
////        }
////
////        private final String type;
////
////
////        private GCMMessageType(String type) {
////            this.type = type;
////        }
////
////        public static GCMMessageType from(String value) {
////            return typesByValue.get(value);
////        }
////
////        public String value() {
////            return this.type;
////        }
////    }
}
