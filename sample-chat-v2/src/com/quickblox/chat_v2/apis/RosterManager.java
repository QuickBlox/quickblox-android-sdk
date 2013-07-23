package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnContactRefreshListener;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collection;

public class RosterManager implements QBRosterListener, OnUserProfileDownloaded {


    private ChatApplication app;

    private OnContactRefreshListener mOnContactRefreshListener;

    public RosterManager(Context pContext) {
        app = ChatApplication.getInstance();
    }

    @Override
    public void entriesAdded(Collection<Integer> addedEntriesIds) {

        app.getQbm().addUserProfileListener(this);
        for (Integer ae : addedEntriesIds) {
            app.getQbm().getSingleUserInfo(ae, ContextForDownloadUser.DOWNLOAD_FOR_ROSTER);
        }
    }

    @Override
    public void entriesDeleted(Collection<Integer> deletedEntriesIds) {

        for (Integer de : deletedEntriesIds) {
            ChatApplication.getInstance().getContactsMap().remove(de);
        }
        if (mOnContactRefreshListener != null) {
            mOnContactRefreshListener.onRefreshCurrentList();
        }
    }

    @Override
    public void entriesUpdated(Collection<Integer> updatedEntriesIds) {
    }

    @Override
    public void presenceChanged(Presence presence) {
        String[] parts = presence.getFrom().split("-");
        app.getUserNetStatusMap().put(Integer.parseInt(parts[0]), presence.getType().toString());
    }


    public void sendPresence(Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBChat.getInstance().startAutoSendPresence(30);
            }
        });
    }


    public boolean getContactListFromRoster() {

        ArrayList<String> userIds = new ArrayList<String>();
        if (app.getQbRoster().getUsersId() != null) {
            for (Integer in : app.getQbRoster().getUsersId()) {
                if (in != -1) {
                    userIds.add(String.valueOf(in));
                }
            }
            if (userIds.isEmpty()) {
                return false;
            } else {
                app.getQbm().getQbUsersFromCollection(userIds, ContextForDownloadUser.DOWNLOAD_FOR_MAIN_ACTIVITY);
            }
        }
        return true;
    }


    public void setOnContactRefreshListener(OnContactRefreshListener pOnContactRefreshListener) {
        mOnContactRefreshListener = pOnContactRefreshListener;
    }

    @Override
    public void downloadComplete(QBUser friend, ContextForDownloadUser pContextForDownloadUser) {
        if (friend != null && ContextForDownloadUser.DOWNLOAD_FOR_ROSTER == pContextForDownloadUser) {
            app.getContactsMap().put(friend.getId(), friend);
        }
    }
}
