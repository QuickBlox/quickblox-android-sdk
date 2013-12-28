package com.quickblox.sample.chat.core;

import org.jivesoftware.smack.XMPPException;

public interface Chat {
    void sendMessage(String message) throws XMPPException;

    void release() throws XMPPException;
}
