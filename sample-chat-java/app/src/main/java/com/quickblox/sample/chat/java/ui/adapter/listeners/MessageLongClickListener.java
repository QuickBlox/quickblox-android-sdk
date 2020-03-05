package com.quickblox.sample.chat.java.ui.adapter.listeners;

import android.view.View;

import com.quickblox.chat.model.QBChatMessage;

public interface MessageLongClickListener {
    void onMessageLongClicked (int itemViewType, View view, QBChatMessage qbChatMessage);
}
