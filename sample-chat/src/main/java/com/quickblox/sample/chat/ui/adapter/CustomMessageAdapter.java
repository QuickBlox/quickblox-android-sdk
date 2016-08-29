package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }
    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {

//      setTextOwnText("Mine text");
        super.onBindViewHolder(holder, position);
    }

    @Override
    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
        holder.messageTextView.setText("Groovy");
        holder.timeTextMessageTextView.setText("time");
    }
}
