package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.List;

public class CustomMsgAdapterSimple extends QBMessagesAdapter {
    public CustomMsgAdapterSimple(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);

    }
}
