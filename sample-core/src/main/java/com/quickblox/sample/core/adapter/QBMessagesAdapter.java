package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.content.res.Resources;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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

    protected static final int TYPE_OWN_MESG_LAYOUT = 1;
    protected static final int TYPE_OPP_MESG_LAYOUT = 2;
    protected static final int TYPE_OWN_ATTACH_LAYOUT = 3;
    protected static final int TYPE_OPP_ATTACH_LAYOUT = 4;

    SparseIntArray containerLayoutRes = new SparseIntArray(){
        {
            put(TYPE_OWN_MESG_LAYOUT, R.layout.item_text_message_own_compound);
            put(TYPE_OPP_MESG_LAYOUT, R.layout.item_text_message_opp_compound);
            put(TYPE_OWN_ATTACH_LAYOUT, R.layout.item_attachment_message_own);
            put(TYPE_OPP_ATTACH_LAYOUT, R.layout.item_attachment_message_opponent);
        }
    };

    private int widgetOwnId;
    private int widgetOppId;

    private String textOwn;
    private String timeOwn;
    private String textOpp;
    private String timeOpp;

    private float textOwnSize;
    private int textOwnColor;

    private int preferredImageSizePreview = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
    private RequestListener glideRequestListener;
    protected QBMessagesAdapterViewHolder qbViewHolder;

    protected List<QBChatMessage> chatMessages;
    protected LayoutInflater inflater;
    protected Context context;


    public QBMessagesAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public QBMessagesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1:
                qbViewHolder = new MessageOwnHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.message_textview, R.id.time_text_message_textview, widgetOwnId);
                return qbViewHolder;
            case 2:
                qbViewHolder = new MessageOpponentHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.message_textview, R.id.time_text_message_textview, widgetOppId);
                return qbViewHolder;
            case 3:
                qbViewHolder = new AttachOwnHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;
            case 4:
                qbViewHolder = new AttachOpponentHolder(inflater.inflate(containerLayoutRes.get(viewType), parent, false), R.id.attach_imageview, R.id.centered_progressbar);
                return qbViewHolder;

            default:
                Log.d(TAG, "onCreateViewHolder case default");
                // resource must be set manually by creating custom adapter
                return qbViewHolder;
        }
    }

    public void setWidgetOwnId(@IdRes int widgetOwnId) {
        this.widgetOwnId = widgetOwnId;
    }

    public void setWidgetOppId(@IdRes int widgetOppId) {
        this.widgetOppId = widgetOppId;
    }

    public void setMessageLayoutResourceByType(int typeLayout, @LayoutRes int messageLayoutResource) {
        containerLayoutRes.put(typeLayout, messageLayoutResource);
    }

    @Override
    public void onBindViewHolder(QBMessagesAdapterViewHolder holder, int position) {
        int valueType = getItemViewType(position);
        switch (valueType) {
            case 1:
                onBindViewMsgOwnHolder((MessageOwnHolder) holder, position);
                break;
            case 2:
                onBindViewMsgOpponentHolder((MessageOpponentHolder) holder, position);
                break;
            case 3:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OWN");
                onBindViewAttachOwnHolder((AttachOwnHolder) holder, position);
                break;
            case 4:
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_MESSAGE_OPPONENT");
                onBindViewAttachOpponentHolder((AttachOpponentHolder) holder, position);
                break;
            default:
                onBindViewCustomHolder(holder, position);
                Log.i(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
                break;
        }
    }

    protected void onBindViewCustomHolder(QBMessagesAdapterViewHolder holder, int position) {
    }

    protected void onBindViewAttachOwnHolder(AttachOwnHolder holder, int position) {
        showAttachment(holder, position);

        QBChatMessage chatMessage = getItem(position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewAttachOpponentHolder(AttachOpponentHolder holder, int position) {
        showAttachment(holder, position);

        QBChatMessage chatMessage = getItem(position);

        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    protected void onBindViewMsgOpponentHolder(MessageOpponentHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);

//        ((EditMessageTextView)holder.itemView).setText("sdsd");
        holder.messageTextView.setText((textOpp == null) ? chatMessage.getBody() : textOpp);
        holder.timeTextMessageTextView.setText((timeOpp == null) ? getDate(chatMessage.getDateSent() * 1000) : timeOpp);

        int valueType = getItemViewType(position);
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


        int valueType = getItemViewType(position);
        String avatarUrl = obtainAvatarUrl(valueType, chatMessage);
        if (avatarUrl != null) {
            displayAvatarImage(avatarUrl, holder.avatar);
        }
    }

    /**
     * ObtainAvatarUrl must be set manually
     *
     * @return String avatar url
     */
    @Nullable
    public String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
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
                    return TYPE_OPP_ATTACH_LAYOUT;
                } else {
                    return TYPE_OWN_ATTACH_LAYOUT;
                }
            }

        } else {
            if (isIncoming(chatMessage)) {
                return TYPE_OPP_MESG_LAYOUT;
            } else {
                return TYPE_OWN_MESG_LAYOUT;
            }
        }
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

    @Override
    public void showAttachment(QBMessagesAdapterViewHolder holder, int position) {
        int valueType = getItemViewType(position);
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
                .into((valueType == TYPE_OWN_ATTACH_LAYOUT) ? ((AttachOwnHolder) holder).attach_imageView : ((AttachOpponentHolder) holder).attach_imageView);
    }


    private void initGlideRequestListener(final QBAttachViewHolder holder, final int type) {
        glideRequestListener = new RequestListener() {
            QBAttachViewHolder viewHolder = (type == TYPE_OWN_ATTACH_LAYOUT) ? (AttachOwnHolder) holder : (AttachOpponentHolder) holder;

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
        public View widgetOwn;

        public MessageOwnHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int widgetId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview_right);
            if (widgetId != 0) {
                widgetOwn = itemView.findViewById(widgetId);
            }
        }
    }

    protected static class MessageOpponentHolder extends QBMessagesAdapterViewHolder {
        public TextView messageTextView;
        public TextView timeTextMessageTextView;
        public View widgetOpp;

        public MessageOpponentHolder(View itemView, @IdRes int msgId, @IdRes int timeId, @IdRes int widgetId) {
            super(itemView);
            messageTextView = (TextView) itemView.findViewById(msgId);
            timeTextMessageTextView = (TextView) itemView.findViewById(timeId);
            avatar = (ImageView) itemView.findViewById(R.id.avatar_imageview_left);
            if (widgetId != 0) {
                widgetOpp = itemView.findViewById(widgetId);
            }
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
