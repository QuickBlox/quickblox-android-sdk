package com.quickblox.sample.groupchatwebrtc.holder;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * QuickBlox team
 */
public class DataHolder {

    public static ArrayList<QBUser> usersList;

    public static ArrayList<QBUser> getUsersList() {
        return usersList;
    }

    public static void  setUsersList(ArrayList<QBUser> newUsersList){
        usersList = newUsersList;
    }

    public static String getUserNameByID(Integer callerID) {
        if (getUsersList()!= null) {   //TODO VT падает при бекграунд звонках
            for (QBUser user : getUsersList()) {
                if (user.getId().equals(callerID)) {
                    return user.getFullName();
                }
            }
        }
        return callerID.toString();
    }

    public static int getUserIndexByID(Integer callerID) {
        if (getUsersList()!= null) {   //TODO VT падает при бекграунд звонках
            for (QBUser user : getUsersList()) {
                if (user.getId().equals(callerID)) {
                    return usersList.indexOf(user);
                }
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

    public static ArrayList<QBUser> getUsersListWithoutSelectedUser(QBUser qbUser){
        ArrayList<QBUser> opponentsWithoutSelectedUser = getUsersList();
        opponentsWithoutSelectedUser.remove(qbUser);
        return opponentsWithoutSelectedUser;
    }

    public static ArrayList<QBUser> getQBUsersByIds(List<Integer> usersIds){
        ArrayList<QBUser> result = new ArrayList<>();
        for (Integer userId: usersIds){
            for (QBUser qbUser : getUsersList()){
                if (userId.equals(qbUser.getId())){
                    result.add(qbUser);
                } else {
                    QBUser newUser = new QBUser(userId);
                    newUser.setFullName(String.valueOf(userId));
                    result.add(newUser);
                }
            }
        }

        return result;
    }
}