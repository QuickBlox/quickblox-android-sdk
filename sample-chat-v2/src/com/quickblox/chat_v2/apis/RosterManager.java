package com.quickblox.chat_v2.apis;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Presence;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;
import com.quickblox.module.chat.xmpp.SubscriptionListener;
import com.quickblox.module.users.model.QBUser;

public class RosterManager implements QBRosterListener, SubscriptionListener {
	
	private ArrayList<String> subscribes;
	
	public RosterManager() {
		QBChat.startAutoSendPresence(30);
		subscribes = new ArrayList<String>();
		
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
		System.out.println("Приход запроса на авторизацию. "+userId);
		
		subscribes.add(String.valueOf(userId));
		ChatApplication.getInstance().getQbm().getQbUserInfo(subscribes, GlobalConsts.REQUEST_CONTEXT_CONTACTS_CANDIDATE);
	}
	
	@Override
	public void onUnSubscribe(int userId) {
	}
	
	public void sendRequestToSubscribe(int userId){ 
		QBChat.subscribed(userId);
	}
}
