package com.quickblox.chat_v2.ui.activities;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TabHost;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.xmpp.LoginListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/8/13 Time: 1:34 PM
 */
public class MainActivity extends TabActivity {


    private static Context context;
    private QBChatRoster qbRoster;
    private RosterManager rosterManager;
    private QuickBloxManager qbm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        context = getBaseContext();

        if (TextUtils.isEmpty(SharedPreferencesHelper.getLogin())) {
            loadSplashScreen();
        } else {
            SharedPreferencesHelper.setLogin("supersample-android");
            SharedPreferencesHelper.setPassword("supersample-android");
            authWithUser();
        }

        qbm = new QuickBloxManager();
    }

    public static Context getContext() {
        return context;
    }

    private void loadSplashScreen() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.TabSpec dialogs = tabHost.newTabSpec("tab1");
        TabHost.TabSpec rooms = tabHost.newTabSpec("tab2");
        TabHost.TabSpec contacts = tabHost.newTabSpec("tab3");
        TabHost.TabSpec profile = tabHost.newTabSpec("tab4");

        dialogs.setIndicator("     " + getString(R.string.TAB_DIALOGS_TITLE) + "     ")
                .setContent(new Intent(this, DialogsActivity.class));

        rooms.setIndicator("     " + getString(R.string.TAB_ROOMS_TITLE) + "     ")
                .setContent(new Intent(this, RoomsActivity.class));

        contacts.setIndicator("     " + getString(R.string.TAB_CONTACTS_TITLE) + "     ")
                .setContent(new Intent(this, ContactsActivity.class));

        profile.setIndicator("     " + getString(R.string.TAB_PROFILE_TITLE) + "     ")
                .setContent(new Intent(this, ProfileActivity.class));

        tabHost.addTab(dialogs);
        tabHost.addTab(rooms);
        tabHost.addTab(contacts);
        tabHost.addTab(profile);
    }

    private void authWithUser() {

        QBSettings.getInstance().fastConfigInit(getResources().getString(R.string.quickblox_app_id), getResources().getString(R.string.quickblox_auth_key),
                getResources().getString(R.string.quickblox_auth_secret));

        QBAuth.createSession(new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    signIn();
                }
            }
        });
    }

    private void signIn() {
        QBUsers.signIn(SharedPreferencesHelper.getLogin(), SharedPreferencesHelper.getPassword(), new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBUser qbUser = ((QBUserResult) result).getUser();
                    DataHolder.getInstance().setQbUser(qbUser);
                    signInChat(qbUser);
                }
            }
        });

    }

    private void signInChat(QBUser qbUser) {

        qbUser.setPassword(SharedPreferencesHelper.getPassword());
        QBChat.loginWithUser(qbUser, new LoginListener() {

            @Override
            public void onLoginError() {
            }

            @Override
            public void onLoginSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupTabs();
                    }
                });
            }
        });
    }

    private void registerRoster() {

        rosterManager = new RosterManager();
        qbRoster = QBChat.registerRoster(rosterManager);
        List<String> userIds = new ArrayList<String>();

        if (userIds.size() > 0) {
            for (Integer id : qbRoster.getUsersId()) {
                userIds.add(String.valueOf(id));
            }

            qbm.getQbUserInfo(userIds);
        }
    }


//    public static void showTabs() {
//        tabHost.setVisibility(View.VISIBLE);
//    }
//
//    public static void hideTabs() {
//        tabHost.setVisibility(View.INVISIBLE);
//    }


//    public static void loadNewDialogScreen() {
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//        NewDialogActivity newDialogFragment = new NewDialogActivity();
//        fragmentTransaction.replace(R.id.main, newDialogFragment);
//        fragmentTransaction.commitAllowingStateLoss();
//    }
}