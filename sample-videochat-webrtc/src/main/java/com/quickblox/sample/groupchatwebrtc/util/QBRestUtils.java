package com.quickblox.sample.groupchatwebrtc.util;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by tereha on 26.04.16.
 */
public class QBRestUtils {
    private String TAG = QBRestUtils.class.getSimpleName();

    public void createSession(QBEntityCallback<QBSession> callback){
        QBAuth.createSession(callback);
    }

    public void signIn(final QBUser currentQbUser, QBEntityCallback<QBUser> callback){
        QBUsers.signIn(currentQbUser, callback);
    }

    public void updateUserOnQBServer(QBUser qbUser, QBEntityCallback<QBUser> callback){
        QBUsers.updateUser(qbUser, callback);
    }

    public void signUpNewUser(QBUser newQbUser,  QBEntityCallback<QBUser> callback){
        QBUsers.signUp(newQbUser, callback);
    }

    public void loadUsersByTag(String tag, QBEntityCallback<ArrayList<QBUser>> callback){

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        List<String> tags = new LinkedList<>();
        tags.add(tag);

        QBUsers.getUsersByTags(tags, requestBuilder, callback);
    }


}
