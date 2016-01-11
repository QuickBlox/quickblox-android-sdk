package com.quickblox.sample.customobjects.utils;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.HashMap;

// TODO Fix filename case via "git mv", git screws case-sensitive names
public class QBCustomObjectsUtils {

    public static String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    // TODO Rename to createCustomObject
    // TODO Change fields types from Object to String and float, convert if needed in the method itself
    // TODO Separate map filling and custom object creation by blank line to improve readability
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