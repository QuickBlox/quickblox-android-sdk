package com.quickblox.sample.helper;


import com.quickblox.sample.object.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 20.11.12
 * Time: 14:18
 */
public class DataHolder {


    private static DataHolder dataHolder;
    private int signInUserId;
    private List<Note> noteList;

    public static synchronized DataHolder getDataHolder() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public int getSignInUserId() {
        return signInUserId;
    }

    public void addNoteToList(String id, String title, String status, String date, String comments) {
        if (noteList == null) {
            noteList = new ArrayList<Note>();
        }
        noteList.add(new Note(id, title, status, date, comments));
    }

    public int getNoteListSize() {
        if (noteList == null) {
            noteList = new ArrayList<Note>();
        }
        return noteList.size();
    }

    public String getNoteTitle(int position) {
        return noteList.get(position).getTitle();
    }

    public String getNoteDate(int position) {
        return noteList.get(position).getDate();
    }

    public List<String> getNoteComments(int position) {
        return noteList.get(position).getCommentList();
    }

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
    public void removeNoteFromList(int position){
        noteList.remove(position);
    }

}
