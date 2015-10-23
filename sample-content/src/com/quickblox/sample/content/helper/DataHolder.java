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
        // http://quickblox.com/developers/Content#Download_File_by_ID_.28get_file_as_a_redirect_to_the_S3_object.29

        String sessionToken = null;
        try {
            sessionToken = BaseService.getBaseService().getToken();
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }

        return "https://api.quickblox.com/blobs/" + qbFileList.get(position).getId() +
                "/download.xml?token=" + sessionToken;
    }

    public void addQbFile(QBFile qbFile) {
        qbFileList.add(qbFile);
    }
}
