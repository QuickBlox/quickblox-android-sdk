package com.quickblox.sample.groupchatwebrtc.util;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tereha on 26.04.16.
 */
public class QBRestUtils {
    private static QBRestUtils instance;
    private String TAG = QBRestUtils.class.getSimpleName();

    public QBRestUtils() {

    }

    public static synchronized QBRestUtils getInstance(){
        if (instance == null) {
            instance = new QBRestUtils();
        }

        return instance;
    }

    public void signIn(final QBUser currentQbUser, QBEntityCallback<QBUser> callback){
        QBUsers.signIn(currentQbUser, callback);
    }

    public void updateUserOnQBServer(QBUser qbUser, QBEntityCallback<QBUser> callback){
        qbUser.setOldPassword(Consts.DEFAULT_USER_PASSWORD);
        QBUsers.updateUser(qbUser, callback);
    }

    public void signUpNewUser(final QBUser newQbUser){
        QBUsers.signUp(newQbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
            }
        });
    }

    public void loadUsersByTag(String tag, QBEntityCallback<ArrayList<QBUser>> callback){

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        List<String> tags = new LinkedList<>();
        tags.add(tag);

        QBUsers.getUsersByTags(tags, requestBuilder, callback);
    }


}
