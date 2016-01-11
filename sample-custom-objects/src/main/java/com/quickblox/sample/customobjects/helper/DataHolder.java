package com.quickblox.sample.customobjects.helper;

import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    private List<Movie> movieList;

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    private DataHolder() {
        movieList = new ArrayList<>();
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
        for (QBCustomObject customObject : qbCustomObjects) {
            movieList.add(new Movie(customObject));
        }
    }
}