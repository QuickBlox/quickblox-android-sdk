package com.quickblox.chat_v2.apis;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.listeners.SubscriptionListener;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;
import com.quickblox.module.users.model.QBUser;

import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;
import java.util.Collection;

public class RosterManager implements QBRosterListener, SubscriptionListener {

    private ArrayList<String> subscribes;

    private Context mContext;
    private ChatApplication app;

    private int userID;

    public RosterManager(Context pContext) {
        mContext = pContext;
        subscribes = new ArrayList<String>();
        app = ChatApplication.getInstance();
    }

    @Override
    public void entriesAdded(Collection<Integer> addedEntriesIds) {
    }

    @Override
    public void entriesDeleted(Collection<Integer> deletedEntriesIds) {
    }

    @Override
    public void entriesUpdated(Collection<Integer> updatedEntriesIds) {
    }

    @Override
    public void presenceChanged(Presence presence) {
        String[] parts = presence.getFrom().split("-");
        app.getUserNetStatusMap().put(Integer.parseInt(parts[0]), presence.getType().toString());
    }

    @Override
    public void onSubscribe(int userId) {
        subscribes.add(String.valueOf(userId));
        ((Activity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                app.getQbm().getQbUsersFromCollection(subscribes, GlobalConsts.DOWNLOAD_LIST_FOR_CONTACTS_CANDIDATE);

            }
        });

    }

    @Override
    public void onUnSubscribe(int userId) {

        for (QBUser user : app.getContactsList()) {
            if (user.getId() == userId) {
                app.getContactsList().remove(user);
                refreshContactList();
            }
        }
    }

    public void sendRequestToSubscribe(int userId) {
        userID = userId;
        ((Activity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                QBChat.getInstance().subscribe(userID);
                refreshContactList();
            }
        });
    }

    public void sendRequestToUnSubscribe(int userId) {
        userID = userId;
        ((Activity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                QBChat.getInstance().unsubscribed(userID);
                refreshContactList();
            }
        });
    }

    public void sendPresence(Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                QBChat.getInstance().startAutoSendPresence(30);
            }
        });
    }


    public void refreshContactList() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {

                ((Activity) mContext).runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        ArrayList<String> userIds = new ArrayList<String>();
                        if (app.getQbRoster().getUsersId() != null) {
                            for (Integer in : app.getQbRoster().getUsersId()) {
                                userIds.add(String.valueOf(in));
                            }
                            app.getQbm().getQbUsersFromCollection(userIds, GlobalConsts.DOWNLOAD_LIST_FOR_CONTACTS);
                        }
                    }
                });
            }
        }, 1000);

    }
}
