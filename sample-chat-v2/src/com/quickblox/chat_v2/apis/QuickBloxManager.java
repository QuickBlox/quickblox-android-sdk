package com.quickblox.chat_v2.apis;

import java.util.List;

import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.result.QBUserPagedResult;

public class QuickBloxManager {
	
	
	
	public void getQbUserInfo(List<String> usersIds) {
		
		QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
				QBUserPagedResult usersResult = (QBUserPagedResult) result;
				//ChatApplication.getInstance().setContactUserList(usersResult.getUsers());
			}
		});
		
	}
	
	public void getQbUserInfo(List<String> usersIds, int flag) {
		
		QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
				
				QBUserPagedResult usersResult = (QBUserPagedResult) result;
				
				//ChatApplication.getInstance().setSubscribeUserList(usersResult.getUsers());				
			}
		});
	}
}
