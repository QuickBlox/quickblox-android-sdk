package com.quickblox.sample.videochatwebrtcnew;

import android.os.Parcel;
import android.os.Parcelable;

import com.quickblox.users.model.QBUser;

public class User extends QBUser{

    public int userNumber;
    //public  String userName;
    //public  String login;
    //public  String password;

    public User (int userNumber, String fullName, String login, String password) {
        super.fullName = fullName;
        super.login = login;
        super.password = password;
        this.userNumber = userNumber;

    }

    public int getUserNumber(){
        return userNumber;
    }

    public void setUserNumber (int userNumber) {
        this.userNumber = userNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        //if (!super.equals(o)) return false;

        User user = (User) o;

        if (userNumber != user.userNumber) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + userNumber;
        return result;
    }


    /*public String getUserName() {
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
    }*/
}
