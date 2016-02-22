package com.quickblox.sample.chat.utils.qb;

import android.text.TextUtils;

import com.quickblox.auth.QBAuth;
import com.quickblox.core.exception.BaseServiceException;

import java.util.Date;

public class QbAuthUtils {

    public static boolean isSessionActive() {
        try {
            String token = QBAuth.getBaseService().getToken();
            Date expirationDate = QBAuth.getBaseService().getTokenExpirationDate();

            if (TextUtils.isEmpty(token)) {
                return false;
            }

            if (System.currentTimeMillis() >= expirationDate.getTime()) {
                return false;
            }

            return true;
        } catch (BaseServiceException ignored) {
        }

        return false;
    }

}
