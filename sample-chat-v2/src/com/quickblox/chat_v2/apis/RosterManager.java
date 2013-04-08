package com.quickblox.chat_v2.apis;

import java.util.Collection;

import org.jivesoftware.smack.packet.Presence;

import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.model.QBChatRoster.QBRosterListener;



public class RosterManager implements QBRosterListener{
	

		
		QBChatRoster roster = QBChat.registerRoster(this); 
		
	

	@Override
	public void entriesAdded(Collection<Integer> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entriesDeleted(Collection<Integer> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entriesUpdated(Collection<Integer> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void presenceChanged(Presence arg0) {
		// TODO Auto-generated method stub
		
	}
	

}
