package com.quickblox.sample.customobjects.model;

import com.quickblox.sample.customobjects.definition.Consts;
import com.quickblox.customobjects.model.QBCustomObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note {

    private String id;
    private String title;
    private String status;
    private String date;
    private List<String> commentsList;

    public Note(QBCustomObject customObject) {
        id = customObject.getCustomObjectId();
        title = parseField(Consts.TITLE, customObject);
        status = parseField(Consts.STATUS, customObject);
        date = customObject.getUpdatedAt().toString();
        commentsList = new ArrayList<String>();
        String commentString = parseField(Consts.COMMENTS, customObject);
        if (commentString != null) {
            String[] comments = commentString.split("/");
            Collections.addAll(this.commentsList, comments);
        }
    }

    private String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
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

    public List<String> getCommentsList() {
        return commentsList;
    }

    public String getComments() {
        String comments = "";
        for (String comment : commentsList) {
            comments += comment + "/";
        }
        return comments;
    }

    public void addNewComment(String comment) {
        commentsList.add(comment);
    }

    public String getId() {
        return id;
    }
}