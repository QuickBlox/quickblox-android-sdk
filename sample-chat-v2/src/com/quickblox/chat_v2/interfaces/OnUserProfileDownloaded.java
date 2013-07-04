package com.quickblox.chat_v2.interfaces;

import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.module.users.model.QBUser;

public interface OnUserProfileDownloaded {

    public void downloadComlete(QBUser friend, ContextForDownloadUser pContextForDownloadUser);
}
