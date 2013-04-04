package com.quickblox.chat_v2.others;

import com.quickblox.module.users.model.QBUser;

import android.app.Application;

public class ChatApplication extends Application {
		
	private QBUser authUser;
	
	
	// INIT
	static ChatApplication instance;
	
	public static ChatApplication getInstance() {
		return instance;
	}
	
	public ChatApplication() {
		instance = this;
	}
	

	// DATA

	public QBUser getAuthUser() {
		return authUser;
	}

	public void setAuthUser(QBUser authUser) {
		this.authUser = authUser;
	}
	
}