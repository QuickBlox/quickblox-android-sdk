package com.quickblox.chat_v2.apis;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnFriendProfileDownloaded;
import com.quickblox.chat_v2.interfaces.OnPictureConvertComplete;
import com.quickblox.core.QBCallback;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.module.users.result.QBUserResult;

public class QuickBloxManager {
	
	private ChatApplication app;
	
	private OnFriendProfileDownloaded friendProvileListener;
	private OnPictureConvertComplete pictureConvertListener;
	
	public QuickBloxManager() {
		app = ChatApplication.getInstance();
	}
	
	public void getQbUserInfo(List<String> usersIds) {
		
		QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
				QBUserPagedResult usersResult = (QBUserPagedResult) result;
				// ChatApplication.getInstance().setContactUserList(usersResult.getUsers());
			}
		});
		
	}
	
	public void getQbUserInfo(List<String> usersIds, int flag) {
		
		QBUsers.getUsersByIDs(usersIds, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
				
				QBUserPagedResult usersResult = (QBUserPagedResult) result;
				
				// ChatApplication.getInstance().setSubscribeUserList(usersResult.getUsers());
			}
		});
	}
	
	public void uploadPic(File file) {
		
		QBRequestCanceler requestCanceler = QBContent.uploadFileTask(file, true, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				
				if (result.isSuccess()) {
					
					QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;
					if (app.getFbUser() != null) {
						app.getFbUser().setFileId(fileUploadTaskResultResult.getFile().getId());
						updateQBUser(app.getFbUser());
					} else {
						
						app.getQbUser().setFileId(fileUploadTaskResultResult.getFile().getId());
						updateQBUser(app.getQbUser());
					}
				}
			}
		});
	}
	
	private void updateQBUser(QBUser upadtedUser) {
		
		QBUsers.updateUser(upadtedUser, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				System.out.println("User updated");
			}
			
		});
	}
	
	public void getQbFileToBitmap(QBUser currentUser) {
		
		if (currentUser.getFileId() == null){
			return;
		}
		
		QBContent.downloadFileTask(currentUser.getFileId(), new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
				if (result.isSuccess()) {
					
					InputStream is = qbFileDownloadResult.getContentStream();
					Bitmap userPic = BitmapFactory.decodeStream(is);
					pictureConvertListener.downloadComlete(userPic);
				}
				
			}
		});
	}
	
	public void getSingleUserInfo(int userId) {
		QBUsers.getUser(userId, new QBCallback() {
			
			@Override
			public void onComplete(Result result, Object context) {
			}
			
			@Override
			public void onComplete(Result result) {
				friendProvileListener.downloadComlete(((QBUserResult) result).getUser());
			}
		});
	}

	//LISTENERS
	
	public void setFriendProvileListener(OnFriendProfileDownloaded friendProvileListener) {
		this.friendProvileListener = friendProvileListener;
	}

	public void setPictureConvertListener(OnPictureConvertComplete pictureConvertListener) {
		this.pictureConvertListener = pictureConvertListener;
	}
	
}
