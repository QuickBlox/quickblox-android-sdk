package com.quickblox.sample.customobjects.helper;

import com.quickblox.sample.customobjects.model.Movie;

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

    public void addMovieToMap(Movie movie) {
        movieMap.put(movie.getId(), movie);
    }
}