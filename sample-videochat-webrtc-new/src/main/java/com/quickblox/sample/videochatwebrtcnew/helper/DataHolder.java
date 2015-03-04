package com.quickblox.sample.videochatwebrtcnew.helper;

import android.util.Log;

import com.quickblox.sample.videochatwebrtcnew.User;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

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

    public static ArrayList<User> createUsersList (){
        usersList = new ArrayList<>();

//        usersList.add(new User(1, "User 1", "user_1", "11111111", 2224038));
//        usersList.add(new User(2, "User 2", "user_2", "11111111", 2224046));
//        usersList.add(new User(3, "User 3", "user_3", "11111111", 2224047));
//        usersList.add(new User(4, "User 4", "user_4", "11111111", 2224050));
//        usersList.add(new User(5, "User 5", "user_5", "11111111", 2224052));
//        usersList.add(new User(6, "User 6", "user_6", "11111111", 2224054));
//        usersList.add(new User(7, "User 7", "user_7", "11111111", 2224057));
//        usersList.add(new User(8, "User 8", "@dev8", "x6Bt0VDy5", 2224058));
//        usersList.add(new User(9, "User 9", "user_9", "11111111", 2224060));
//        usersList.add(new User(10, "User 10", "user_10", "11111111", 2224062));



        usersList.add(new User(1, "User 1", "webrtc_user1", PASSWORD, 2436251));
        usersList.add(new User(2, "User 2", "webrtc_user2", PASSWORD,2436254));
        usersList.add(new User(3, "User 3", "webrtc_user3", PASSWORD,2436257));
        usersList.add(new User(4, "User 4", "webrtc_user4", PASSWORD,2436258));
        usersList.add(new User(5, "User 5", "webrtc_user5", PASSWORD,2436259));
        usersList.add(new User(6, "User 6", "webrtc_user6", PASSWORD,2436262));
        usersList.add(new User(7, "User 7", "webrtc_user7", PASSWORD,2436263));
        usersList.add(new User(8, "User 8", "webrtc_user8", PASSWORD,2436265));
        usersList.add(new User(9, "User 9", "webrtc_user9", PASSWORD,2436266));
        usersList.add(new User(10, "User 10", "webrtc_user10", PASSWORD,2436269));

        return usersList;
    }


    public static String getUserNameByID(Integer callerID) {
        Log.d("Track", "callerID " + callerID);
        for (User user : usersList){
            Log.d("Track", "getFullName " + user.getId());
            if (user.getId().equals(callerID)){
                return user.getFullName();
            }
        }
        return "User_name_unused";
    }
}