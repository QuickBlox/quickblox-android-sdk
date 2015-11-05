package com.quickblox.sample.content.helper;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;

import java.util.List;

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

    public int getSignInUserId() {
        return signInUserId;
    }

    public void setSignInUserId(int signInUserId) {
        this.signInUserId = signInUserId;
    }

    public void setQbFileList(List<QBFile> qbFileList) {
        this.qbFileList = qbFileList;
    }

    public int getQbFileListSize() {
        return qbFileList.size();
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
