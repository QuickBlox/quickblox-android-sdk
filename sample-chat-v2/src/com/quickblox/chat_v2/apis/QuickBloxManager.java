package com.quickblox.chat_v2.apis;

import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserPagedResult;

import java.util.List;

public class QuickBloxManager {


    public void getQbUserInfo(List<String> usersIds) {

        QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {

            @Override
            public void onComplete(Result result) {
                super.onComplete(result);

                QBUserPagedResult usersResult = (QBUserPagedResult) result;
                DataHolder.getInstance().setChatUserList(usersResult.getUsers());
            }

        });
    }
}
