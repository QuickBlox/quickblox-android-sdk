package com.quickblox.sample.videochatwebrtcnew.holder;

import android.util.Log;

import com.quickblox.sample.videochatwebrtcnew.User;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    private static final String TAG = DataHolder.class.getSimpleName();
    public static ArrayList<User> usersList;
    public static final String PASSWORD = "x6Bt0VDy5";


    public void setUsersList(List<User> UsersList) {
        this.usersList = usersList;
    }

    public int getUserListSize() {
        return usersList.size();
    }

    public String getUserName(int index) {
        return usersList.get(index).getFullName();
    }

    public User getUser(int index) {
        return usersList.get(index);
    }

    public User getLastQBUser() {
        return usersList.get(usersList.size() - 1);
    }

    public void addUserToList(User user) {
        usersList.add(user);
    }

    public static ArrayList<User> createUsersList() {
        usersList = new ArrayList<>();




        usersList.add(new User(1, "User 1", "webrtc_user1", PASSWORD, 2436251));
        usersList.add(new User(2, "User 2", "webrtc_user2", PASSWORD, 2436254));
        usersList.add(new User(3, "User 3", "webrtc_user3", PASSWORD, 2436257));
        usersList.add(new User(4, "User 4", "webrtc_user4", PASSWORD, 2436258));
        usersList.add(new User(5, "User 5", "webrtc_user5", PASSWORD, 2436259));
        usersList.add(new User(6, "User 6", "webrtc_user6", PASSWORD, 2436262));
        usersList.add(new User(7, "User 7", "webrtc_user7", PASSWORD, 2436263));
        usersList.add(new User(8, "User 8", "webrtc_user8", PASSWORD, 2436265));
        usersList.add(new User(9, "User 9", "webrtc_user9", PASSWORD, 2436266));
        usersList.add(new User(10, "User 10", "webrtc_user10", PASSWORD, 2436269));
//        usersList.add(new User(11, "Kate 1_1", "Kate_1_1", PASSWORD, 3571691));
//        usersList.add(new User(12, "Kate 1_2", "Kate_1_2", PASSWORD, 3571751));
//        usersList.add(new User(13, "Kate 2_1", "Kate_2_1", PASSWORD, 3571763));
//        usersList.add(new User(14, "Kate 2_2", "Kate_2_2", PASSWORD, 3571768));
//        usersList.add(new User(15, "My majesty 1", "My_majesty_1", PASSWORD, 3571778));
//        usersList.add(new User(16, "My majesty 2", "My_majesty_2", PASSWORD, 3571784));
//        usersList.add(new User(17, "Padavan 1", "Padavan_1", PASSWORD, 3571791));
//        usersList.add(new User(18, "Padavan 2", "Padavan_2", PASSWORD, 3571794));


        return usersList;
    }


    public static String getUserNameByID(Integer callerID) {
//        Log.d(TAG, "callerID " + callerID);
        for (User user : usersList) {
//            Log.d(TAG, "getFullName " + user.getId());
            if (user.getId().equals(callerID)) {
                return user.getFullName();
            }
        }
        return "User_name_unused";
    }

    public static User getUserByID(Integer callerID) {
//        Log.d(TAG, "callerID " + callerID);
        for (User user : usersList) {
//            Log.d(TAG, "getFullName " + user.getId());
            if (user.getId().equals(callerID)) {
                return user;
            }
        }
        return null;
    }

    public static int getUserIndexByID(Integer callerID) {
//        Log.d(TAG, "callerID " + callerID);
        for (User user : usersList) {
//            Log.d(TAG, "getFullName " + user.getId());
            if (user.getId().equals(callerID)) {
                return usersList.indexOf(user);
            }
        }
        return -1;
    }

    public static int getUserIndexByFullName(String fullName) {
//        Log.d(TAG, "callerID " + fullName);
        for (User user : usersList) {
//            Log.d(TAG, "getFullName " + user.getFullName());
            if (user.getFullName().equals(fullName)) {
                return usersList.indexOf(user);
            }
        }
        return -1;
    }
}