package com.quickblox.chat_v2.apis;

import java.util.Collection;

import org.jivesoftware.smack.packet.Presence;

import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;

public class RosterManager implements QBRosterListener {
	
	
	@Override
	public void entriesAdded(Collection<Integer> arg0) {
		System.out.println("ADD");
		System.out.println("entress added = " + arg0.toString());
		
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
	
}
