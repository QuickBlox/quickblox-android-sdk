package com.quickblox.chat_v2.ui.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.apis.PictureManager;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.RoomReceivingListener;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.xmpp.LoginListener;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/8/13 Time: 1:34 PM
 */
public class MainActivity extends TabActivity {
	
	private static final String DIALOGS_TAB = "tab1";
	private static final String ROOMS_TAB = "tab2";
	private static final String CONTACTS_TAB = "tab3";
	private static final String PROFILE_TAB = "tab4";
	
	private QBChatRoster qbRoster;
	private ArrayList<String> userIds;
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
		signIn();
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
	
	private void signIn() {
		QBUsers.signIn(SharedPreferencesHelper.getLogin(MainActivity.this), SharedPreferencesHelper.getPassword(MainActivity.this), new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					System.out.println("GOOD");
					
					signInChat(((QBUserResult) result).getUser());
				} else {
					
					System.out.println("false");
					blockUi(false);
				}
			}
			
		});
	}
	private void signInChat(QBUser qbUser) {
		
		if (app.getFbUser() != null) {
			qbUser = ChatApplication.getInstance().getFbUser();
			qbUser.setPassword(SharedPreferencesHelper.getPassword(this));
			
		} else {
			qbUser = app.getQbUser();
			qbUser.setPassword(SharedPreferencesHelper.getPassword(this));
		}
		
		QBChat.loginWithUser(qbUser, new LoginListener() {
			
			@Override
			public void onLoginError() {
				System.out.println("Чёт не так");
				blockUi(false);
			}
			
			@Override
			public void onLoginSuccess() {
				System.out.println("GOOD");
				registerRoster();
				
			}
		});
	}
	
	private void registerRoster() {
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				
				rosterManager = new RosterManager();
				app.setRstManager(rosterManager);
				
				qbRoster = QBChat.registerRoster(rosterManager);
				userIds = new ArrayList<String>();
				
				QBChat.registerSubscription(rosterManager);
				
				if (qbRoster.getUsersId() != null) {
					for (Integer in : qbRoster.getUsersId()) {
						userIds.add(String.valueOf(in));
						System.out.println("User unit = "+in);
					}
					qbm.getQbUserInfo(userIds, GlobalConsts.REQUEST_CONTEXT_CONTACTS);
					
					System.out.println("Юзеры из ростера = "+userIds.size());
				}
				
				QBChat.openXmmpChat(msgManager);
				downloadRoomList();
				
			}
		});
		
	}
	private void downloadRoomList() {
		QBChat.requestJoinedRooms(app.getQbUser() != null ? app.getQbUser().getId() : app.getFbUser().getId(), new RoomReceivingListener() {
			
			@Override
			public void onReceiveRooms(List<String> roomId) {
				app.setUserPresentRoomList(new ArrayList<String>());
				
				for (String roomsUid : roomId) {
					String[] parts = roomsUid.split("_");
					app.getUserPresentRoomList().add(parts[0]);
				}
				blockUi(false);
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
}