package com.quickblox.sample.content.utils;

import android.text.TextUtils;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;

public class QBContentUtils {

    public static String getUrl(QBFile qbFile) {
        // URL formation documentation
        // http://quickblox.com/developers/Content#API_Content_Get_File_As_A_Redirect_To_The_S3_Object

        if (qbFile.isPublic()) {
            String publicUrl = qbFile.getPublicUrl();
            if (!TextUtils.isEmpty(publicUrl)) {
                return publicUrl;
            }
        }

        String sessionToken = null;
        try {
            sessionToken = BaseService.getBaseService().getToken();
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
        return BaseService.getServiceEndpointURL() + "/blobs/" + qbFile.getUid() +
                "?token=" + sessionToken;
    }
}
