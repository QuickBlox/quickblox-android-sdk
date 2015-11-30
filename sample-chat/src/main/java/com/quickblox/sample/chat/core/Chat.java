package com.quickblox.sample.chat.core;

import com.quickblox.chat.model.QBChatMessage;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public interface Chat {

    void sendMessage(QBChatMessage message) throws XMPPException, SmackException.NotConnectedException;

    void release() throws XMPPException;
}
