package com.quickblox.sample.chat.model;

import java.util.Date;

public class ChatMessage {
    private boolean incoming;
    private String text;
    private Date time;
    private String sender;

    public ChatMessage(String text, Date time, boolean incoming) {
        this(text, null, time, incoming);
    }

    public ChatMessage(String text, String sender, Date time, boolean incoming) {
        this.text = text;
        this.sender = sender;
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

    public String getSender() {
        return sender;
    }
}
