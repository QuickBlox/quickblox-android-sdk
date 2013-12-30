package com.quickblox.customobject.object;

import com.quickblox.customobject.definition.Consts;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Note {

    private String id;
    private String title;
    private String status;
    private String date;
    private List<String> commentList;

    public Note(QBCustomObject customObject) {
        id = customObject.getCustomObjectId();
        title = parseField(Consts.TITLE, customObject);
        status = parseField(Consts.STATUS, customObject);
        date = customObject.getUpdatedAt().toString();
        commentList = new ArrayList<String>();
        String commentString = parseField(Consts.COMMENTS, customObject);
        if (commentString != null) {
            String[] comments = commentString.split("/");
            Collections.addAll(this.commentList, comments);
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

    private String parseField(String field, QBCustomObject customObject) {
        Object object = customObject.getFields().get(field);
        if (object != null) {
            return object.toString();
        }
        return null;
    }
}
