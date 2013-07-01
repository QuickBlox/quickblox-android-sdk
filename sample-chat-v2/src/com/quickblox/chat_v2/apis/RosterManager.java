package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnContactRefreshListener;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
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

        app.getQbm().setUserProfileListener(this);
        for (Integer ae : addedEntriesIds) {
            app.getQbm().getSingleUserInfo(ae);
        }
    }

    @Override
    public void entriesDeleted(Collection<Integer> deletedEntriesIds) {

        for (Integer de : deletedEntriesIds) {
            ChatApplication.getInstance().getContactsMap().remove(String.valueOf(de));
        }
        if (mOnContactRefreshListener != null) {
            mOnContactRefreshListener.reFreshCurrentList();
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


    public void getContactListFromRoster() {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            public void run() {
                ArrayList<String> userIds = new ArrayList<String>();
                if (app.getQbRoster().getUsersId() != null) {
                    for (Integer in : app.getQbRoster().getUsersId()) {
                        if (in == -1) {
                            continue;
                        }
                        userIds.add(String.valueOf(in));
                    }
                    app.getQbm().getQbUsersFromCollection(userIds, GlobalConsts.DOWNLOAD_LIST_FOR_CONTACTS);
                }
            }
        }, 1000);

    }

    @Override
    public void downloadComlete(QBUser friend) {
        if (friend != null) {
            ChatApplication.getInstance().getContactsMap().put(String.valueOf(friend.getId()), friend);
        }
    }

    public void setOnContactRefreshListener(OnContactRefreshListener pOnContactRefreshListener) {
        mOnContactRefreshListener = pOnContactRefreshListener;
    }
}
