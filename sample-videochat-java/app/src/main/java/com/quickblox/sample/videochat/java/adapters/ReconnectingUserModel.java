package com.quickblox.sample.videochat.java.adapters;

import com.quickblox.users.model.QBUser;

public class ReconnectingUserModel {
    private QBUser user;
    private String reconnectingState;

    public ReconnectingUserModel(QBUser user, String reconnectingState) {
        this.user = user;
        this.reconnectingState = reconnectingState;
    }

    public QBUser getUser() {
        return user;
    }

    public String getReconnectingState() {
        return reconnectingState;
    }

    public void setUser(QBUser user) {
        this.user = user;
    }

    public void setReconnectingState(String reconnectingState) {
        this.reconnectingState = reconnectingState;
    }
}
