package com.quickblox.sample.chat.ui.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.model.ChatMessage;

import java.util.Date;
import java.util.List;

public class ChatAdapter extends BaseAdapter {

    public static final String DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
    private final List<ChatMessage> chatMessages;
    private Context context;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        super();
        this.context = context;
        this.chatMessages = chatMessages;
    }

    @Override
    public int getCount() {
        if (chatMessages != null) {
            return chatMessages.size();
        } else {
            return 0;
        }
    }

    @Override
    public ChatMessage getItem(int position) {
        if (chatMessages != null) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage chatMessage = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = convertView;
        if (v == null) {
            if (chatMessage.isIncoming()) {
                v = vi.inflate(R.layout.list_item_message_incoming, null);
            } else {
                v = vi.inflate(R.layout.list_item_message_outgoing, null);
            }
            holder = createViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.txtMessage.setText(chatMessage.getText());
        holder.txtTime.setText(getTimeText(chatMessage.getTime()));

        return v;
    }

    public void add(ChatMessage message) {
        chatMessages.add(message);
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder;
        holder = new ViewHolder();
        holder.txtMessage = (TextView) v.findViewById(R.id.txtMessage);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        holder.contentWithBG = (LinearLayout) v.findViewById(R.id.contentWithBackground);
        holder.txtTime = (TextView) v.findViewById(R.id.txtTime);
        v.setTag(holder);
        return holder;
    }

    private String getTimeText(Date date) {
        return DateFormat.format(DATE_FORMAT, date).toString();
    }

    private static class ViewHolder {
        public TextView txtMessage;
        public TextView txtTime;
        public LinearLayout content;
        public LinearLayout contentWithBG;
    }
}
