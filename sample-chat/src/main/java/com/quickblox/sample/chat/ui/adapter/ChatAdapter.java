package com.quickblox.sample.chat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.sample.chat.utils.TimeUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

import vc908.stickerfactory.StickersManager;

public class ChatAdapter extends BaseAdapter {
    private List<QBChatMessage> chatMessages;
    private Context context;
    private LayoutInflater inflater;

    public ChatAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return chatMessages.size();
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return ChatItemType.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        String messageBody = getItem(position).getBody();
        return StickersManager.isSticker(messageBody)
                ? ChatItemType.STICKER.ordinal()
                : ChatItemType.MESSAGE.ordinal();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(QBChatMessage message) {
        chatMessages.add(message);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            if (getItemViewType(position) == ChatItemType.STICKER.ordinal()) {
                convertView = inflater.inflate(R.layout.list_item_message_sticker, parent, false);
            } else {
                convertView = inflater.inflate(R.layout.list_item_message_text, parent, false);
            }
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        QBChatMessage chatMessage = getItem(position);
        String messageBody = chatMessage.getBody();
        QBUser currentUser = ChatUtils.getCurrentUser();
        boolean isIncomingMessage = chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());

        setMessageAlignmentAndBackground(holder, isIncomingMessage);
        setMessageBody(holder, messageBody);
        setMessageInfo(chatMessage, holder);

        return convertView;
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        String messageDate = TimeUtils.millisToLongDHMS(chatMessage.getDateSent() * 1000);
        if (chatMessage.getSenderId() != null) {
            QBUser sender = ChatHelper.getInstance().getQbUserById(chatMessage.getSenderId());
            holder.messageInfoTextView.setText(String.format("%s\n%s", sender.getLogin(), messageDate));
        } else {
            holder.messageInfoTextView.setText(messageDate);
        }
    }

    private void setMessageBody(ViewHolder holder, String messageBody) {
        if (StickersManager.isSticker(messageBody)) {
            StickersManager.with(context)
                    .loadSticker(messageBody)
                    .into(holder.stickerImageView);
        } else if (holder.messageBodyTextView != null) {
            holder.messageBodyTextView.setText(messageBody);
        }
    }

    @SuppressLint("RtlHardcoded")
    private void setMessageAlignmentAndBackground(ViewHolder holder, boolean isIncoming) {
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        holder.messageInfoTextView.setGravity(gravity);
        if (holder.messageBodyTextView != null) {
            LinearLayout.LayoutParams messageLp = (LinearLayout.LayoutParams) holder.messageBodyTextView.getLayoutParams();
            messageLp.gravity = gravity;
            holder.messageBodyTextView.setLayoutParams(messageLp);
        }

        int textContainerBgResource = isIncoming
                ? R.drawable.incoming_message_bg
                : R.drawable.outgoing_message_bg;
        holder.textContainerLayout.setBackgroundResource(textContainerBgResource);
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();

        holder.messageBodyTextView = (TextView) v.findViewById(R.id.text_chat_message);
        holder.messageContainerLayout = (LinearLayout) v.findViewById(R.id.layout_chat_message_container);
        holder.textContainerLayout = (LinearLayout) v.findViewById(R.id.layout_chat_text_container);
        holder.messageInfoTextView = (TextView) v.findViewById(R.id.text_chat_info);
        holder.stickerImageView = (ImageView) v.findViewById(R.id.image_chat_sticker);

        return holder;
    }

    private enum ChatItemType {
        MESSAGE,
        STICKER
    }

    private static class ViewHolder {
        public TextView messageBodyTextView;
        public TextView messageInfoTextView;
        public LinearLayout messageContainerLayout;
        public LinearLayout textContainerLayout;
        public ImageView stickerImageView;
    }
}
