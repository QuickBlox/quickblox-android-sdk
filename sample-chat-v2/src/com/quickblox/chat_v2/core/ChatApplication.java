package com.quickblox.chat_v2.core;

import java.util.List;

import android.app.Application;
import android.graphics.Bitmap;

import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.apis.PictureManager;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/12/13 Time: 4:41
 * PM
 */
public class ChatApplication extends Application {
	
	private static ChatApplication instance;
	
	public ChatApplication(){
		instance = this;
	}
	public static ChatApplication getInstance(){
		return instance;
	}
	
	
	private Bitmap myPic;
	private QBUser qbUser;
	private QBUser fbUser;
	
	private MessageManager msgManager;
	private QuickBloxManager qbm;
	private PictureManager picManager;
	private RosterManager rstManager;
	
	 private List<QBCustomObject> dialogList;
	    
	
	//
	@Override
	public void onCreate() {
		super.onCreate();
		SmackAndroid.init(this);
	}
	
	// DATA
	
	public Bitmap getMyPic() {
		return myPic;
	}
	
	public void setMyPic(Bitmap myPic) {
		this.myPic = myPic;
	}
	
	public QBUser getQbUser() {
		return qbUser;
	}
	
	public void setQbUser(QBUser qbUser) {
		this.qbUser = qbUser;
	}
	
	public QuickBloxManager getQbm() {
		return qbm;
	}
	
	public void setQbm(QuickBloxManager qbm) {
		this.qbm = qbm;
	}

	public QBUser getFbUser() {
		return fbUser;
	}

	public void setFbUser(QBUser fbUser) {
		this.fbUser = fbUser;
	}

	public MessageManager getMsgManager() {
		return msgManager;
	}

	public void setMsgManager(MessageManager msgManager) {
		this.msgManager = msgManager;
	}

	public List<QBCustomObject> getDialogList() {
		return dialogList;
	}

	public void setDialogList(List<QBCustomObject> dialogList) {
		this.dialogList = dialogList;
	}
	public PictureManager getPicManager() {
		return picManager;
	}
	public void setPicManager(PictureManager picManager) {
		this.picManager = picManager;
	}
	public RosterManager getRstManager() {
		return rstManager;
	}
	public void setRstManager(RosterManager rstManager) {
		this.rstManager = rstManager;
	}
}
