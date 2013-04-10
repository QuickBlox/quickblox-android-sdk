package com.quickblox.chat_v2.others;

import java.util.ArrayList;

import android.app.Application;
import android.util.Log;

import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.users.model.QBUser;

public class ChatApplication extends Application {
	
	private ArrayList<QBUser> chatUserList;
	
	//
	@Override
	public void onCreate() {
		Log.d("APPP", "OnCreate");
		
		super.onCreate();
		SmackAndroid.init(this);
	}
	
	// INIT
	static ChatApplication instance;
	
	public static ChatApplication getInstance() {
		return instance;
	}
	
	public ChatApplication() {
		instance = this;
	}
	
	// DATA
	
	public ArrayList<QBUser> getChatUserList() {
		return chatUserList;
	}
	
	public void setChatUserList(ArrayList<QBUser> chatUserList) {
		this.chatUserList = chatUserList;
	}
	
}