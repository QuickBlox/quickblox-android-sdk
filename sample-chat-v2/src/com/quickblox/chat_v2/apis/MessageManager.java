package com.quickblox.chat_v2.apis;

import java.io.File;
import java.util.HashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.users.model.QBUser;

public class MessageManager implements MessageListener, OnPictureDownloadComplete {
	
	private Context context;
	private ChatApplication app;
	
	private String message;
	private int authorId;
	private int opponentId;
	
	private OnMessageListDownloaded listDownloadedListener;
	
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
		String[] id = message.getFrom().split("-");
		sendToQB(Integer.parseInt(id[0]), message.getBody(), Integer.parseInt(id[0]));
		
		if (message.getBody().substring(0,13).equals(GlobalConsts.ATTACH_INDICATOR)){
			String[] parts = message.getBody().split("#");
			
			QBUser tmpUser = new QBUser();
			tmpUser.setFileId(Integer.parseInt(parts[1]));
			app.getQbm().downloadQBFile(tmpUser);
		}
		
	}
	
	// send messages into xmpp & customobject
	public void sendSingleMessage(Integer userId, String messageBody, String dialogId) {
		QBChat.sendMessage(userId, messageBody);
		
		sendToQB(userId, messageBody, app.getQbUser() != null ? app.getQbUser().getId() : app.getFbUser().getId());
		updateDialogLastMessage(messageBody, dialogId);
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
		
	}
	
	public void getDialogMessages(int userId) {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		requestBuilder.eq(GlobalConsts.USER_ID_FIELD, app.getQbUser() != null ? app.getQbUser().getId() : app.getFbUser().getId());
		requestBuilder.eq(GlobalConsts.OPPONENT_ID, userId);
		requestBuilder.sortAsc("created_at");
		
		QBCustomObjects.getObjects(GlobalConsts.MESSAGES, requestBuilder, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					listDownloadedListener.messageListDownloaded(((QBCustomObjectLimitedResult) result).getCustomObjects());
				}
			}
		});
	}
	
	public  void updateDialogLastMessage(String lastMsg, String dialogId) {
		QBCustomObject co = new QBCustomObject();
		co.setClassName(GlobalConsts.DIALOGS);
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(GlobalConsts.LAST_MSG, lastMsg);
		co.setFields(fields);
		co.setCustomObjectId(dialogId);
		QBCustomObjects.updateObject(co, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				
			}
		});
	}
		public void sendRoomMessage(String msg, int roomId){
			
		}
	
	public void setListDownloadedListener(OnMessageListDownloaded listDownloadedListener) {
		this.listDownloadedListener = listDownloadedListener;
	}

	@Override
	public void downloadComlete(Bitmap bitmap, File file) {
		
	}
}