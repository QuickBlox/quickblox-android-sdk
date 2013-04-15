package com.quickblox.chat_v2.interfaces;

import org.jivesoftware.smack.packet.Message;

public interface onReciveNewMessageToCurrentChat {

	
	public void reciveNewMessage(Message incomeMessage);
}
