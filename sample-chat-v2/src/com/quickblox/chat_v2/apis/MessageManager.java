package com.quickblox.chat_v2.apis;

import java.io.File;
import java.util.HashMap;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnDialogCreateComplete;
import com.quickblox.chat_v2.interfaces.OnDialogListRefresh;
import com.quickblox.chat_v2.interfaces.OnFriendProfileDownloaded;
import com.quickblox.chat_v2.interfaces.OnMessageListDownloaded;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.custom.result.QBCustomObjectLimitedResult;
import com.quickblox.module.custom.result.QBCustomObjectResult;
import com.quickblox.module.users.model.QBUser;

public class MessageManager implements MessageListener, OnPictureDownloadComplete, OnFriendProfileDownloaded {
	
	private Context context;
	private ChatApplication app;
	
	private String message;
	private String backgroundMessage;
	private int authorId;
	private int opponentId;
	private QBUser tQbuser;
	private boolean isNeedreview;
	
	private OnMessageListDownloaded listDownloadedListener;
	private OnDialogCreateComplete dialogCreateListener;
	
	private OnDialogListRefresh dialogRefreshListener;
	
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
		
		QBCustomObject localResult = dialogReview(Integer.parseInt(id[0]));
		
		if (localResult != null) {
			updateDialogLastMessage(message.getBody(), localResult.getCustomObjectId());
		} else {
			backgroundMessage = message.getBody();
			((Activity)context).runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					app.getQbm().setFriendProvileListener(MessageManager.this);
					app.getQbm().getSingleUserInfo(opponentId);
					
				}
			});	
		}
		
		if (message.getBody().substring(0, 13).equals(GlobalConsts.ATTACH_INDICATOR)) {
			String[] parts = message.getBody().split("#");
			
			QBUser tmpUser = new QBUser();
			tmpUser.setFileId(Integer.parseInt(parts[1]));
			app.getQbm().downloadQBFile(tmpUser);
		}
	}
	
	// send messages into xmpp & customobject
	public void sendSingleMessage(Integer userId, String messageBody, String dialogId) {
		
		if (messageBody == null && dialogId == null && userId == null) {
			return;
		}
		
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
	
	public synchronized void createDialog(QBUser qbuser, boolean isNeedExtraReview) {
		
		opponentId = qbuser.getId();
		isNeedreview = isNeedExtraReview;
		tQbuser = qbuser;
		
		if (isNeedExtraReview) {
			QBCustomObject oldDialog = dialogReview(opponentId);
			
			if (oldDialog != null) {
				dialogCreateListener.dialogCreate(opponentId, oldDialog.getCustomObjectId());
				return;
			}
		}
		
		((Activity) context).runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				QBCustomObject co = new QBCustomObject();
				HashMap<String, Object> fields = new HashMap<String, Object>();
				fields.put(GlobalConsts.RECEPIENT_ID_FIELD, tQbuser.getId());
				fields.put(GlobalConsts.NAME_FIELD, tQbuser.getFullName());
				co.setFields(fields);
				co.setClassName(GlobalConsts.DIALOGS_CLASS);
				QBCustomObjects.createObject(co, new QBCallbackImpl() {
					@Override
					public void onComplete(Result result) {
						if (result.isSuccess()) {
							
							if (isNeedreview) {
								dialogCreateListener.dialogCreate(opponentId, ((QBCustomObjectResult) result).getCustomObject().getCustomObjectId());
							} else {
								updateDialogLastMessage(backgroundMessage ,((QBCustomObjectResult)result).getCustomObject().getCustomObjectId());
								dialogRefreshListener.refreshList();
							}
						} else {
							
							Log.d("Error", "all bad");
							
						}
					}
				});
				
			}
		});
	}
	
	public void downloadDialogList() {
		QBCustomObjectRequestBuilder requestBuilder = new QBCustomObjectRequestBuilder();
		
		requestBuilder.eq(GlobalConsts.USER_ID_FIELD, app.getQbUser() != null ? app.getQbUser().getId() : app.getFbUser().getId());
		QBCustomObjects.getObjects(GlobalConsts.DIALOGS, requestBuilder, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					ChatApplication.getInstance().setDialogList(((QBCustomObjectLimitedResult) result).getCustomObjects());
					dialogRefreshListener.refreshList();
				}
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
	
	public void updateDialogLastMessage(String lastMsg, String dialogId) {
		QBCustomObject co = new QBCustomObject();
		co.setClassName(GlobalConsts.DIALOGS);
		HashMap<String, Object> fields = new HashMap<String, Object>();
		fields.put(GlobalConsts.LAST_MSG, lastMsg);
		co.setFields(fields);
		co.setCustomObjectId(dialogId);
		QBCustomObjects.updateObject(co, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				dialogRefreshListener.refreshList();
			}
		});
	}
	
	public QBCustomObject dialogReview(int opponentId) {
		for (QBCustomObject dialog : app.getDialogList()) {
			HashMap<String, Object> test = dialog.getFields();
			if (Integer.parseInt((String) test.get(GlobalConsts.RECEPIENT_ID_FIELD)) == opponentId) {
				return dialog;
			}
		}
		return null;
	}
	
	@Override
	public void downloadComlete(QBUser friend) {
				createDialog(friend, false);
	}
	
	public void setListDownloadedListener(OnMessageListDownloaded listDownloadedListener) {
		this.listDownloadedListener = listDownloadedListener;
	}
	
	@Override
	public void downloadComlete(Bitmap bitmap, File file) {
		
	}
	
	public void setDialogCreateListener(OnDialogCreateComplete dialogCreateListener) {
		this.dialogCreateListener = dialogCreateListener;
	}
	
	public void setDialogRefreshListener(OnDialogListRefresh dialogRefreshListener) {
		this.dialogRefreshListener = dialogRefreshListener;
	}
}