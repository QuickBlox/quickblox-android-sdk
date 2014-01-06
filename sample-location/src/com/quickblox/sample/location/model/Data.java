package com.quickblox.sample.location.model;

/**
 * Date: 1.11.12
 * Time: 12:16
 */

/**
 * Custom map marker
 *
 * @author <a href="mailto:igor@quickblox.com">Igor Khomenko</a>
 */

public class Data {

    private String userName;
    private String userStatus;

    public Data(String userName, String userStatus) {
        this.userName = userName;
        this.userStatus = userStatus;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }
}