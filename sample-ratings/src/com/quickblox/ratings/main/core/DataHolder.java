package com.quickblox.ratings.main.core;

import android.graphics.drawable.Drawable;
import com.quickblox.ratings.main.object.Movie;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 29.11.12
 * Time: 9:36
 * To change this template use File | Settings | File Templates.
 */
public class DataHolder {

    private static DataHolder dataHolder;
    List<Movie> movieList;
    int qbUserId;
    int chosenMoviePosition;


    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }


    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

    public int getMovieGameModeId(int position) {
        return movieList.get(position).getGameModeId();
    }

    public void setMovieRating(int position, double movieRating) {
        movieList.get(position).setMovieRating(movieRating);
    }

    public int getMovieListSize() {
        return movieList.size();
    }

    public void setQbUserId(int qbUserId) {
        this.qbUserId = qbUserId;
    }

    public double getMovieRating(int position) {
        return movieList.get(position).getMovieRating();
    }

    public Drawable getMovieCover(int position) {
        return movieList.get(position).getMovieCover();
    }

    public String getMovieName(int position) {
        return movieList.get(position).getMovieName();
    }

    public String getMovieDescription(int position) {
        return movieList.get(position).getMovieDescription();
    }

    public int getQbUserId() {
        return qbUserId;
    }

    public void setChosenMoviePosition(int chosenMoviePosition){
        this.chosenMoviePosition = chosenMoviePosition;
    }
    public int getChosenMoviePosition(){
        return chosenMoviePosition;
    }

}
