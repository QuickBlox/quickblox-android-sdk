package com.quickblox.sample.core.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class QbConfigs implements Serializable{

    public QbConfigs() {
    }

    @SerializedName("app_id")
    private String appId;

    @SerializedName("auth_key")
    private String authKey;

    @SerializedName("auth_secret")
    private String authSecret;

    @SerializedName("account_key")
    private String accountKey;

    @SerializedName("api_domain")
    private String apiDomain;

    @SerializedName("chat_domain")
    private String chatDomain;


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getAuthSecret() {
        return authSecret;
    }

    public void setAuthSecret(String authSecret) {
        this.authSecret = authSecret;
    }

    public String getAccountKey() {
        return accountKey;
    }

    public void setAccountKey(String accountKey) {
        this.accountKey = accountKey;
    }

    public String getApiDomain() {
        return apiDomain;
    }

    public void setApiDomain(String apiDomain) {
        this.apiDomain = apiDomain;
    }

    public String getChatDomain() {
        return chatDomain;
    }

    public void setChatDomain(String chatDomain) {
        this.chatDomain = chatDomain;
    }
}
