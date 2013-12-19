package com.quickblox.sample.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 30.11.12
 * Time: 16:23
 */
public class Note {

    String id;
    String title;
    String status;
    String date;
    List<String> commentList;

    public Note(String id, String title, String status, String date, String commentList) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.date = date;
        this.commentList = new ArrayList<String>();
        String[] comments = commentList.split("/");
        for (String comment : comments) {
            this.commentList.add(comment);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public List<String> getCommentList() {
        return commentList;
    }

    public String getComments() {
        String comments = "";
        for (String comment : commentList) {
            comments += comment + "/";
        }
        return comments;
    }

    public void addNewComment(String comment) {
        commentList.add(comment);
    }

    public String getId() {
        return id;
    }
}
