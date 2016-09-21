package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChat;
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

public class QBMessagesAdapter extends RecyclerView.Adapter<QBMessagesAdapter.QBMessagesAdapterViewHolder> implements QBBaseAdapter<QBChatMessage> {
    private static final String TAG = QBMessagesAdapter.class.getSimpleName();

    private int typeOwnAttachmentMessageLayoutResource = R.layout.item_attachment_message_own;
    private int typeOpponentAttachmentMessageLayoutResource = R.layout.item_attachment_message_opponent;
    private int typeOwnMessageLayoutResource = R.layout.item_text_message_own_compound;
    private int typeOpponentMessageLayoutResource = R.layout.item_text_message_opp_compound;

    private String textOwn;
    private String timeOwn;
    private String textOpp;
    private String timeOpp;

    private float textOwnSize;
    private int textOwnColor;

    private int preferredImageSizePreview = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
    private RequestListener glideRequestListener;
    private QBMessagesAdapterViewHolder qbViewHolder;

    protected enum ViewTypes {TYPE_OWN_MESSAGE, TYPE_OPPONENT_MESSAGE, TYPE_ATTACHMENT_MESSAGE_OWN, TYPE_ATTACHMENT_MESSAGE_OPPONENT, TYPE_ATTACHMENT_CUSTOM}

    protected List<QBChatMessage> chatMessages;
    protected LayoutInflater inflater;
    protected Context context;

    protected QBMessagesAdapterViewHolder customHolder;


