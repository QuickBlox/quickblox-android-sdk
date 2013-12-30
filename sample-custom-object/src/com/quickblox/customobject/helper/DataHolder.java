package com.quickblox.customobject.helper;

import com.quickblox.customobject.object.Note;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.List;

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

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
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

    public void removeNoteFromList(int position) {
        noteList.remove(position);
    }

    public void clear() {
        noteList.clear();
    }

    public int size() {
        if (noteList != null) {
            return noteList.size();
        }
        return 0;
    }

    public void addNoteToList(QBCustomObject customObject) {
        if (noteList == null) {
            noteList = new ArrayList<Note>();
        }
        noteList.add(new Note(customObject));
    }
}
