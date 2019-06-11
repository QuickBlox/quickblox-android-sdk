package com.quickblox.sample.chat.models;

import com.google.gson.annotations.SerializedName;

public class SampleConfigs {

    public SampleConfigs() {
    }

    @SerializedName("users_tag")
    private String usersTag;

    @SerializedName("users_password")
    private String usersPassword;

    @SerializedName("port")
    private int chatPort;

    @SerializedName("socket_timeout")
    private int chatSocketTimeout;

    @SerializedName("keep_alive")
    private boolean keepAlive;

    @SerializedName("use_tls")
    private boolean useTls;

    @SerializedName("auto_join")
    private boolean autoJoinEnabled;

    @SerializedName("auto_mark_delivered")
    private boolean autoMarkDelivered;

    @SerializedName("reconnection_allowed")
    private boolean reconnectionAllowed;

    @SerializedName("allow_listen_network")
    private boolean allowListenNetwork;

    public String getUsersTag() {
        return usersTag;
    }

    public void setUsersTag(String usersTag) {
        this.usersTag = usersTag;
    }

    public String getUsersPassword() {
        return usersPassword;
    }

    public void setUsersPassword(String usersPassword) {
        this.usersPassword = usersPassword;
    }

    public int getChatPort() {
        return chatPort;
    }

    public void setChatPort(int chatPort) {
        this.chatPort = chatPort;
    }

    public int getChatSocketTimeout() {
        return chatSocketTimeout;
    }

    public void setChatSocketTimeout(int chatSocketTimeout) {
        this.chatSocketTimeout = chatSocketTimeout;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public boolean isAutoJoinEnabled() {
        return autoJoinEnabled;
    }

    public void setAutoJoinEnabled(boolean autoJoinEnabled) {
        this.autoJoinEnabled = autoJoinEnabled;
    }

    public boolean isAutoMarkDelivered() {
        return autoMarkDelivered;
    }

    public void setAutoMarkDelivered(boolean autoMarkDelivered) {
        this.autoMarkDelivered = autoMarkDelivered;
    }

    public boolean isReconnectionAllowed() {
        return reconnectionAllowed;
    }

    public void setReconnectionAllowed(boolean reconnectionAllowed) {
        this.reconnectionAllowed = reconnectionAllowed;
    }

    public boolean isAllowListenNetwork() {
        return allowListenNetwork;
    }

    public void setAllowListenNetwork(boolean allowListenNetwork) {
        this.allowListenNetwork = allowListenNetwork;
    }
}
