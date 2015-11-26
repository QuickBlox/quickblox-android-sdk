package com.quickblox.sample.groupchatwebrtc.holder;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class DataHolder {

    public static ArrayList<QBUser> usersList;
    public static final String PASSWORD = "x6Bt0VDy5";

    private static QBUser currentUser;

    public static QBUser getLoggedUser() {
        return currentUser;
    }

    public static void setLoggedUser(QBUser currentUser) {
        DataHolder.currentUser = currentUser;
    }

    public static ArrayList<QBUser> getUsersList() {

        if (usersList == null) {
            usersList = new ArrayList<>();

            QBUser user = new QBUser("webrtc_user1", PASSWORD);
            user.setId(2436251);
            user.setFullName("User 1");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user2", PASSWORD);
            user.setId(2436254);
            user.setFullName("User 2");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user3", PASSWORD);
            user.setId(2436257);
            user.setFullName("User 3");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user4", PASSWORD);
            user.setId(2436258);
            user.setFullName("User 4");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user5", PASSWORD);
            user.setId(2436259);
            user.setFullName("User 5");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user6", PASSWORD);
            user.setId(2436262);
            user.setFullName("User 6");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user7", PASSWORD);
            user.setId(2436263);
            user.setFullName("User 7");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user8", PASSWORD);
            user.setId(2436265);
            user.setFullName("User 8");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user9", PASSWORD);
            user.setId(2436266);
            user.setFullName("User 9");
            usersList.add(user);
            //
            user = new QBUser("webrtc_user10", PASSWORD);
            user.setId(2436269);
            user.setFullName("User 10");
            usersList.add(user);
        }

        return usersList;
    }

    public static ArrayList<QBUser> createUsersList(List<QBUser> users) {
        usersList = new ArrayList<>();

        for(QBUser user : users){
            QBUser newUser = new QBUser(user.getLogin(), PASSWORD);
            newUser.setId(user.getId());
            newUser.setFullName(user.getFullName());
            usersList.add(newUser);
        }
        return usersList;
    }


    public static String getUserNameByID(Integer callerID) {
        for (QBUser user : getUsersList()) {
            if (user.getId().equals(callerID)) {
                return user.getFullName();
            }
        }
        return callerID.toString();
    }

    public static int getUserIndexByID(Integer callerID) {
        for (QBUser user : getUsersList()) {
            if (user.getId().equals(callerID)) {
                return usersList.indexOf(user);
            }
        }
        return -1;
    }

    public static List<QBUser> getUsers(){
        return usersList;
    }

    public static int getUserIndexByFullName(String fullName) {
        for (QBUser user : usersList) {
            if (user.getFullName().equals(fullName)) {
                return usersList.indexOf(user);
            }
        }
        return -1;
    }

    public static ArrayList<QBUser> getUsersByIDs(Integer... ids) {
        ArrayList<QBUser> result = new ArrayList<>();
        for (Integer userId : ids) {
            for (QBUser user : usersList) {
                if (userId.equals(user.getId())){
                    result.add(user);
                }
            }
        }
        return result;
    }
}