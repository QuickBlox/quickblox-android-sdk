package com.quickblox.sample.chat.core;

public interface Chat {
    void sendMessage(String message);

    void release();
}
