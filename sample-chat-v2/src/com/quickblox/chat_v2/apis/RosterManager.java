package com.quickblox.chat_v2.apis;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Context;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;
import com.quickblox.module.chat.xmpp.SubscriptionListener;

public class RosterManager implements QBRosterListener, SubscriptionListener {
	
	private ArrayList<String> subscribes;
	private ArrayList<String> userIds;
	
	private Context context;
	private ChatApplication app;
	
	private int userID;
	
	public RosterManager(Context context) {
		this.context = context;
		QBChat.startAutoSendPresence(30);
		subscribes = new ArrayList<String>();
		app = ChatApplication.getInstance();
	}
	
	@Override
	public void entriesAdded(Collection<Integer> addedEntriesIds) {
	}
	
	@Override
	public void entriesDeleted(Collection<Integer> deletedEntriesIds) {
		System.out.println("entress deleted = " + deletedEntriesIds.toString());
		
	}
	
	@Override
	public void entriesUpdated(Collection<Integer> updatedEntriesIds) {
		System.out.println("entress updated = " + updatedEntriesIds.toString());
		
	}
	
	@Override
	public void presenceChanged(Presence presence) {
		System.out.println("presence = " + presence.toString());
		
	}
	
	@Override
	public void onSubscribe(int userId) {
		System.out.println("Приход запроса на авторизацию. " + userId);
		
		subscribes.add(String.valueOf(userId));
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				ChatApplication.getInstance().getQbm().getQbUsersInfoCandidate(subscribes);
				
			}
		});
		
	}
	
	@Override
	public void onUnSubscribe(int userId) {
	}
	
	public synchronized void sendRequestToSubscribe(int userId) {
		System.out.println("Пытаюсь подтвердить. UID = "+userId);
		userID = userId; 
		((Activity)context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				QBChat.subscribed(userID);				
			}
		});
	}
	
	public void refreshContactList() {
		userIds = new ArrayList<String>();
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (app.getQbRoster().getUsersId() != null) {
					for (Integer in : app.getQbRoster().getUsersId()) {
						userIds.add(String.valueOf(in));
					}
					app.getQbm().getQbUsersInfoContact(userIds);
				}
			}
		});
	}
}
