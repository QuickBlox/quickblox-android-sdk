package com.quickblox.sample.content.helper;

import com.quickblox.content.model.QBFile;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    private int signInUserId;
    private List<QBFile> qbFileList;

    private DataHolder() {
        qbFileList = new ArrayList<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public void setQbFileList(List<QBFile> qbFileList) {
        this.qbFileList = qbFileList;
    }

    public List<QBFile> getQBFileList() {
        return qbFileList;
    }

    public void addQbFile(QBFile qbFile) {
        qbFileList.add(qbFile);
    }
}
