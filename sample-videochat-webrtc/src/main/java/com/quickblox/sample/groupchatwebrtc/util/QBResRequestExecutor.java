package com.quickblox.sample.groupchatwebrtc.util;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.groupchatwebrtc.utils.QBEntityCallbackImpl;
import com.quickblox.sample.groupchatwebrtc.utils.TokenUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tereha on 26.04.16.
 */
public class QBResRequestExecutor {
    private String TAG = QBResRequestExecutor.class.getSimpleName();

    public void createSession(QBEntityCallback<QBSession> callback) {
        QBAuth.createSession().performAsync(callback);
    }

    public void createSessionWithUser(final QBUser qbUser, final QBEntityCallback<QBSession> callback) {
        QBAuth.createSession(qbUser).performAsync(callback);
    }

    public void signUpNewUser(final QBUser newQbUser, final QBEntityCallback<QBUser> callback) {
        createSessionWithoutUser(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                QBUsers.signUp(newQbUser).performAsync(callback);
            }

            @Override
            public void onError(QBResponseException e) {
                callback.onError(e);
            }
        });
    }

    public void signInUser(final QBUser currentQbUser, final QBEntityCallback<QBUser> callback) {
        QBUsers.signIn(currentQbUser).performAsync(callback);
    }

    public void deleteCurrentUser(int currentQbUserID, QBEntityCallback<Void> callback) {
        QBUsers.deleteUser(currentQbUserID).performAsync(callback);
    }

    public void loadUsersByTag(final String tag, final QBEntityCallback<ArrayList<QBUser>> callback) {
        restoreOrCreateSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                List<String> tags = new LinkedList<>();
                tags.add(tag);

                QBUsers.getUsersByTags(tags, requestBuilder).performAsync(callback);
            }
        });
    }

    public void loadUsersByIds(final Collection<Integer> usersIDs, final QBEntityCallback<ArrayList<QBUser>> callback) {
        restoreOrCreateSession(new QBEntityCallbackImpl<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                QBUsers.getUsersByIDs(usersIDs, null).performAsync(callback);
            }
        });
    }

    private void restoreOrCreateSession(final QBEntityCallbackImpl<QBSession> creatingSessionCallback) {
        if (TokenUtils.isTokenValid()) {
            if (TokenUtils.restoreExistentQbSessionWithResult()) {
                creatingSessionCallback.onSuccess(null, null);
            } else {
                creatingSessionCallback.onError(null);
            }
        } else if (SharedPrefsHelper.getInstance().hasQbUser()) {
            createSessionWithSavedUser(creatingSessionCallback);
        } else {
            createSessionWithoutUser(creatingSessionCallback);
        }
    }

    private void createSessionWithSavedUser(final QBEntityCallback<QBSession> creatingSessionCallback) {
        createSessionWithUser(SharedPrefsHelper.getInstance().getQbUser(), new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                TokenUtils.saveTokenData();
                creatingSessionCallback.onSuccess(result, params);
            }

            @Override
            public void onError(QBResponseException responseException) {
                creatingSessionCallback.onError(responseException);
                Log.d(TAG, "Error creating session with user");
            }
        });
    }

    private void createSessionWithoutUser(final QBEntityCallback<QBSession> creatingSessionCallback) {
        createSession(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession result, Bundle params) {
                creatingSessionCallback.onSuccess(result, params);
            }

            @Override
            public void onError(QBResponseException responseException) {
                creatingSessionCallback.onError(responseException);
                Log.d(TAG, "Error creating session");
            }
        });
    }
}