package com.quickblox.sample.content.helper;

import android.util.Log;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;

import java.util.List;

public class DataHolder {

    private static DataHolder instance;
    private int signInUserId;
    private List<QBFile> qbFileList;

    private DataHolder (){}

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

    public String getUrl(int position) {
        // URL formation documentation
        // http://quickblox.com/developers/Content#API_Content_Get_File_As_A_Redirect_To_The_S3_Object

        String sessionToken = null;
        try {
            sessionToken = BaseService.getBaseService().getToken();
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
        return BaseService.getServiceEndpointURL() + "/blobs/" + qbFileList.get(position).getUid() +
                "?token=" + sessionToken;
    }

    public void addQbFile(QBFile qbFile) {
        qbFileList.add(qbFile);
    }
}
