package com.quickblox.ratings.main.core;

import android.graphics.drawable.Drawable;

import com.quickblox.ratings.main.model.Movie;

import java.util.List;

public class DataHolder {

    private static DataHolder dataHolder;
    private List<Movie> moviesList;
    private int qbUserId;
    private int chosenMoviePosition;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public void setMoviesList(List<Movie> moviesList) {
        this.moviesList = moviesList;
    }

    public int getMovieGameModeId(int position) {
        return moviesList.get(position).getGameModeId();
    }

    public void setMovieRating(int position, double movieRating) {
        moviesList.get(position).setMovieRating(movieRating);
    }

    public int getMovieListSize() {
        return moviesList.size();
    }

    public double getMovieRating(int position) {
        return moviesList.get(position).getMovieRating();
    }

    public Drawable getMovieCover(int position) {
        return moviesList.get(position).getMovieCover();
    }

    public String getMovieName(int position) {
        return moviesList.get(position).getMovieName();
    }

    public String getMovieDescription(int position) {
        return moviesList.get(position).getMovieDescription();
    }

    public int getQbUserId() {
        return qbUserId;
    }

    public void setQbUserId(int qbUserId) {
        this.qbUserId = qbUserId;
    }

    public int getChosenMoviePosition() {
        return chosenMoviePosition;
    }

    public void setChosenMoviePosition(int chosenMoviePosition) {
        this.chosenMoviePosition = chosenMoviePosition;
    }
}
