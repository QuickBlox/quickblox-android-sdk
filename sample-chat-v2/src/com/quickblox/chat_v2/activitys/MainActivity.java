package com.quickblox.chat_v2.activitys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.TabHost;
import com.danikula.aibolit.Aibolit;
import com.danikula.aibolit.annotation.InjectView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.chat_v2.fragment.*;
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
public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener {


    private static Context context;
    private static FragmentManager fragmentManager;

    @InjectView(android.R.id.tabhost)
    private static TabHost tabHost;

    private QBChatRoster qbRoster;
    private RosterManager rosterManager;
    private QuickBloxManager qbm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Aibolit.doInjections(this);


        context = getBaseContext();
        fragmentManager = getSupportFragmentManager();

        if (TextUtils.isEmpty(SharedPreferencesHelper.getLogin())) {
            loadSplashScreen();
        } else {
            SharedPreferencesHelper.setLogin("ivanych1_100005029718675");
            SharedPreferencesHelper.setPassword("cc2f5f3249bcfaed7eb1322b07fdd61df27a7ef1");
            authWithUser();
            setupTabs();
        }

        qbm = new QuickBloxManager();
    }

    public static Context getContext() {
        return context;
    }

    public static FragmentManager getSupportFragmentActivityManager() {
        return fragmentManager;
    }

    public static void loadDialogScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        DialogsFragment dialogsFragment = new DialogsFragment();
        fragmentTransaction.replace(R.id.main, dialogsFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public static void loadRoomListScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        RoomListFragment roomsFragment = new RoomListFragment();
        fragmentTransaction.replace(R.id.main, roomsFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public static void loadContactsScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ContactsFragment contactsFragment = new ContactsFragment();
        fragmentTransaction.replace(R.id.main, contactsFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public static void loadProfileScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ProfileFragment profileFragment = new ProfileFragment();
        fragmentTransaction.replace(R.id.main, profileFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public static void loadNewRoomScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        CreateNewRoomFragment createNewRoomFragment = new CreateNewRoomFragment();
        fragmentTransaction.replace(R.id.main, createNewRoomFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void loadSplashScreen() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        TabHost.TabSpec spec; // Resusable TabSpec for each tab
        tabHost.setOnTabChangedListener(this);
        tabHost.setup(); // you must call this before adding your tabs!
        spec = tabHost.newTabSpec(getString(R.string.TAB_DIALOGS_TITLE)).setIndicator(getString(R.string.maintab_tab_1)).setContent(R.id.tab_dialogs);
        tabHost.addTab(spec);
        spec = tabHost.newTabSpec(getString(R.string.TAB_ROOMS_TITLE)).setIndicator(getString(R.string.maintab_tab_2)).setContent(R.id.tab_rooms);
        tabHost.addTab(spec);
        spec = tabHost.newTabSpec(getString(R.string.TAB_CONTACTS_TITLE)).setIndicator(getString(R.string.maintab_tab_3)).setContent(R.id.tab_contacts);
        tabHost.addTab(spec);
        spec = tabHost.newTabSpec(getString(R.string.TAB_PROFILE_TITLE)).setIndicator(getString(R.string.maintab_tab_4)).setContent(R.id.tab_profiles);
        tabHost.addTab(spec);
    }

    private void updateTab(String tabTitle) {
        if (tabTitle.equals(getString(R.string.TAB_DIALOGS_TITLE))) {
            loadDialogScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_ROOMS_TITLE))) {
            loadRoomListScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_CONTACTS_TITLE))) {
            loadContactsScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_PROFILE_TITLE))) {
            loadProfileScreen();
        }
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
                registerRoster();
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

            qbm.getQbUserUnfo(userIds);
        }
    }

    @Override
    public void onTabChanged(String tabTitle) {
        updateTab(tabTitle);
    }

    public static void showTabs() {
        tabHost.setVisibility(View.VISIBLE);
    }

    public static void hideTabs() {
        tabHost.setVisibility(View.INVISIBLE);
    }


}