package com.quickblox.ratings.main.object;

import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 27.11.12
 * Time: 16:54
 * To change this template use File | Settings | File Templates.
 */
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

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public Drawable getMovieCover() {
        return movieCover;
    }

    public int getGameModeId() {
        return gameModeId;
    }

    public void setMovieRating(double movieRating) {
        this.movieRating = movieRating;
    }

    public double getMovieRating() {
        return movieRating;
    }
}
