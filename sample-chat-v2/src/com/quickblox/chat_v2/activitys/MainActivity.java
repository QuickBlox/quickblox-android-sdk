package com.quickblox.chat_v2.activitys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.widget.TabHost;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.fragment.ContactsFragment;
import com.quickblox.chat_v2.fragment.DialogsFragment;
import com.quickblox.chat_v2.fragment.ProfileFragment;
import com.quickblox.chat_v2.fragment.RoomsFragment;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.QBUsers;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/8/13
 * Time: 1:34 PM
 */
public class MainActivity extends FragmentActivity implements TabHost.OnTabChangeListener {

    private static Context context;
    private static FragmentManager fragmentManager;
    private TabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        context = getBaseContext();
        fragmentManager = getSupportFragmentManager();
        if (TextUtils.isEmpty(SharedPreferencesHelper.getAccessToken())) {
            loadSplashScreen();
        } else {
            auth();
            setupTabs();
        }
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

    public static void loadRoomsScreen() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        RoomsFragment roomsFragment = new RoomsFragment();
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


    private void loadSplashScreen() {
        Intent intent = new Intent(getContext(), SplashActivity.class);
        startActivity(intent);
        finish();
    }


    private void setupTabs() {
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.setup(); // you must call this before adding your tabs!
        spec = mTabHost.newTabSpec(getString(R.string.TAB_DIALOGS_TITLE))
                .setIndicator(getString(R.string.maintab_tab_1))
                .setContent(R.id.tab_dialogs);
        mTabHost.addTab(spec);
        spec = mTabHost.newTabSpec(getString(R.string.TAB_ROOMS_TITLE))
                .setIndicator(getString(R.string.maintab_tab_2))
                .setContent(R.id.tab_rooms);
        mTabHost.addTab(spec);
        spec = mTabHost.newTabSpec(getString(R.string.TAB_CONTACTS_TITLE))
                .setIndicator(getString(R.string.maintab_tab_3))
                .setContent(R.id.tab_contacts);
        mTabHost.addTab(spec);
        spec = mTabHost.newTabSpec(getString(R.string.TAB_PROFILE_TITLE))
                .setIndicator(getString(R.string.maintab_tab_4))
                .setContent(R.id.tab_profiles);
        mTabHost.addTab(spec);
    }

    private void updateTab(String tabTitle) {
        if (tabTitle.equals(getString(R.string.TAB_DIALOGS_TITLE))) {
            loadDialogScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_ROOMS_TITLE))) {
            loadRoomsScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_CONTACTS_TITLE))) {
            loadContactsScreen();
        } else if (tabTitle.equals(getString(R.string.TAB_PROFILE_TITLE))) {
            loadProfileScreen();
        }
    }

    private void auth() {
        QBUsers.signInUsingSocialProvider(QBProvider.FACEBOOK, SharedPreferencesHelper.getAccessToken(), null, new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
//                if (result.isSuccess()) {
//
//                    QBUser currentUser = ((QBUserResult) result).getUser();
//
//                    if (arg1.equals("social")) {
//                        // Set Chat password
//                        try {
//                            currentUser.setPassword(BaseService.getBaseService().getToken());
//                        } catch (BaseServiceException e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        app.setAuthUser(currentUser);
//                    }
//                }
            }
        });
    }

    private void signInChat() {


//        // LOGIN IN XMMP
//        QBChat.loginWithUser(currentUser, new LoginListener() {
//
//            @Override
//            public void onLoginError() {
//                progress.dismiss();
//                Toast.makeText(SplashActivity.this, getResources().getString(R.string.splash_login_reject), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onLoginSuccess() {
//
////				Intent intent = new Intent(SplashActivity.this, MainTabActivity.class);
////				startActivity(intent);
////				finish();
//            }
//
//        });
    }


    @Override
    public void onTabChanged(String tabTitle) {
        updateTab(tabTitle);
    }
}
