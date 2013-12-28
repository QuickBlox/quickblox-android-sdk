package com.quickblox.sample.chat.core;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.ChatMessageListener;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.sample.chat.model.ChatMessage;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.util.Calendar;

public class SingleChat implements Chat, ChatMessageListener {

    public static final String EXTRA_USER_ID = "user_id";
    private ChatActivity chatActivity;
    private QBPrivateChat chat;
    private int companionId;

    public SingleChat(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
        companionId = chatActivity.getIntent().getIntExtra(EXTRA_USER_ID, 0);
        chat = QBChatService.getInstance().createChat();
        chat.addChatMessageListener(this);
    }

    @Override
    public void sendMessage(String message) throws XMPPException {
        chat.sendMessage(companionId, message);
    }

    @Override
    public void release() {
        chat.removeChatMessageListener(this);
    }

    @Override
    public void processMessage(Message message) {
        final String messageBody = message.getBody();
        // Show message
        chatActivity.showMessage(new ChatMessage(messageBody, Calendar.getInstance().getTime(), true));
    }

    @Override
    public boolean accept(Message.Type messageType) {
        switch (messageType) {
            case chat:
                return true;
            default:
                return false;
        }
    }
}
