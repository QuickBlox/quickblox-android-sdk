package com.quickblox.ratings.main.model;

import android.graphics.drawable.Drawable;

public class Movie {

    private String movieName;
    private Drawable movieCover;
    private String movieDescription;
    private int gameModeId;
    private double movieRating;

    public Movie(int gameModeId, Drawable movieCover, String movieName, String movieDescription) {
        this.gameModeId = gameModeId;
        this.movieCover = movieCover;
        this.movieName = movieName;
        this.movieDescription = movieDescription;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }

    public Drawable getMovieCover() {
        return movieCover;
    }

    public int getGameModeId() {
        return gameModeId;
    }

    public double getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(double movieRating) {
        this.movieRating = movieRating;
    }
}