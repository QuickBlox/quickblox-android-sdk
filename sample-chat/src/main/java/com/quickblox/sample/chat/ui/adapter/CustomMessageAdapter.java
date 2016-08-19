package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.qblist.QBMessagesAdapter;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    TextView customMessage;
    int customMessageID = R.id.custom_message_textview;

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    @Override
    public QBMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        setTypeOwnMessageLayoutResource(R.layout.item_custom_message_own);
        addCustomView(customMessageID);
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        QBChatMessage chatMessage = getItem(position);
        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        Log.d("CustomMessageAdapter","valueType= "+valueType);
        switch (valueType) {
            case TYPE_OWN_MESSAGE:
                customMessage = (TextView) holder.listViewBinded.get(0);
                customMessage.setText("custom view!");
                break;
            default:
                break;
        }
    }
}
