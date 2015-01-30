package com.quickblox.sample.videochatwebrtcnew;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by tereha on 30.01.15.
 */
public class Opponent {

    public int opponentNumber;
    public  String opponentName;
    public  String opponentLogin;
    public  String opponentPassword;

    public Opponent (int opponentNumber, String opponentName, String opponentLogin, String opponentPassword) {
        setOpponentNumber(opponentNumber);
        setOpponentName(opponentName);
        setOpponentLogin(opponentLogin);
        setOpponentPassword(opponentPassword);
    }

    public int getOpponentNumber() {
        return opponentNumber;
    }
    public String getOpponentName(){
        return opponentName;
    }
    public String getOpponentLogin(){
        return opponentLogin;
    }
    public String getOpponentPassword() {
        return opponentPassword;
    }
    public void setOpponentNumber (int opponentNumber) {
        this.opponentNumber = opponentNumber;
    }
    public void setOpponentName(String opponentName){
        this.opponentName = opponentName;
    }
    public void setOpponentLogin (String opponentLogin) {
        this.opponentLogin = opponentLogin;
    }
    public void setOpponentPassword (String opponentPassword) {
        this.opponentPassword = opponentPassword;
    }
}
