package com.quickblox.sample.customobjects.model;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.utils.QBCustomObjectsUtils;

import java.util.Comparator;

public class Movie {

    public interface Contract {
        String NAME = "name";
        String DESCRIPTION = "description";
        String YEAR = "year";
        String RATING = "rating";
    }

    private String id;
    private String name;
    private String description;
    private String year;
    private float rating;
    private long date;

    public Movie(QBCustomObject qbCustomObject) {
        id = qbCustomObject.getCustomObjectId();
        name = QBCustomObjectsUtils.parseField(Contract.NAME, qbCustomObject);
        description = QBCustomObjectsUtils.parseField(Contract.DESCRIPTION, qbCustomObject);
        year = QBCustomObjectsUtils.parseField(Contract.YEAR, qbCustomObject);
        rating = Float.parseFloat(QBCustomObjectsUtils.parseField(Contract.RATING, qbCustomObject));
        date = qbCustomObject.getCreatedAt().getTime();
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

    public float getRating() {
        return rating;
    }

    public long getDate() {
        return date;
    }

    public static class DateComparator implements Comparator<Movie> {

        @Override
        public int compare(Movie lhs, Movie rhs) {
            return Long.valueOf(rhs.getDate()).compareTo(lhs.getDate());
        }
    }
}
