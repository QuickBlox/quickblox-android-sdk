package com.quickblox.chat_v2.apis;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.quickblox.chat_v2.interfaces.onReciveNewMessageToCurrentChat;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoster;

public class MessageManager implements MessageListener {
	
	private int currentChatUserId;
	private onReciveNewMessageToCurrentChat newMessageListener;
	
	// Глобальный слушатель
	@Override
	public void processMessage(Chat chat, Message message) {
		if (message.getBody() == null) {
			return;
		}
		
		System.out.println("message = "+message);
		
		if (currentChatUserId != 0 && message.getFrom().equals(currentChatUserId)) {
			newMessageListener.reciveNewMessage(message);
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
	/**
	 * Метод получения всех сообщений Пока банан
	 * 
	 * @param roster
	 */
	public void getAllMessages(QBChatRoster roster) {
		
	}
	
	// Будет ясно в понедельник
	public void getAllmessagesFromRoom() {
		
	}
	
	// Setters
	public void setCurrentChatUserId(int currentChatUserId) {
		this.currentChatUserId = currentChatUserId;
	}
	
	// Listeners
	public void setNewMessageListener(onReciveNewMessageToCurrentChat newMessageListener) {
		this.newMessageListener = newMessageListener;
	}
	
}