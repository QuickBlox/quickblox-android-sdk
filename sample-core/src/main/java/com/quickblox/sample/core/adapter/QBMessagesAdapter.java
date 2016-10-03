package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.core.R;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QBMessagesAdapter extends RecyclerView.Adapter<QBMessagesAdapter.QBMessageViewHolder> implements QBBaseAdapter<QBChatMessage> {
    private static final String TAG = QBMessagesAdapter.class.getSimpleName();

    protected static final int TYPE_OWN_TEXT = 1;
    protected static final int TYPE_OPPONENT_TEXT = 2;
    protected static final int TYPE_OWN_ATTACH = 3;
    protected static final int TYPE_OPPONENT_ATTACH = 4;

    private SparseIntArray containerLayoutRes = new SparseIntArray() {
        {
            put(TYPE_OWN_TEXT, R.layout.widget_text_msg_right);
            put(TYPE_OPPONENT_TEXT, R.layout.widget_text_msg_left);
            put(TYPE_OWN_ATTACH, R.layout.widget_attach_msg_right);
            put(TYPE_OPPONENT_ATTACH, R.layout.widget_attach_msg_left);
        }
    };

    protected QBMessageViewHolder qbViewHolder;

    protected List<QBChatMessage> chatMessages;
    protected LayoutInflater inflater;
    protected Context context;


    public QBMessagesAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public QBMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_OWN_TEXT:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message, R.id.msg_text_time_message);
                return qbViewHolder;
            case TYPE_OPPONENT_TEXT:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_text_message, R.id.msg_text_time_message);
                return qbViewHolder;
            case TYPE_OWN_ATTACH:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach);
                return qbViewHolder;
            case TYPE_OPPONENT_ATTACH:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.msg_image_attach, R.id.msg_progressbar_attach);
                return qbViewHolder;

            default:
                Log.d(TAG, "onCreateViewHolder case default");
                // resource must be set manually by creating custom adapter
                return onCreateCustomViewHolder(parent, viewType);
        }
    }

    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.i(TAG, "You must create ViewHolder by your own");
        return null;
    }

    protected void setMsgLayoutResourceByType(int typeLayout, @LayoutRes int messageLayoutResource) {
        containerLayoutRes.put(typeLayout, messageLayoutResource);
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        int valueType = getItemViewType(position);
        switch (valueType) {
            case TYPE_OWN_TEXT:
                onBindViewMsgOwnHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_OPPONENT_TEXT:
                onBindViewMsgOpponentHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case TYPE_OWN_ATTACH:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OWN");
                onBindViewAttachOwnHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case TYPE_OPPONENT_ATTACH:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                onBindViewAttachOpponentHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            default:
                onBindViewCustomHolder(holder, chatMessage, position);
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
                break;
        }
    }

    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
    }

    protected void onBindViewAttachOwnHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        displayAttachment(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewAttachOpponentHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        displayAttachment(holder, position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgOpponentHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.timeTextMessageTextView.setText(getDate(chatMessage.getDateSent() * 1000));

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgOwnHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        holder.messageTextView.setText(chatMessage.getBody());
        holder.timeTextMessageTextView.setText(getDate(chatMessage.getDateSent() * 1000));

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    /**
     * ObtainAvatarUrl must be implemented in derived class
     *
     * @return String avatar url
     */
    @Nullable
    public String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
        return null;
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            Log.d("QBMessagesAdapter", "attachment.getType= " + attachment.getType());

            if (QBAttachment.PHOTO_TYPE.equals(attachment.getType())) {
                if (isIncoming(chatMessage)) {
                    return TYPE_OPPONENT_ATTACH;
                } else {
                    return TYPE_OWN_ATTACH;
                }
            }

        } else {
            if (isIncoming(chatMessage)) {
                return TYPE_OPPONENT_TEXT;
            } else {
                return TYPE_OWN_TEXT;
            }
        }
        return customViewType();
    }

    protected int customViewType() {
        return -1;
    }

    @Override
    public void add(QBChatMessage item) {
        chatMessages.add(item);
        notifyDataSetChanged();
    }

    @Override
    public List<QBChatMessage> getList() {
        return chatMessages;
    }

    @Override
    public void addList(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyDataSetChanged();
    }

    protected boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }


    protected String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    /**
     * displayAttachment must be implemented in derived class
     */
    protected void displayAttachment(QBMessageViewHolder holder, int position) {
    }

    /**
     * displayAvatarImage must be implemented in derived class
     */
    @Override
    public void displayAvatarImage(String uri, ImageView imageView) {
    }


    protected static class TextMessageHolder extends QBMessageViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;

        public TextMessageHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
        }
    }

    protected static class ImageAttachHolder extends QBMessageViewHolder {
        public ImageView attachImageView;
        public ProgressBar attachmentProgressBar;

        public ImageAttachHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attachImageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

    protected abstract static class QBMessageViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;

        public QBMessageViewHolder(View itemView) {
            super(itemView);
            avatar = (ImageView) itemView.findViewById(R.id.msg_image_avatar);
        }
    }
}