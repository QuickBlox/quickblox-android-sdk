package com.quickblox.sample.customobjects.helper;

import com.quickblox.customobjects.model.QBCustomObject;
import com.quickblox.sample.customobjects.model.Movie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DataHolder {

    private static DataHolder instance;
    private Map<String, Movie> movieMap;

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    private DataHolder() {
        movieMap = new HashMap<>();
    }

    public Map<String, Movie> getMovieMap() {
        return movieMap;
    }

    public Movie getMovieObject(String id) {
        return movieMap.get(id);
    }

    public void clear() {
        movieMap.clear();
    }

    //TODO if DataHolder stores movies, logically it should work with movie.
    // It's better to create movie outside and pass movie here
    public void addMovieToList(QBCustomObject customObject) {
        Movie movie = new Movie(customObject);
        movieMap.put(movie.getId(), movie);
    }

    //TODO the same
    public void addQBCustomObject(ArrayList<QBCustomObject> qbCustomObjects) {
        for (QBCustomObject customObject : qbCustomObjects) {
            addMovieToList(customObject);
        }
    }
}