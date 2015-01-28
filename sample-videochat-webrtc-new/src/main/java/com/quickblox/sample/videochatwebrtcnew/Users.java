package com.quickblox.sample.videochatwebrtcnew;

import java.util.ArrayList;

public class Users {

    public  String userName;
    public  String login;
    public  String password;

    public Users (String userName, String login, String password) {
        setUserName(userName);
        setLogin(login);
        setPassword(password);
    }

    /*public static ArrayList<Users> createCollection() {
        ArrayList <Users> users = new ArrayList<>();
        users.add(new Users("User 1", "user_1", "11111111"));
        users.add(new Users("User 2", "user_2", "11111111"));
        users.add(new Users("User 3", "user_3", "11111111"));
        users.add(new Users("User 4", "user_4", "11111111"));
        users.add(new Users("User 5", "user_5", "11111111"));
        users.add(new Users("User 6", "user_6", "11111111"));
        users.add(new Users("User 7", "user_7", "11111111"));
        users.add(new Users("User 8", "user_8", "11111111"));
        users.add(new Users("User 9", "user_9", "11111111"));
        users.add(new Users("User 10", "user_10", "11111111"));
        return users;
    }*/

    public String getUserName() {
        return userName;
    }

    public String getLogin(){
        return login;
    }

    public String getPassword(){
        return password;
    }

    public void setUserName (String userName) {
        this.userName = userName;
    }
    public void setLogin (String login) {
        this.login = login;
    }
    public void setPassword (String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Users{" +
                "userName='" + userName + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
