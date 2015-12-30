package com.quickblox.sample.customobjects.model;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.definition.Consts;

public class Movie {

    private String id;
    private String name;
    private String description;
    private String year;
    // TODO Shouldn't rating be float value?
    private String rating;
    private String date;

    public Movie(QBCustomObject qbCustomObject) {
        id = qbCustomObject.getParentId();
        name = parseField(Consts.NAME, qbCustomObject);
        description = parseField(Consts.DESCRIPTION, qbCustomObject);
        year = parseField(Consts.YEAR, qbCustomObject);
        rating = parseField(Consts.RATING, qbCustomObject);
        date = qbCustomObject.getUpdatedAt().toString();
    }

    // TODO Make static, maybe create QbCustomObjectUtils class and move this method there
    private String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getYear() {
        return year;
    }

    public String getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }
}
