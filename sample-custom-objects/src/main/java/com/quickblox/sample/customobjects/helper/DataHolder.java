package com.quickblox.sample.customobjects.helper;

import com.quickblox.sample.customobjects.model.Movie;
import com.quickblox.sample.customobjects.model.Note;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static DataHolder dataHolder;
    private int signInUserId;
    private List<Movie> movieList;
    List<Note> noteList;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public int getMovieListSize() {
        if (movieList == null) {
            movieList = new ArrayList<Movie>();
        }
        return movieList.size();
    }

    public String getMovieName(int position) {
        return movieList.get(position).getName();
    }

    public String getMovieDescription(int position ){
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

//    public List<String> getNoteComments(int position) {
//        return movieList.get(position).getCommentsList();
//    }

    public String getNoteStatus(int position) {
        return noteList.get(position).getStatus();
    }

    public String getNoteId(int position) {
        return noteList.get(position).getId();
    }

    public void setNoteToNoteList(int position, Note note) {
        noteList.set(position, note);
    }

    public void addNewComment(int notePosition, String comment) {
        noteList.get(notePosition).addNewComment(comment);
    }

    public String getComments(int notePosition) {
        return noteList.get(notePosition).getComments();
    }

    public void removeNoteFromList(int position) {
        movieList.remove(position);
    }

    public void clear() {
        movieList.clear();
    }

    public int size() {
        if (movieList != null) {
            return movieList.size();
        }
        return 0;
    }

    public void addMovieToList(QBCustomObject customObject){
        if(movieList ==null){
           movieList = new ArrayList<>();
        }
        movieList.add(new Movie(customObject));
    }

    public void addNoteToList(QBCustomObject customObject) {
        if (noteList == null) {
            noteList = new ArrayList<Note>();
        }
        noteList.add(new Note(customObject));
    }
}