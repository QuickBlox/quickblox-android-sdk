package com.quickblox.chat_v2.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Application;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.apis.FaceBookManager;
import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.apis.PictureManager;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/12/13 Time: 4:41
 * PM
 */
public class ChatApplication extends Application {

    private static ChatApplication instance;

    public ChatApplication() {
        instance = this;
    }

    public static ChatApplication getInstance() {
        return instance;
    }

    private Bitmap myPic;
    private QBUser qbUser;
    private String accessTokien;

    private MessageManager msgManager;
    private QuickBloxManager qbm;
    private FaceBookManager fbm;
    private PictureManager picManager;
    private RosterManager rstManager;

    private QBChatRoster qbRoster;

    private ArrayList<QBCustomObject> dialogList;
    private ArrayList<QBCustomObject> userPresentRoomList;

    private ArrayList<QBUser> contactsList;
    private ArrayList<QBUser> contactsCandidateList;
    private ArrayList<String> inviteUserList;
    private ArrayList<String> outSideInvite;

    private HashMap<String,QBUser> dialogsUsersMap;
    private HashMap<String, QBUser> contactsMap;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .displayer(new RoundedBitmapDisplayer(20)).build();

    //
    @Override
    public void onCreate() {
        super.onCreate();
        SmackAndroid.init(this);
    }

    public void clearAllData() {
        myPic = null;
        qbUser = null;
        msgManager = null;
        qbm = null;
        picManager = null;
        rstManager = null;
        dialogList = null;
        userPresentRoomList = null;
        contactsList = null;
        contactsCandidateList = null;
        inviteUserList = null;
        dialogsUsersMap = null;
        contactsMap = null;
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

    public MessageManager getMsgManager() {
        return msgManager;
    }

    public void setMsgManager(MessageManager msgManager) {
        this.msgManager = msgManager;
    }

    public ArrayList<QBCustomObject> getDialogList() {
        return dialogList;
    }

    public void setDialogList(ArrayList<QBCustomObject> dialogList) {
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

    public ArrayList<QBCustomObject> getUserPresentRoomList() {
        return userPresentRoomList;
    }

    public void setUserPresentRoomList(ArrayList<QBCustomObject> userPresentRoomList) {
        this.userPresentRoomList = userPresentRoomList;
    }

    public ArrayList<QBUser> getContactsList() {
        return contactsList;
    }

    public void setContactsList(ArrayList<QBUser> contactsList) {
        this.contactsList = contactsList;
    }

    public ArrayList<QBUser> getContactsCandidateList() {
        return contactsCandidateList;
    }

    public void setContactsCandidateList(ArrayList<QBUser> contactsCandidateList) {
        this.contactsCandidateList = contactsCandidateList;
    }

    public QBChatRoster getQbRoster() {
        return qbRoster;
    }

    public void setQbRoster(QBChatRoster qbRoster) {
        this.qbRoster = qbRoster;
    }

    public ArrayList<String> getInviteUserList() {
        return inviteUserList;
    }

    public void setInviteUserList(ArrayList<String> inviteUserList) {
        this.inviteUserList = inviteUserList;
    }

    public ArrayList<String> getOutSideInvite() {
        return outSideInvite;
    }

    public void setOutSideInvite(ArrayList<String> outSideInvite) {
        this.outSideInvite = outSideInvite;
    }

    public HashMap<String, QBUser> getDialogsUsersMap() {
        return dialogsUsersMap;
    }

    public void setDialogsUsersMap(HashMap<String, QBUser> dialogsUsersMap) {
        this.dialogsUsersMap = dialogsUsersMap;
    }
    public HashMap<String, QBUser> getContactsMap() {
        return contactsMap;
    }

    public void setContactsMap(HashMap<String, QBUser> contactsMap) {
        this.contactsMap = contactsMap;
    }

    public DisplayImageOptions getOptions() {
        return options;
    }

    public void setOptions(DisplayImageOptions options) {
        this.options = options;
    }

    public FaceBookManager getFbm() {
        return fbm;
    }

    public void setFbm(FaceBookManager fbm) {
        this.fbm = fbm;
    }

    public String getAccessTokien() {
        return accessTokien;
    }

    public void setAccessTokien(String accessTokien) {
        this.accessTokien = accessTokien;
    }
}
