package com.quickblox.sample.chat.utils;

public class StringUtils {
    private static final String NULL_TEXT = "null";

    public static boolean textIsNull(String text){
        return NULL_TEXT.equals(text);
    }
}
