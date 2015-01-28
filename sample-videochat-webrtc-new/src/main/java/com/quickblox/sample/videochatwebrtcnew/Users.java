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