    public QBMessagesAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public QBMessagesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewTypes valueType = ViewTypes.values()[viewType];
        switch (valueType) {
            case TYPE_OWN_MESSAGE:
                qbViewHolder = new MessageOwnHolder(inflater.inflate(typeOwnMessageLayoutResource, parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_OPPONENT_MESSAGE:
                qbViewHolder = new MessageOpponentHolder(inflater.inflate(typeOpponentMessageLayoutResource, parent, false), R.id.message_textview, R.id.time_text_message_textview);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                qbViewHolder = new AttachOwnHolder(inflater.inflate(typeOwnAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                qbViewHolder = new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);

            case TYPE_ATTACHMENT_CUSTOM:
                Log.d(TAG, "onCreateViewHolder case TYPE_ATTACHMENT_CUSTOM");
                // resource must be set manually by creating custom adapter
//                ToDo temporary stub потом customHolder будет создаваться в CustomAdapter на клиенте
                return customHolder = new AttachOpponentHolder(inflater.inflate(typeOpponentAttachmentMessageLayoutResource, parent, false), R.id.attach_imageview, R.id.centered_progressbar);
            default:
                return null;
        }
    }

    public void setOwnMessageLayoutResource(@LayoutRes int typeOwnMessageLayoutResource) {
        this.typeOwnMessageLayoutResource = typeOwnMessageLayoutResource;
    }

    public void setOpponentMessageLayoutResource(@LayoutRes int typeOpponentMessageLayoutResource) {
        this.typeOpponentMessageLayoutResource = typeOpponentMessageLayoutResource;
    }

    public void setOwnAttachmentMessageLayoutResource(@LayoutRes int typeOwnAttachmentMessageLayoutResource) {
        this.typeOwnAttachmentMessageLayoutResource = typeOwnAttachmentMessageLayoutResource;
    }

    public void setOpponentAttachmentMessageLayoutResource(@LayoutRes int typeOpponentAttachmentMessageLayoutResource) {
        this.typeOpponentAttachmentMessageLayoutResource = typeOpponentAttachmentMessageLayoutResource;
    }

    public void setMessageLayoutResourceByType(ViewTypes typeLayout, @LayoutRes int messageLayoutResource) {
        switch (typeLayout) {
            case TYPE_OWN_MESSAGE:
                typeOwnMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_OPPONENT_MESSAGE:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                typeOwnAttachmentMessageLayoutResource = messageLayoutResource;
                break;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                typeOpponentMessageLayoutResource = messageLayoutResource;
                break;
            default:
                break;
        }
    }

    @Override
    public void onBindViewHolder(QBMessagesAdapterViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        switch (valueType) {
            case TYPE_ATTACHMENT_MESSAGE_OWN:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                onBindViewAttachOwnHolder((AttachOwnHolder) holder, position);
                break;
            case TYPE_ATTACHMENT_MESSAGE_OPPONENT:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                onBindViewAttachOpponentHolder((AttachOpponentHolder) holder, position);
                break;
            case TYPE_OWN_MESSAGE:
                onBindViewMsgOwnHolder((MessageOwnHolder) holder, position);
                break;
            case TYPE_OPPONENT_MESSAGE:
                onBindViewMsgOpponentHolder((MessageOpponentHolder) holder, position);
                break;
            case TYPE_ATTACHMENT_CUSTOM:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
            default:
                break;
        }

    }

    protected void onBindViewAttachOwnHolder(AttachOwnHolder holder, int position) {
        showAttachment(holder, position);

        QBChatMessage chatMessage = getItem(position);

        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewAttachOpponentHolder(AttachOpponentHolder holder, int position) {
        showAttachment(holder, position);

        QBChatMessage chatMessage = getItem(position);

        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgOpponentHolder(MessageOpponentHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);

        holder.messageTextView.setText((textOpp == null) ? chatMessage.getBody() : textOpp);
        holder.timeTextMessageTextView.setText((timeOpp == null) ? getDate(chatMessage.getDateSent() * 1000) : timeOpp);

        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        if (textOwnSize != 0) {
            Log.d(TAG, "textOwnSize= " + textOwnSize);
            holder.messageTextView.setTextSize(textOwnSize);
        }
        if (textOwnColor != 0) {
            holder.messageTextView.setTextColor(textOwnColor);
        }

        holder.messageTextView.setText((textOwn == null) ? chatMessage.getBody() : textOwn);
        holder.timeTextMessageTextView.setText((timeOwn == null) ? getDate(chatMessage.getDateSent() * 1000) : timeOwn);


        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    @Nullable
    public String obtainAvatarUrl(ViewTypes valueType, QBChatMessage chatMessage) {
        return null;
    }


    // например можно сетить свой текст
    protected void setTextOwnText(String str) {
        textOwn = str;
    }

    protected void setTimeOwnText(String str) {
        timeOwn = str;
    }

    protected void setTextOpponentText(String str) {
        textOpp = str;
    }

    protected void setTimeOpponentText(String str) {
        timeOpp = str;
    }


    // например можно предопределить самые юзаемые методы
    protected void setTextOwnSize(float size) {
        textOwnSize = size;
    }

    protected void setTextOwnColor(int color) {
        textOwnColor = color;
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
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);

        if (hasAttachments(chatMessage)) {
            QBAttachment attachment = chatMessage.getAttachments().iterator().next();
            Log.d("QBMessagesAdapter", "attachment.getType= " + attachment.getType());

            if (QBAttachment.PHOTO_TYPE.equals(attachment.getType())) {
                if (isIncoming(chatMessage)) {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OPPONENT.ordinal();
                } else {
                    return ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN.ordinal();
                }
            } else {
                return ViewTypes.TYPE_ATTACHMENT_CUSTOM.ordinal();
            }

        } else {
            if (isIncoming(chatMessage)) {
                return ViewTypes.TYPE_OPPONENT_MESSAGE.ordinal();
            } else {
                return ViewTypes.TYPE_OWN_MESSAGE.ordinal();
            }
        }
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

    @Override
    public void showAttachment(QBMessagesAdapterViewHolder holder, int position) {
        ViewTypes valueType = ViewTypes.values()[getItemViewType(position)];
        Log.d(TAG, "showAttachment valueType= " + valueType);
        initGlideRequestListener((QBAttachViewHolder) holder, valueType);

        QBChatMessage chatMessage = getItem(position);

        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        Glide.with(context)
                .load(attachment.getUrl())
                .listener(glideRequestListener)
                .override(preferredImageSizePreview, preferredImageSizePreview)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into((valueType == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? ((AttachOwnHolder) holder).attach_imageView : ((AttachOpponentHolder) holder).attach_imageView);
    }


    private void initGlideRequestListener(final QBAttachViewHolder holder, final ViewTypes type) {
        glideRequestListener = new RequestListener() {
            QBAttachViewHolder viewHolder = (type == ViewTypes.TYPE_ATTACHMENT_MESSAGE_OWN) ? (AttachOwnHolder) holder : (AttachOpponentHolder) holder;

            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                e.printStackTrace();
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                viewHolder.attach_imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                viewHolder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }
        };
    }

    @Override
    public void displayAvatarImage(String uri, ImageView imageView) {
        int prefSize = (int) (20 * Resources.getSystem().getDisplayMetrics().density);
        Glide.with(context)
                .load(uri)
                .listener(null)
                .override(prefSize, prefSize)
                .dontTransform()
                .error(R.drawable.ic_error)
                .into(imageView);
    }


    protected static class MessageOwnHolder extends QBMessagesAdapterViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;

        public MessageOwnHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview_right);
        }
    }

    protected static class MessageOpponentHolder extends QBMessagesAdapterViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;

        public MessageOpponentHolder(View itemView, @IdRes int msgId, @IdRes int timeId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview_left);
        }
    }

    protected static class AttachOwnHolder extends QBAttachViewHolder {

        public AttachOwnHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview);
        }
    }

    protected static class AttachOpponentHolder extends QBAttachViewHolder {

        public AttachOpponentHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach_imageView = (ImageView) itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview);
        }
    }

    protected abstract static class QBAttachViewHolder extends QBMessagesAdapterViewHolder {
        public ImageView attach_imageView;
        public ProgressBar attachmentProgressBar;

        public QBAttachViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected abstract static class QBMessagesAdapterViewHolder extends RecyclerView.ViewHolder {
        public ImageView avatar;

        public QBMessagesAdapterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
