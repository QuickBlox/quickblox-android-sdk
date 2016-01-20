package com.quickblox.sample.content.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;
import com.quickblox.sample.content.R;
import com.quickblox.sample.core.CoreApp;
import com.quickblox.sample.core.utils.Toaster;

public class QBContentUtils {

    public static String getUrl(QBFile qbFile) {
        // URL formation documentation
        // http://quickblox.com/developers/Content#API_Content_Get_File_As_A_Redirect_To_The_S3_Object

        String sessionToken = null;
        try {
            sessionToken = BaseService.getBaseService().getToken();
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
        return BaseService.getServiceEndpointURL() + "/blobs/" + qbFile.getUid() +
                "?token=" + sessionToken;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String showTypeError(FailReason failReason) {
        String message = null;

        switch (failReason.getType()) {
            case IO_ERROR:
                message = CoreApp.getInstance().getString(R.string.mgs_io_error);
                break;
            case DECODING_ERROR:
                message = CoreApp.getInstance().getString(R.string.mgs_decode_error);
                break;
            case NETWORK_DENIED:
                message = CoreApp.getInstance().getString(R.string.mgs_denied_error);
                break;
            case OUT_OF_MEMORY:
                message = CoreApp.getInstance().getString(R.string.mgs_memory_error);
                break;
            case UNKNOWN:
                message = CoreApp.getInstance().getString(R.string.mgs_unknown_error);
                break;
        }
        Toaster.longToast(message);
        return message;
    }
}
