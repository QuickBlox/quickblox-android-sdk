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

    protected static final int TYPE_OWN_TEXT_LAYOUT = 1;
    protected static final int TYPE_OPP_TEXT_LAYOUT = 2;
    protected static final int TYPE_OWN_ATTACH_LAYOUT = 3;
    protected static final int TYPE_OPP_ATTACH_LAYOUT = 4;

    SparseIntArray containerLayoutRes = new SparseIntArray(){
        {
            put(TYPE_OWN_TEXT_LAYOUT, R.layout.item_text_message_own_compound);
            put(TYPE_OPP_TEXT_LAYOUT, R.layout.item_text_message_opp_compound);
            put(TYPE_OWN_ATTACH_LAYOUT, R.layout.item_attachment_message_own);
            put(TYPE_OPP_ATTACH_LAYOUT, R.layout.item_attachment_message_opponent);
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
            case 1:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case 2:
                qbViewHolder = new TextMessageHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case 3:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;
            case 4:
                qbViewHolder = new ImageAttachHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.attach_imageview, R.id.centered_progressbar);
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

    public void setMsgLayoutResourceByType(int typeLayout, @LayoutRes int messageLayoutResource) {
        containerLayoutRes.put(typeLayout, messageLayoutResource);
    }

    @Override
    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        int valueType = getItemViewType(position);
        switch (valueType) {
            case 1:
                onBindViewMsgOwnHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case 2:
                onBindViewMsgOpponentHolder((TextMessageHolder) holder, chatMessage, position);
                break;
            case 3:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OWN");
                onBindViewAttachOwnHolder((ImageAttachHolder) holder, chatMessage, position);
                break;
            case 4:
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
     * ObtainAvatarUrl must be implement in derived class
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

//  если переопределить getItemId, то setHasStableIds(true) работает (может возвращать unique ID)?
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
                    return TYPE_OPP_ATTACH_LAYOUT;
                } else {
                    return TYPE_OWN_ATTACH_LAYOUT;
                }
            }

        } else {
            if (isIncoming(chatMessage)) {
                return TYPE_OPP_TEXT_LAYOUT;
            } else {
                return TYPE_OWN_TEXT_LAYOUT;
            }
        }
        return customViewType();
    }

    protected int customViewType(){
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

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }


    public String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds));
    }

    /**
     * displayAttachment must be implement in derived class
     */
    @Override
    public void displayAttachment(QBMessageViewHolder holder, int position) {
    }

    /**
     * displayAvatarImage must be implement in derived class
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
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview);
        }
    }
}