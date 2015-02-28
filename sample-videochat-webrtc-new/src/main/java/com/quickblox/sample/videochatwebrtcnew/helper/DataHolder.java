package com.quickblox.sample.videochatwebrtcnew.helper;

import com.quickblox.sample.videochatwebrtcnew.User;

import java.util.ArrayList;
import java.util.List;

public class DataHolder {

    public static ArrayList<User> usersList;


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
//
//
        usersList.add(new User(1, "androidUser1", "androidUser1", "androidUser1", 2327456));
        usersList.add(new User(2, "androidUser2", "androidUser2", "androidUser2",2344849));
//        usersList.add(new User(3, "user1", "@user1", "x6Bt0VDy5"));
//        usersList.add(new User(4, "user2", "@user2", "x6Bt0VDy5"));
//        usersList.add(new User(5, "user3", "@user3", "x6Bt0VDy5"));
//        usersList.add(new User(6, "user4", "@user4", "x6Bt0VDy5"));
//        usersList.add(new User(7, "user5", "@user5", "x6Bt0VDy5"));
//        usersList.add(new User(8, "user6", "@user6", "x6Bt0VDy5"));
//        usersList.add(new User(9, "user7", "@user7", "x6Bt0VDy5"));
//        usersList.add(new User(10, "user8", "@user8", "x6Bt0VDy5"));
//        usersList.add(new User(11, "user9", "@user9", "x6Bt0VDy5"));
//        usersList.add(new User(12, "user10", "@user10", "x6Bt0VDy5"));



        return usersList;

    }


}