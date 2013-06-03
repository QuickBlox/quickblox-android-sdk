package com.quickblox.chat_v2.ui.activities;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.apis.PictureManager;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/8/13 Time: 1:34 PM
 */
public class MainActivity extends TabActivity implements OnUserProfileDownloaded {

    private static final String DIALOGS_TAB = "tab1";
    private static final String ROOMS_TAB = "tab2";
    private static final String CONTACTS_TAB = "tab3";
    private static final String PROFILE_TAB = "tab4";

    private RosterManager rosterManager;
    private MessageManager msgManager;
    private PictureManager picManager;
    private QuickBloxManager qbm;
    private ChatApplication app;

    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        app = ChatApplication.getInstance();
        setupTabs();

        blockUi(true);

        qbm = new QuickBloxManager(this);
        picManager = new PictureManager(this);
        msgManager = new MessageManager(this);
        app.setPicManager(picManager);
        app.setMsgManager(msgManager);
        app.setQbm(qbm);
        app.setContactsList(new ArrayList<QBUser>());
        app.setContactsCandidateList(new ArrayList<QBUser>());
        app.setContactsMap(new HashMap<String, QBUser>());

        app.setInviteUserList(new ArrayList<String>());
        app.getInviteUserList().add(String.valueOf(app.getQbUser().getId()));

        app.setOutSideInvite(new ArrayList<String>());
        app.setDialogsUsersMap(new HashMap<String, QBUser>());

        registerRoster();
    }

    private void setupTabs() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.TabSpec dialogs = tabHost.newTabSpec(DIALOGS_TAB);
        TabHost.TabSpec rooms = tabHost.newTabSpec(ROOMS_TAB);
        TabHost.TabSpec contacts = tabHost.newTabSpec(CONTACTS_TAB);
        TabHost.TabSpec profile = tabHost.newTabSpec(PROFILE_TAB);

        dialogs.setIndicator(getString(R.string.TAB_DIALOGS_TITLE)).setContent(new Intent(this, DialogsActivity.class));

        rooms.setIndicator(getString(R.string.TAB_ROOMS_TITLE)).setContent(new Intent(this, RoomsActivity.class));

        contacts.setIndicator(getString(R.string.TAB_CONTACTS_TITLE)).setContent(new Intent(this, ContactsActivity.class));

        profile.setIndicator(getString(R.string.TAB_PROFILE_TITLE)).setContent(new Intent(this, ProfileActivity.class));

        tabHost.addTab(dialogs);
        tabHost.addTab(rooms);
        tabHost.addTab(contacts);
        tabHost.addTab(profile);

    }

    private void registerRoster() {

        rosterManager = new RosterManager(MainActivity.this);
        app.setRstManager(rosterManager);
        app.setQbRoster(QBChat.registerRoster(rosterManager));
        QBChat.registerSubscription(rosterManager);
        QBChatRoster qbRoster;
        QBChat.openXmmpChat(msgManager);

        downloadStartUpInfo();
    }

    private void downloadStartUpInfo(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                app.getQbm().setUserProfileListener(MainActivity.this);
                app.getMsgManager().downloadPersistentRoom();
                rosterManager.refreshContactList();
            }
        });

    }

    public void blockUi(boolean enable) {
        if (enable) {
            progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.loading), true);
        } else {
            progress.dismiss();
        }
    }

    @Override
    public void downloadComlete(QBUser friend) {
        blockUi(false);
    }
}