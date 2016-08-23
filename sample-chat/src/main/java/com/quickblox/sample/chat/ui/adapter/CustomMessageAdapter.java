package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.ui.adapter.QBMessagesAdapter;
import com.quickblox.sample.chat.R;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    private String TAG = CustomMessageAdapter.class.getSimpleName();
    static int number = 0;
    int customMessageHeader = R.id.custom_message_textview_header;
    int customMessageFooter = R.id.custom_message_textview_footer;
    int customLayoutTextOwn = R.layout.item_custom_message_own;
    int customWidgetMessageOwn = R.layout.custom_widget_text_own;

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    @Override
    public QBMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        setWidgetLayoutResource(customWidgetMessageOwn);


//        setOwnMessageLayoutResource(customLayoutTextOwn);
        setMessageLayoutResourceByType(ViewTypes.TYPE_OWN_MESSAGE, customLayoutTextOwn);
        addCustomView(customMessageHeader, customMessageFooter);

        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        setCustomViewCustomLayout(holder);
    }

    private void setCustomTextViewOwn(ViewHolder holder) {
        if (holder.customLayout != null) {
            TextView customTextView = (TextView) holder.customLayout.findViewById(R.id.custom_text_view);
            if (customTextView != null) {
                customTextView.setText("number " + ++number);
            }
        }
    }

    private void setCustomViewCustomLayout(ViewHolder holder) {
        if (holder.listViewBind != null) {
            Log.d(TAG, "listViewBind.size " + holder.listViewBind.size());
            TextView customMessageHeader = (TextView) holder.listViewBind.get(0);

            if (customMessageHeader != null) {
                customMessageHeader.setText("i am custom title");
            }

            TextView customMessageFooter = holder.listViewBind.size() >= 2 ? (TextView) holder.listViewBind.get(1) : null;

            if (customMessageFooter != null) {
                customMessageFooter.setText("i am custom footer");
            }
        }
    }
}
