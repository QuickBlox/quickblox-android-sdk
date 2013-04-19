package com.quickblox.chat_v2.apis;

import java.util.HashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Context;

import com.google.common.base.Splitter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;

public class MessageManager implements MessageListener {
	
	private Context context;
	private ChatApplication app;
	
	private String message;
	private int authorId;
	private int opponentId;
	
	public MessageManager(Context context) {
		this.context = context;
		app = ChatApplication.getInstance();
	}
	
	// Глобальный слушатель
	@Override
	public void processMessage(Chat chat, Message message) {
		if (message.getBody() == null) {
			return;
		}
		System.out.println("Сообщение = " + message.getBody());
		
		String[] id = message.getFrom().split("-");
		System.out.println("ID = "+ id[0]);
		
		sendToQB(Integer.parseInt(id[0]), message.getBody(), Integer.parseInt(id[0]));
		
	}
	
	// send messages into xmpp & customobject
	public void sendSingleMessage(Integer userId, String messageBody) {
		QBChat.sendMessage(userId, messageBody);
		sendToQB(userId, messageBody, app.getQbUser() !=null ? app.getQbUser().getId() : app.getFbUser().getId());
	}
	
	
	private void sendToQB(Integer opponentID, String messageBody, Integer authorID) {
		message = messageBody; 
		authorId = authorID;
		opponentId = opponentID;
		
		((Activity) context).runOnUiThread(new Runnable() {	
			@Override
			public void run() {
				QBCustomObject custobj = new QBCustomObject();
				
				custobj.setClassName(GlobalConsts.MESSAGES);
				
				HashMap<String, Object> fields = new HashMap<String, Object>();
				
				fields.put(GlobalConsts.AUTHOR_ID, authorId);
				fields.put(GlobalConsts.OPPONENT_ID, opponentId);
				fields.put(GlobalConsts.MSG_TEXT, message);
				
				custobj.setFields(fields);
				
				QBCustomObjects.createObject(custobj, new QBCallbackImpl() {
					@Override
					public void onComplete(Result result) {
						System.out.println("Сообщение отправлено на QB");
					}
				});
			}
				
			
		});
		
		
}}