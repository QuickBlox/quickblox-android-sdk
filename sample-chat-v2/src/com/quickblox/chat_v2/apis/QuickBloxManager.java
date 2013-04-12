package com.quickblox.chat_v2.apis;

import java.util.List;

import com.quickblox.chat_v2.others.ChatApplication;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserPagedResult;

public class QuickBloxManager {
	
	private ChatApplication app;
	
	public QuickBloxManager() {
		app = ChatApplication.getInstance();
	}
	
	public void getQbUserInfo(List<String> usersIds) {
		
		QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
				
				QBUserPagedResult usersResult = (QBUserPagedResult) result;
				app.setChatUserList(usersResult.getUsers());	
			}
			
		});
	}
}
