package com.quickblox.chat_v2.apis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.packet.Presence;

import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;

public class RosterManager implements QBRosterListener {
	
	private Timer presenceTimer;
	private ArrayList<Integer> requestAutoriseNewUser;
	
	
	public RosterManager() {
		startOnlineTimer();
	}
	
	@Override
	public void entriesAdded(Collection<Integer> arg0) {
		System.out.println("entress added = " + arg0);
		requestAutoriseNewUser = new ArrayList<Integer>();
		requestAutoriseNewUser.addAll(arg0);
		
		
		
	}
	
	@Override
	public void entriesDeleted(Collection<Integer> arg0) {
		System.out.println("DEL");
		System.out.println("entress deleted = " + arg0.toString());
		
	}
	
	@Override
	public void entriesUpdated(Collection<Integer> arg0) {
		System.out.println("UPD");
		System.out.println("entress updated = " + arg0.toString());
		
	}
	
	@Override
	public void presenceChanged(Presence arg0) {
		System.out.println("presence = " + arg0.toString());
		
	}

	// timers
		private void startOnlineTimer() {
			
			presenceTimer = new Timer();
			presenceTimer.schedule(new TimerTask() {
				public void run() {
					QBChat.sendPresence();
					DataHolder app = DataHolder.getInstance();
					
				}
			}, 10000L, 30000L);
		}
}
