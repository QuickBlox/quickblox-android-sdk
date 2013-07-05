package com.quickblox.chat_v2.ui.activities.chat;

/**
 * Created by andrey on 05.07.13.
 */
public interface Chat {

    void sendMessage(String message);

    void release();

    void registerListeners();

    void unRegisterListeners();
}
