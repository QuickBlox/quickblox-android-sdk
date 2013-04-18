package com.quickblox.chat_v2.apis;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster;

public class MessageManager implements MessageListener {
	
	
	// Глобальный слушатель
	@Override
	public void processMessage(Chat chat, Message message) {
		if (message.getBody() == null) {
			return;
		}
		
	}
	
	/**
	 * Метод отправки сообщения на QB по xmpp
	 * 
	 * @param userId
	 * @param messageBody
	 */
	public void sendMessage(Integer userId, String messageBody) {
		QBChat.sendMessage(userId, messageBody);
		
		// Add new message to local restore
	}
	
}