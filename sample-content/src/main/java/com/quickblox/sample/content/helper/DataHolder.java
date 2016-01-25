package com.quickblox.sample.content.helper;

import android.util.SparseArray;

import com.quickblox.content.model.QBFile;

import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    private SparseArray<QBFile> qbFileSparseArray;

    private DataHolder() {
        qbFileSparseArray = new SparseArray<>();
    }

    public static synchronized DataHolder getInstance() {
        if (instance == null) {
            instance = new DataHolder();
        }
        return instance;
    }

    public void addQbFiles(List<QBFile> qbFileList) {
        for (QBFile qbFile : qbFileList) {
            addQbFile(qbFile);
        }
    }

    public SparseArray<QBFile> getQBFileSparseArray() {
        return qbFileSparseArray;
    }

    public void clear() {
        qbFileSparseArray.clear();
    }

    public void addQbFile(QBFile qbFile) {
        qbFileSparseArray.put(qbFile.getId(), qbFile);
    }

    public QBFile getQBFile(int id) {
        return qbFileSparseArray.get(id);
    }
}
