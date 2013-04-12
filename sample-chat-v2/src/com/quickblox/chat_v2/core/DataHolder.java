package com.quickblox.chat_v2.core;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import com.quickblox.module.chat.smack.SmackAndroid;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: Andrew Dmitrenko
 * Date: 4/12/13
 * Time: 4:41 PM
 */
public class DataHolder extends Application {


    private static DataHolder dataHolder;


    public static synchronized DataHolder getInstance() {
        if (dataHolder == null) {
            dataHolder = new DataHolder();
        }
        return dataHolder;
    }


    private ArrayList<QBUser> chatUserList;
    private Bitmap myPic;
    private QBUser qbUser;


    //
    @Override
    public void onCreate() {
        Log.d("APPP", "OnCreate");
        super.onCreate();
        SmackAndroid.init(this);
    }


    // DATA

    public ArrayList<QBUser> getChatUserList() {
        return chatUserList;
    }

    public void setChatUserList(ArrayList<QBUser> chatUserList) {
        this.chatUserList = chatUserList;
    }

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
}
