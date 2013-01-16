package com.quickblox.content.helper;

import com.quickblox.module.content.model.QBFile;

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
    private List<QBFile> qbFileList;

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

    public void setQbFileList(List<QBFile> qbFileList) {
        this.qbFileList = qbFileList;
    }

    public int getQbFileListSize() {
        return qbFileList.size();
    }

    public String getPublicUrl(int position) {
        return qbFileList.get(position).getUid();

    }

    public void addQbFile(QBFile qbFile) {
        qbFileList.add(qbFile);
    }
}
