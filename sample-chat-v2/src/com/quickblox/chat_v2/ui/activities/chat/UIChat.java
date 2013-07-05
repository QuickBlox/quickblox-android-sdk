package com.quickblox.chat_v2.ui.activities.chat;

import android.content.Context;
import android.content.Intent;

import com.quickblox.module.users.model.QBUser;

import java.util.List;

/**
 * Created by andrey on 05.07.13.
 */
public interface UIChat {
    void setBarTitle(String pTitle);

    void setTopBarParams(String s, int v, boolean b);

    Intent getIntent();

    Context getContext();

    void showMessages(List<String> pList);

    void setTopBarFriendParams(QBUser pOpponentUser, boolean b);

    void changeUploadState(boolean b);

    void showMessage(String pLastMsg, boolean b);
}
