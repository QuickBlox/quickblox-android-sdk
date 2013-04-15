package com.quickblox.chat_v2.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.packet.Presence;

import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;

public class RosterManager implements QBRosterListener {
	
	private Timer presenceTimer;
	private ArrayList<Integer> requestAutoriseNewUser;
	
	
	public RosterManager() {
		startOnlineTimer();
	}
	
	@Override
	public void entriesAdded(Collection<Integer> addedEntriesIds) {
		System.out.println("entress added = " + addedEntriesIds);
		requestAutoriseNewUser = new ArrayList<Integer>();
		requestAutoriseNewUser.addAll(addedEntriesIds);
		
		
		
	}
	
	@Override
	public void entriesDeleted(Collection<Integer> deletedEntriesIds) {
		System.out.println("DEL");
		System.out.println("entress deleted = " + deletedEntriesIds.toString());
		
	}
	
	@Override
	public void entriesUpdated(Collection<Integer> updatedEntriesIds) {
		System.out.println("UPD");
		System.out.println("entress updated = " + updatedEntriesIds.toString());
		
	}
	
	@Override
	public void presenceChanged(Presence presence) {
		System.out.println("presence = " + presence.toString());
		
	}

	// timers
		private void startOnlineTimer() {
			
			presenceTimer = new Timer();
			presenceTimer.schedule(new TimerTask() {
				public void run() {
					QBChat.sendPresence();					
				}
			}, 10000L, 30000L);
		}
}
