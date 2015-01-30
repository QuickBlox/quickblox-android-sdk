package com.quickblox.sample.videochatwebrtcnew;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable{

    public  String userName;
    public  String login;
    public  String password;

    public User (String userName, String login, String password) {
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

    public int describeContents() {
        return 0;
    }

    // упаковываем объект в Parcel
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(userName);
        parcel.writeString(login);
        parcel.writeString(password);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        // распаковываем объект из Parcel
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private User(Parcel parcel) {
        userName = parcel.readString();
        login = parcel.readString();
        password = parcel.readString();
    }
}
