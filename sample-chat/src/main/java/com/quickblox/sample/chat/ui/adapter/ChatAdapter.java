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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.TimeUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.model.QBUser;

import java.util.List;

import vc908.stickerfactory.StickersManager;

public class ChatAdapter extends BaseAdapter {

    private final List<QBChatMessage> chatMessages;
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
        QBUser currentUser = ChatHelper.getInstance().getCurrentUser();
        boolean isOutgoingMessage = chatMessage.getSenderId() == null || chatMessage.getSenderId().equals(currentUser.getId());
        String messageBody = chatMessage.getBody();

        setMessageBubbleAlignment(holder, isOutgoingMessage);
        setMessageBody(holder, messageBody);
        setMessageInfo(chatMessage, holder);

        return convertView;
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        String messageDate = TimeUtils.millisToLongDHMS(chatMessage.getDateSent() * 1000);
        if (chatMessage.getSenderId() != null) {
            holder.messageInfoTextView.setText(String.format("%s: %s", chatMessage.getSenderId(), messageDate));
        } else {
            holder.messageInfoTextView.setText(messageDate);
        }
    }

    private void setMessageBody(ViewHolder holder, String messageBody) {
        if (StickersManager.isSticker(messageBody)) {
            StickersManager.with(context)
                    .loadSticker(messageBody)
                    .setPlaceholderColorFilterRes(android.R.color.darker_gray)
                    .into(holder.stickerImageView);
        } else if (holder.messageTextView != null) {
            holder.messageTextView.setText(messageBody);
        }
    }

    @SuppressLint("RtlHardcoded")
    private void setMessageBubbleAlignment(ViewHolder holder, boolean isOutgoing) {
        LinearLayout.LayoutParams messageContainerLp = (LinearLayout.LayoutParams) holder.messageContainerLayout.getLayoutParams();
        RelativeLayout.LayoutParams mainContainerLp = (RelativeLayout.LayoutParams) holder.mainContainerLayout.getLayoutParams();
        LinearLayout.LayoutParams infoLayoutParams = (LinearLayout.LayoutParams) holder.messageInfoTextView.getLayoutParams();
        int messageContainerBgResource = 0;
        int messageGravity = 0;

        if (isOutgoing) {
            messageContainerLp.gravity = Gravity.LEFT;

            mainContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
            mainContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            infoLayoutParams.gravity = Gravity.LEFT;
            if (holder.messageTextView != null) {
                messageContainerBgResource = R.drawable.outgoing_message_bg;
                messageGravity = Gravity.LEFT;
            }
        } else {
            messageContainerLp.gravity = Gravity.RIGHT;

            mainContainerLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
            mainContainerLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            infoLayoutParams.gravity = Gravity.RIGHT;
            if (holder.messageTextView != null) {
                messageContainerBgResource = R.drawable.incoming_message_bg;
                messageGravity = Gravity.RIGHT;
            }
        }

        holder.messageContainerLayout.setLayoutParams(messageContainerLp);
        holder.mainContainerLayout.setLayoutParams(mainContainerLp);
        holder.messageInfoTextView.setLayoutParams(infoLayoutParams);

        if (holder.messageTextView != null) {
            LinearLayout.LayoutParams messageLp = (LinearLayout.LayoutParams) holder.messageTextView.getLayoutParams();
            messageLp.gravity = messageGravity;
            holder.messageTextView.setLayoutParams(messageLp);
        }
        holder.messageContainerLayout.setBackgroundResource(messageContainerBgResource);
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();

        holder.messageTextView = (TextView) v.findViewById(R.id.text_chat_message);
        holder.mainContainerLayout = (LinearLayout) v.findViewById(R.id.container_chat_content);
        holder.messageContainerLayout = (LinearLayout) v.findViewById(R.id.container_chat_bubble);
        holder.messageInfoTextView = (TextView) v.findViewById(R.id.text_chat_info);
        holder.stickerImageView = (ImageView) v.findViewById(R.id.image_chat_sticker);

        return holder;
    }

    private enum ChatItemType {
        MESSAGE,
        STICKER
    }

    private static class ViewHolder {
        public TextView messageTextView;
        public TextView messageInfoTextView;
        public LinearLayout mainContainerLayout;
        public LinearLayout messageContainerLayout;
        public ImageView stickerImageView;
    }
}
