package com.quickblox.sample.customobjects.helper;

import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    // TODO Rename to instance, it's a common name for singleton field
    private static DataHolder dataHolder;
    private List<Movie> movieList;

    // TODO Rename to getInstance()
    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    // TODO Add private constructor to avoid DataHolder creation somewhere else then in singleton

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
        // TODO remove null check after moving initialization to constructor
        if (movieList != null) {
            return movieList.size();
        }
        return 0;
    }

    public void addMovieToList(QBCustomObject customObject) {
        // TODO movieList initialization should be done in constructor and nowhere else
        if (movieList == null) {
            movieList = new ArrayList<>();
        }
        movieList.add(new Movie(customObject));
    }
}