package com.quickblox.sample.customobjects.model;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.sample.customobjects.util.QbCustomObjectsUtils;

public class Movie {

   public interface Contract{
        String NAME = "name";
        String DESCRIPTION = "description";
        String YEAR = "year";
        String RATING = "rating";
    }


    private String id;
    private String name;
    private String description;
    private String year;
    // TODO Shouldn't rating be float value?
    private String rating;
    private String date;

    public Movie(QBCustomObject qbCustomObject) {
        id = qbCustomObject.getParentId();
        name = QbCustomObjectsUtils.parseField(Contract.NAME, qbCustomObject);
        description = QbCustomObjectsUtils.parseField(Contract.DESCRIPTION, qbCustomObject);
        year = QbCustomObjectsUtils.parseField(Contract.YEAR, qbCustomObject);
        rating = QbCustomObjectsUtils.parseField(Contract.RATING, qbCustomObject);
        date = qbCustomObject.getUpdatedAt().toString();
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
