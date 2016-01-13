package com.quickblox.sample.customobjects.helper;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance;
    private Map<String, Movie> movieListMap;

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    private DataHolder() {
        movieListMap = new HashMap<>();
    }

    public Map<String, Movie> getMovieList() {
        return movieListMap;
    }

    public Movie getMovieObject(String id) {
        return movieListMap.get(id);
    }

    public void clear() {
        movieListMap.clear();
    }

    public void addMovieToList(QBCustomObject customObject) {
        Movie movie = new Movie(customObject);
        movieListMap.put(movie.getId(), movie);
    }

    public void addQBCustomObject(ArrayList<QBCustomObject> qbCustomObjects) {
        for (QBCustomObject customObject : qbCustomObjects) {
            Movie movie = new Movie(customObject);
            movieListMap.put(movie.getId(), movie);
        }
    }
}