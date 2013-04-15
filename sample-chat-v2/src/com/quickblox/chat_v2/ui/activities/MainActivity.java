package com.quickblox.chat_v2.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TabHost;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.MessageManager;
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

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/8/13 Time: 1:34 PM
 */
public class MainActivity extends TabActivity {

	private static final String DIALOGS_TAB = "tab1";
    private static final String ROOMS_TAB = "tab2";
    private static final String CONTACTS_TAB = "tab3";
    private static final String PROFILE_TAB = "tab4";

    private QBChatRoster qbRoster;
    
    private RosterManager rosterManager;
    private QuickBloxManager qbm;
    private MessageManager msgManager;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (TextUtils.isEmpty(SharedPreferencesHelper.getLogin(getBaseContext()))) {
            loadSplashScreen();
        } else {
           
            authWithUser();
        }
        initViews();       
}

    private void initViews() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        qbm = new QuickBloxManager();
    }

    private void loadSplashScreen() {
        Intent intent = new Intent(getBaseContext(), SplashActivity.class);
        startActivity(intent);
        finish();
    }

    private void setupTabs() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.TabSpec dialogs = tabHost.newTabSpec(DIALOGS_TAB);
        TabHost.TabSpec rooms = tabHost.newTabSpec(ROOMS_TAB);
        TabHost.TabSpec contacts = tabHost.newTabSpec(CONTACTS_TAB);
        TabHost.TabSpec profile = tabHost.newTabSpec(PROFILE_TAB);

        dialogs.setIndicator(getString(R.string.TAB_DIALOGS_TITLE))
                .setContent(new Intent(this, DialogsActivity.class));

        rooms.setIndicator(getString(R.string.TAB_ROOMS_TITLE))
                .setContent(new Intent(this, RoomsActivity.class));

        contacts.setIndicator(getString(R.string.TAB_CONTACTS_TITLE))
                .setContent(new Intent(this, ContactsActivity.class));
        
        profile.setIndicator(getString(R.string.TAB_PROFILE_TITLE))
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
                } else {
                    reportError(result.getErrors().get(0));
                }
            }
        });
    }

    private void signIn() {
        QBUsers.signIn(SharedPreferencesHelper.getLogin(getBaseContext()), SharedPreferencesHelper.getPassword(getBaseContext()), new QBCallbackImpl() {
            @Override
            public void onComplete(Result result) {
                if (result.isSuccess()) {
                    QBUser qbUser = ((QBUserResult) result).getUser();
                    DataHolder.getInstance().setQbUser(qbUser);
                    signInChat(qbUser);
                    
                } else {
                    reportError(result.getErrors().get(0));
                }
            }
        });

    }

    private void reportError(String errorMsg) {
        progressDialog.hide();
        Toast.makeText(getBaseContext(), errorMsg, Toast.LENGTH_SHORT).show();
    }

    private void signInChat(QBUser qbUser) {

        qbUser.setPassword(SharedPreferencesHelper.getPassword(getBaseContext()));
        QBChat.loginWithUser(qbUser, new LoginListener() {

            @Override
            public void onLoginError() {
                reportError(getString(R.string.check_connection_error));
            }

            @Override
            public void onLoginSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupTabs();
                        progressDialog.hide();
       
                        msgManager = new MessageManager();
                        registerRoster();
                       
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
       registerMessageListener();
    }
    
	private void registerMessageListener() {
		QBChat.openXmmpChat(msgManager);
	}
}