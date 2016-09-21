package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    private static final String TAG = CustomMessageAdapter.class.getSimpleName();

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    @Override
    public void onBindViewHolder(QBMessagesAdapterViewHolder holder, int position) {

      setOwnMessageLayoutResource(R.layout.item_text_message_own);
//      setMessageLayoutResourceByType(ViewTypes.TYPE_OWN_MESSAGE, R.layout.item_text_message_own);

//      setTextOwnText("Mine text");

        setTextOwnSize(16);
        setTextOwnColor(Color.BLUE);
        super.onBindViewHolder(holder, position);
    }

//    @Override
//    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
//        holder.messageTextView.setText("Groovy");
//        holder.timeTextMessageTextView.setText("time");
//    }
}
