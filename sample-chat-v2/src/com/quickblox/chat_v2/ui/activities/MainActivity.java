package com.quickblox.chat_v2.ui.activities;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.gcm.GCMHelper;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/8/13 Time: 1:34 PM
 */
public class MainActivity extends TabActivity implements OnUserProfileDownloaded {

    private static final String DIALOGS_TAB = "tab1";
    private static final String ROOMS_TAB = "tab2";
    private static final String CONTACTS_TAB = "tab3";
    private static final String PROFILE_TAB = "tab4";

    private ChatApplication app;
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        app = ChatApplication.getInstance();
        GCMHelper.register(this);

        setupTabs();
        switchProgressDialog(true);
        app.getRstManager().sendPresence(this);
        downloadStartUpInfo();
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

    private void downloadStartUpInfo() {
        app.getQbm().addUserProfileListener(this);
        app.getMsgManager().downloadPersistentRoom();
        boolean isNeedLoadUsersFromQb = app.getRstManager().getContactListFromRoster();
        if (!isNeedLoadUsersFromQb) {
            switchProgressDialog(false);
            app.getQbm().removeUserProfileListener(this);
        }

    }

    public void switchProgressDialog(boolean enable) {


        if (enable) {
            progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.loading), true);
        } else {

            if (progress == null) {
                return;
            }
            progress.dismiss();
        }
    }

    @Override
    public void downloadComplete(QBUser friend, ContextForDownloadUser pContextForDownloadUser) {
        if (pContextForDownloadUser == ContextForDownloadUser.DOWNLOAD_FOR_MAIN_ACTIVITY) {
            switchProgressDialog(false);
            app.getQbm().removeUserProfileListener(MainActivity.this);
        }
    }
}