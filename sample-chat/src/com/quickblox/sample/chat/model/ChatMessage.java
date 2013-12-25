package com.quickblox.sample.chat.model;

import java.util.Date;

public class ChatMessage {
    private boolean incoming;
    private String text;
    private Date time;

    public ChatMessage(String text, Date time, boolean incoming) {
        this.text = text;
        this.time = time;
        this.incoming = incoming;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public String getText() {
        return text;
    }

    public Date getTime() {
        return time;
    }
}
