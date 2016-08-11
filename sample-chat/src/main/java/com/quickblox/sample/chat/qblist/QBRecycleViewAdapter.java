package com.quickblox.sample.chat.qblist;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.users.model.QBUser;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class QBRecycleViewAdapter extends RecyclerView.Adapter<QBRecycleViewAdapter.ViewHolder> implements QBBaseAdapter<QBChatMessage> {
    static int PREFERRED_IMAGE_SIZE_PREVIEW = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
    protected LayoutInflater inflater;
    protected Context context;
    protected List<QBChatMessage> chatMessages;

    public QBRecycleViewAdapter(Context context, List<QBChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.list_item_chat_message, null);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {


        final QBChatMessage chatMessage = getItem(position);

        setIncomingOrOutgoingMessageAttributes(holder, chatMessage);
        setMessageBody(holder, chatMessage);
        setMessageInfo(chatMessage, holder);
        setMessageAuthor(holder, chatMessage);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public Integer getPosition(QBChatMessage item) {
        return null;
    }

    @Override
    public Integer getPosition(QBChatMessage item, int lastPosition) {
        return null;
    }

    @Override
    public QBChatMessage getItem(int position) {
        return chatMessages.get(position);
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

    @Override
    public QBChatMessage getItem(RecyclerView.ViewHolder viewHolder) {
        return null;
    }

    private void setIncomingOrOutgoingMessageAttributes(ViewHolder holder, QBChatMessage chatMessage) {
        boolean isIncoming = isIncoming(chatMessage);
        int gravity = isIncoming ? Gravity.LEFT : Gravity.RIGHT;
        holder.messageContainerLayout.setGravity(gravity);
        holder.messageInfoTextView.setGravity(gravity);

        //draw bubbles
        int messageBodyContainerBgResource = isIncoming
                ? R.drawable.incoming_message_bg
                : R.drawable.outgoing_message_bg;
        if (hasAttachments(chatMessage)) {
            holder.messageBodyContainerLayout.setBackgroundResource(0);
            holder.messageBodyContainerLayout.setPadding(0, 0, 0, 0);
            holder.attachmentImageView.getResources().getDrawable(messageBodyContainerBgResource);
        } else {
            holder.messageBodyContainerLayout.setBackgroundResource(messageBodyContainerBgResource);
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.messageAuthorTextView.getLayoutParams();
        if (isIncoming && hasAttachments(chatMessage)) {
            lp.leftMargin = (int) context.getResources().getDimension(R.dimen.chat_message_attachment_username_margin);
            lp.topMargin = (int) context.getResources().getDimension(R.dimen.chat_message_attachment_username_margin);
        } else if (isIncoming) {
            lp.leftMargin =(int) context.getResources().getDimension(R.dimen.chat_message_username_margin);
            lp.topMargin = 0;
        }
        holder.messageAuthorTextView.setLayoutParams(lp);

        int textColorResource = isIncoming
                ? R.color.text_color_black
                : R.color.text_color_white;
        holder.messageBodyTextView.setTextColor(context.getResources().getColor(textColorResource));
    }

    private void setMessageBody(final ViewHolder holder, QBChatMessage chatMessage) {
        if (hasAttachments(chatMessage)) {
            Collection<QBAttachment> attachments = chatMessage.getAttachments();
            QBAttachment attachment = attachments.iterator().next();

            holder.messageBodyTextView.setVisibility(View.GONE);
            holder.attachmentImageView.setVisibility(View.VISIBLE);
            holder.attachmentProgressBar.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(attachment.getUrl())
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model,
                                                   Target<GlideDrawable> target, boolean isFirstResource) {
                            e.printStackTrace();
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model,
                                                       Target<GlideDrawable> target,
                                                       boolean isFromMemoryCache, boolean isFirstResource) {
                            holder.attachmentImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            holder.attachmentProgressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .override(PREFERRED_IMAGE_SIZE_PREVIEW, PREFERRED_IMAGE_SIZE_PREVIEW)
                    .dontTransform()
                    .error(R.drawable.ic_error)
                    .into(holder.attachmentImageView);
        } else {
            holder.messageBodyTextView.setText(chatMessage.getBody());
            holder.messageBodyTextView.setVisibility(View.VISIBLE);
            holder.attachmentImageView.setVisibility(View.GONE);
            holder.attachmentProgressBar.setVisibility(View.GONE);
        }
    }

    private void setMessageInfo(QBChatMessage chatMessage, ViewHolder holder) {
        holder.messageInfoTextView.setText(new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(chatMessage.getDateSent() * 1000)));
    }

    private void setMessageAuthor(ViewHolder holder, QBChatMessage chatMessage) {
        if (isIncoming(chatMessage)) {
//            QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
//            holder.messageAuthorTextView.setText(chatMessage.getSenderId());
            holder.messageAuthorTextView.setVisibility(View.VISIBLE);

            if (hasAttachments(chatMessage)) {
                holder.messageAuthorTextView.setBackgroundResource(R.drawable.shape_rectangle_semi_transparent);
                holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_white));
            } else {
                holder.messageAuthorTextView.setBackgroundResource(0);
                holder.messageAuthorTextView.setTextColor(ResourceUtils.getColor(R.color.text_color_dark_grey));
            }
        } else {
            holder.messageAuthorTextView.setVisibility(View.GONE);
        }
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = QBChatService.getInstance().getUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    private boolean hasAttachments(QBChatMessage chatMessage) {
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        return attachments != null && !attachments.isEmpty();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout messageContainerLayout;
        public TextView messageInfoTextView;
        public RelativeLayout messageBodyContainerLayout;
        public ImageView attachmentImageView;
        public TextView messageAuthorTextView;
        public TextView messageBodyTextView;
        public ProgressBar attachmentProgressBar;

        public ViewHolder(View itemView) {
            super(itemView);

            messageContainerLayout = (LinearLayout) itemView.findViewById(R.id.layout_chat_message_container);
            messageInfoTextView = (TextView) itemView.findViewById(R.id.text_message_info);
            messageBodyContainerLayout = (RelativeLayout) itemView.findViewById(R.id.layout_message_content_container);
            attachmentImageView = (ImageView) itemView.findViewById(R.id.image_message_attachment);
            messageAuthorTextView = (TextView) itemView.findViewById(R.id.text_message_author);
            messageBodyTextView = (TextView) itemView.findViewById(R.id.text_image_message);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(R.id.progress_message_attachment);
        }
    }
}
