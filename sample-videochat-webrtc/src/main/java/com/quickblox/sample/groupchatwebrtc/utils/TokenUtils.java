package com.quickblox.sample.groupchatwebrtc.utils;

import android.text.TextUtils;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.sample.core.utils.SharedPrefsHelper;

import java.util.Date;

/**
 * Created by tereha on 06.06.16.
 */
public class TokenUtils {

    public static boolean isTokenValid() {
        String token = getCurrentToken();
        Date expirationDate = getTokenExpirationDate();

        if (TextUtils.isEmpty(token)) {
            return false;
        }

        if (expirationDate != null && System.currentTimeMillis() >= expirationDate.getTime()) {
            return false;
        }

        return true;
    }

    private static String getCurrentToken(){
        return SharedPrefsHelper.getInstance().get(Consts.PREF_CURRENT_TOKEN);
    }

    private static Date getTokenExpirationDate(){
        Date tokenExpirationDate = null;
        long tokenExpitationDateMilis = SharedPrefsHelper.getInstance().get(Consts.PREF_TOKEN_EXPIRATION_DATE, 0l);
        if (tokenExpitationDateMilis != 0l){
            tokenExpirationDate = new Date(tokenExpitationDateMilis);
        }

        return tokenExpirationDate;
    }

    public static void saveTokenData() {
        try {
            String currentToken = QBAuth.getBaseService().getToken();
            Date tokenExpirationDate = QBAuth.getBaseService().getTokenExpirationDate();
            SharedPrefsHelper.getInstance().save(Consts.PREF_CURRENT_TOKEN, currentToken);
            SharedPrefsHelper.getInstance().save(Consts.PREF_TOKEN_EXPIRATION_DATE, tokenExpirationDate.getTime());

        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
    }

    public static boolean restoreExistentQbSessionWithResult() {
        if (isTokenValid()) {
            try {
                QBAuth.createFromExistentToken(getCurrentToken(), getTokenExpirationDate());
                return true;
            } catch (BaseServiceException e) {
                e.printStackTrace();
            }
        }

        return false;
    }
}
