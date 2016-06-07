package com.quickblox.sample.groupchatwebrtc.util;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.utils.TokenUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tereha on 26.04.16.
 */
public class QBResRequestExecutor {
    private String TAG = QBResRequestExecutor.class.getSimpleName();

    public void createSession(QBEntityCallback<QBSession> callback){

        QBAuth.createSession(callback);
    }

    public void createSessionWithUser(final QBUser qbUser, final QBEntityCallback<QBSession> callback) {
        QBAuth.createSession(qbUser, callback);
    }

    public void signIn(final QBUser currentQbUser, final QBEntityCallback<QBUser> callback){
        QBUsers.signIn(currentQbUser, callback);
    }

    public void updateUserOnQBServer(final QBUser qbUser, final QBEntityCallback<QBUser> callback){
        createSessionIfNeedAndRunAction(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                QBUsers.updateUser(qbUser, callback);
            }

            @Override
            public void onError(QBResponseException responseException) {
                callback.onError(responseException);
            }
        });
    }

    public void signUpNewUser(QBUser newQbUser,  QBEntityCallback<QBUser> callback){
        QBUsers.signUp(newQbUser, callback);
    }

    public void loadUsersByTag(final String tag, final QBEntityCallback<ArrayList<QBUser>> callback) {

        createSessionIfNeedAndRunAction(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                List<String> tags = new LinkedList<>();
                tags.add(tag);

                QBUsers.getUsersByTags(tags, requestBuilder, callback);
            }

            @Override
            public void onError(QBResponseException responseException) {

            }
        });
    }

    private void createSessionIfNeedAndRunAction(final QBEntityCallback <QBSession> creatingSessionCallback) {
        if (TokenUtils.isTokenValid()) {
            try {
                TokenUtils.restoreExistentQbSession();
                creatingSessionCallback.onSuccess(null, null);
            } catch (BaseServiceException e) {
                creatingSessionCallback.onError(null);
                e.printStackTrace();
            }
        } else if (SharedPrefsHelper.getInstance().hasQbUser()) {
            createSessionWithUser(SharedPrefsHelper.getInstance().getQbUser(), new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession result, Bundle params) {
                    creatingSessionCallback.onSuccess(result, params);
                }

                @Override
                public void onError(QBResponseException responseException) {
                    creatingSessionCallback.onError(responseException);
                    Log.d(TAG, "Error creating session with user");
                }
            });
        } else {
            createSession(new QBEntityCallback<QBSession>() {
                @Override
                public void onSuccess(QBSession result, Bundle params) {
                    creatingSessionCallback.onSuccess(result, params);
                }

                @Override
                public void onError(QBResponseException responseException) {
                    creatingSessionCallback.onError(responseException);
                    Log.d(TAG, "Error restoring session");
                }
            });
        }
    }
}
