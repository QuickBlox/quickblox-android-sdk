package com.quickblox.sample.content.helper;

import android.util.SparseArray;

import com.quickblox.content.model.QBFile;

import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    // TODO Remove signInUserId field, we don't need it
    private int signInUserId;
    // TODO Rename to qbFileSparseArray, without shortening
    private SparseArray<QBFile> qbFileSparseArr;

    private DataHolder() {
        qbFileSparseArr = new SparseArray<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setQbFileSparseArray(List<QBFile> qbFileList) {
        for (QBFile qbFile : qbFileList) {
            qbFileSparseArr.put(qbFile.getId(), qbFile);
        }
    }

    public SparseArray<QBFile> getQBFileSparseArray() {
        return qbFileSparseArr;
    }

    public void clear() {
        qbFileSparseArr.clear();
    }

    public void addQbFile(QBFile qbFile) {
        qbFileSparseArr.put(qbFile.getId(), qbFile);
    }

    public QBFile getQBFile(int id) {
        return qbFileSparseArr.get(id);
    }
}
