package com.quickblox.sample.customobjects.util;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.HashMap;

public class QBCustomObjectsUtils {

    public static String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public static QBCustomObject setQBFields(Object title, Object description, Object year, Object rating) {
        HashMap<String, Object> fields = new HashMap<>();
        fields.put(Movie.Contract.NAME, title);
        fields.put(Movie.Contract.DESCRIPTION, description);
        fields.put(Movie.Contract.YEAR, year);
        fields.put(Movie.Contract.RATING, rating);
        QBCustomObject qbCustomObject = new QBCustomObject();
        qbCustomObject.setClassName(Consts.CLASS_NAME);
        qbCustomObject.setFields(fields);

        return qbCustomObject;
    }
}
