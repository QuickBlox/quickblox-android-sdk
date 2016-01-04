package com.quickblox.sample.customobjects.util;

import com.quickblox.customobjects.model.QBCustomObject;

public class QbCustomObjectsUtils {

    public static String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }
}
