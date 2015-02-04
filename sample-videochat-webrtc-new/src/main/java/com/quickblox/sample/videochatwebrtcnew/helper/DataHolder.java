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

        usersList.add(new User(1, "User 1", "user_1", "11111111"));
        usersList.add(new User(2, "User 2", "user_2", "11111111"));
        usersList.add(new User(3, "User 3", "user_3", "11111111"));
        usersList.add(new User(4, "User 4", "user_4", "11111111"));
        usersList.add(new User(5, "User 5", "user_5", "11111111"));
        usersList.add(new User(6, "User 6", "user_6", "11111111"));
        usersList.add(new User(7, "User 7", "user_7", "11111111"));
        usersList.add(new User(8, "User 8", "user_8", "11111111"));
        usersList.add(new User(9, "User 9", "user_9", "11111111"));
        usersList.add(new User(10, "User 10", "user_10", "11111111"));

        return usersList;

    }


}