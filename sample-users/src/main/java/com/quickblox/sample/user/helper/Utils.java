package com.quickblox.sample.user.helper;

import com.quickblox.core.helper.StringifyArrayList;

public class Utils {

    public static String removeBraces(StringifyArrayList tags) {
        return tags.isEmpty() ? "" : tags.toString().replace("[", "").replace("]", "");
    }
}
