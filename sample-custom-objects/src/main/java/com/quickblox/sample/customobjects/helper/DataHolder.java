package com.quickblox.sample.customobjects.helper;

import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    private List<Movie> movieList;

    private DataHolder() {
        movieList = new ArrayList<>();
    }

    // TODO Static singleton method must be first in class, but below the fields
    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public Movie getMovieObject(int id) {
        return movieList.get(id);
    }

    public void clear() {
        movieList.clear();
    }

    public void addMovieToList(QBCustomObject customObject) {
        movieList.add(new Movie(customObject));
    }

    public void addQBCustomObject(ArrayList<QBCustomObject> qbCustomObjects) {
        // TODO It's okay to skip emptiness check, loop will not launch if list is empty
        if (!qbCustomObjects.isEmpty()) {
            for (QBCustomObject customObject : qbCustomObjects) {
                movieList.add(new Movie(customObject));
            }
        }
    }
}