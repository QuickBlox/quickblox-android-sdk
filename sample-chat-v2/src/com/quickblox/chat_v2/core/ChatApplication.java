package com.quickblox.chat_v2.core;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.chat_v2.apis.FaceBookManager;
import com.quickblox.chat_v2.apis.MessageManager;
import com.quickblox.chat_v2.apis.PictureManager;
import com.quickblox.chat_v2.apis.QuickBloxManager;
import com.quickblox.chat_v2.apis.RosterManager;
import com.quickblox.module.chat.model.QBChatRoom;
import com.quickblox.module.chat.model.QBChatRoster;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashMap;

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
    private QBChatRoom joinedRoom;

    private MessageManager msgManager;
    private QuickBloxManager qbm;
    private FaceBookManager fbm;
    private PictureManager picManager;
    private RosterManager rstManager;

    private QBChatRoster qbRoster;

    private ArrayList<QBCustomObject> dialogList;
    private HashMap<String, QBCustomObject> dialogMap;
    private ArrayList<QBCustomObject> userPresentRoomList;

    private ArrayList<String> inviteUserList;

    private HashMap<String, QBUser> dialogsUsersMap;
    private HashMap<String, QBUser> contactsMap;
    private HashMap<String, QBUser> contactsCandidateMap;
    private HashMap<Integer, QBCustomObject> userIdDialogIdMap;
    private HashMap<Integer, String> userNetStatusMap;

    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().displayer(new RoundedBitmapDisplayer(20)).build();

    //
    @Override
    public void onCreate() {
        super.onCreate();
        SmackAndroid.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void createData(Context context) {
        msgManager = new MessageManager(context);
        qbm = new QuickBloxManager(context);
        fbm = new FaceBookManager();
        picManager = new PictureManager(context);
        rstManager = new RosterManager(context);

        contactsCandidateMap = new HashMap<String, QBUser>();
        contactsMap = new HashMap<String, QBUser>();

        dialogsUsersMap = new HashMap<String, QBUser>();
        dialogMap = new HashMap<String, QBCustomObject>();
        dialogList = new ArrayList<QBCustomObject>();
        userIdDialogIdMap = new HashMap<Integer, QBCustomObject>();

        inviteUserList = new ArrayList<String>();
        userNetStatusMap = new HashMap<Integer, String>();
    }


    public void clearAllData() {
        myPic = null;
        qbRoster = null;
        joinedRoom = null;

        qbUser = null;
        msgManager = null;
        qbm = null;
        fbm = null;
        picManager = null;
        rstManager = null;

        dialogList = null;
        dialogsUsersMap = null;
        dialogMap = null;


        contactsCandidateMap = null;
        contactsMap = null;
        inviteUserList = null;


        userPresentRoomList = null;
        userNetStatusMap = null;
        userIdDialogIdMap = null;

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

    public MessageManager getMsgManager() {
        return msgManager;
    }

    public ArrayList<QBCustomObject> getDialogList() {
        return dialogList;
    }

    public PictureManager getPicManager() {
        return picManager;
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

    public QBChatRoster getQbRoster() {
        return qbRoster;
    }

    public void setQbRoster(QBChatRoster qbRoster) {
        this.qbRoster = qbRoster;
    }

    public ArrayList<String> getInviteUserList() {
        return inviteUserList;
    }

    public HashMap<String, QBUser> getDialogsUsersMap() {
        return dialogsUsersMap;
    }

    public HashMap<String, QBUser> getContactsMap() {
        return contactsMap;
    }

    public DisplayImageOptions getOptions() {
        return options;
    }

    public FaceBookManager getFbm() {
        return fbm;
    }

    public String getAccessTokien() {
        return accessTokien;
    }

    public void setAccessTokien(String accessTokien) {
        this.accessTokien = accessTokien;
    }

    public HashMap<Integer, String> getUserNetStatusMap() {
        return userNetStatusMap;
    }

    public HashMap<Integer, QBCustomObject> getUserIdDialogIdMap() {
        return userIdDialogIdMap;
    }

    public QBChatRoom getJoinedRoom() {
        return joinedRoom;
    }

    public void setJoinedRoom(QBChatRoom joinedRoom) {
        this.joinedRoom = joinedRoom;
    }

    public HashMap<String, QBCustomObject> getDialogMap() {
        return dialogMap;
    }

    public HashMap<String, QBUser> getContactsCandidateMap() {
        return contactsCandidateMap;
    }
}
