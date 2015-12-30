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

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    // TODO Remove Movie fields (name, description, date, year, rating) access from here
    // DataHolder should only be able to put/get Movie objects by id (not position) and nothing else
    public String getMovieName(int position) {
        return movieList.get(position).getName();
    }

    public String getMovieDescription(int position) {
        return movieList.get(position).getDescription();
    }

    public String getMovieDate(int position) {
        return movieList.get(position).getDate();
    }

    public String getMovieYear(int position) {
        return movieList.get(position).getYear();
    }

    public String getMovieRating(int position) {
        return movieList.get(position).getRating();
    }

    public void clear() {
        movieList.clear();
    }

    public int size() {
        return movieList.size();
    }

    public void addMovieToList(QBCustomObject customObject) {
        movieList.add(new Movie(customObject));
    }

    public void addQBCustomObject (ArrayList<QBCustomObject> qbCustomObjects){
        if (!qbCustomObjects.isEmpty()) {
            for (QBCustomObject customObject : qbCustomObjects) {
                movieList.add(new Movie(customObject));
            }
        }
    }
}