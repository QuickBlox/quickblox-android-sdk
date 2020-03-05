package com.quickblox.sample.chat.java.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.core.io.IOUtils;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.managers.DialogsManager;
import com.quickblox.sample.chat.java.ui.activity.ChatActivity;
import com.quickblox.sample.chat.java.ui.adapter.listeners.AttachClickListener;
import com.quickblox.sample.chat.java.ui.adapter.listeners.MessageLongClickListener;
import com.quickblox.sample.chat.java.utils.TimeUtils;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.UiUtils;
import com.quickblox.sample.chat.java.utils.ValidationUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.sample.chat.java.utils.qb.PaginationHistoryListener;
import com.quickblox.sample.chat.java.utils.qb.QbUsersHolder;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;


public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.NewMessageViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = ChatAdapter.class.getSimpleName();

    public static final int CUSTOM_VIEW_TYPE = -1;
    public static final int TYPE_TEXT_RIGHT = 1;
    public static final int TYPE_TEXT_LEFT = 2;
    public static final int TYPE_ATTACH_RIGHT = 3;
    public static final int TYPE_ATTACH_LEFT = 4;
    public static final int TYPE_NOTIFICATION_CENTER = 5;

    private static final int FILE_DOWNLOAD_ATTEMPS_COUNT = 2;
    private static final float ATTACHMENT_CORNER_RADIUS = 20;

    protected Context context;
    private final QBChatDialog chatDialog;
    private List<QBChatMessage> chatMessages;
    private PaginationHistoryListener paginationListener;
    private int previousGetCount = 0;
    private LayoutInflater inflater;

    private AttachClickListener attachImageClickListener;
    private AttachClickListener attachVideoClickListener;
    private AttachClickListener attachFileClickListener;
    private MessageLongClickListener messageLongClickListener;

    private HashMap<String, Integer> fileLoadingAttemptsMap = new HashMap<>();

    private SparseIntArray containerLayoutRes = new SparseIntArray() {
        {
            put(TYPE_TEXT_RIGHT, R.layout.list_item_message_right);
            put(TYPE_TEXT_LEFT, R.layout.list_item_message_left);
            put(TYPE_ATTACH_RIGHT, R.layout.list_item_message_right);
            put(TYPE_ATTACH_LEFT, R.layout.list_item_message_left);
            put(TYPE_NOTIFICATION_CENTER, R.layout.list_item_notification_message);
            put(CUSTOM_VIEW_TYPE, R.layout.list_item_notification_message);
        }
    };

    public ChatAdapter(Context context, QBChatDialog chatDialog, List<QBChatMessage> chatMessages) {
        this.chatDialog = chatDialog;
        this.context = context;
        this.chatMessages = chatMessages;
        this.inflater = LayoutInflater.from(context);
    }

    public void updateStatusDelivered(String messageID, Integer userId) {
        for (int position = 0; position < chatMessages.size(); position++) {
            QBChatMessage message = chatMessages.get(position);
            if (message.getId().equals(messageID)) {
                ArrayList<Integer> deliveredIds = new ArrayList<>();
                if (message.getDeliveredIds() != null) {
                    deliveredIds.addAll(message.getDeliveredIds());
                }
                deliveredIds.add(userId);
                message.setDeliveredIds(deliveredIds);
                notifyItemChanged(position);
            }
        }
    }

    public void updateStatusRead(String messageID, Integer userId) {
        for (int position = 0; position < chatMessages.size(); position++) {
            QBChatMessage message = chatMessages.get(position);
            if (message.getId().equals(messageID)) {
                ArrayList<Integer> readIds = new ArrayList<>();
                if (message.getReadIds() != null) {
                    readIds.addAll(message.getReadIds());
                }
                readIds.add(userId);
                message.setReadIds(readIds);
                notifyItemChanged(position);
            }
        }
    }

    public void setAttachImageClickListener(AttachClickListener clickListener) {
        attachImageClickListener = clickListener;
    }

    public void setAttachVideoClickListener(AttachClickListener clickListener) {
        attachVideoClickListener = clickListener;
    }

    public void setAttachFileClickListener(AttachClickListener clickListener) {
        attachFileClickListener = clickListener;
    }

    public void setMessageLongClickListener(MessageLongClickListener longClickListener) {
        messageLongClickListener = longClickListener;
    }

    public void removeClickListeners() {
        attachImageClickListener = null;
        attachVideoClickListener = null;
        attachFileClickListener = null;
        messageLongClickListener = null;
    }

    public void addMessages(List<QBChatMessage> items) {
        chatMessages.addAll(0, items);
        notifyItemRangeInserted(0, items.size());
    }

    public void setMessages(List<QBChatMessage> items) {
        chatMessages.clear();
        chatMessages.addAll(items);
        notifyDataSetChanged();
    }

    public void addMessage(QBChatMessage item) {
        this.chatMessages.add(item);
        this.notifyItemInserted(chatMessages.size() - 1);
    }

    public List<QBChatMessage> getMessages() {
        return chatMessages;
    }

    @Override
    public NewMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View currentView = inflater.inflate(containerLayoutRes.get(viewType), parent, false);
        return new NewMessageViewHolder(currentView);
    }

    @Override
    public void onViewRecycled(NewMessageViewHolder holder) {
        //holder.ivVideoAttachPreview.setImageBitmap(null);
        //abort loading avatar before setting new avatar to view
        if (containerLayoutRes.get(holder.getItemViewType()) != 0 && holder.avatar != null) {
            Glide.clear(holder.avatar);
        }

        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(@NonNull NewMessageViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder on position " + position);
        downloadMore(position);
        QBChatMessage chatMessage = getItem(position);
        if (chatMessage != null) {
            if (isIncoming(chatMessage) && !isReadByCurrentUser(chatMessage)) {
                readMessage(chatMessage);
            }
            if (getItemViewType(position) != TYPE_NOTIFICATION_CENTER && messageLongClickListener != null) {
                holder.rootLayout.setOnLongClickListener(new ItemClickListenerFilter(getItemViewType(position), messageLongClickListener, holder, position));
            }

            switch (getItemViewType(position)) {
                case TYPE_NOTIFICATION_CENTER:
                    onBindViewNotificationHolder(holder, chatMessage);
                    break;
                case TYPE_TEXT_RIGHT:
                    onBindViewMsgRightHolder(holder, chatMessage);
                    break;
                case TYPE_TEXT_LEFT:
                    onBindViewMsgLeftHolder(holder, chatMessage);
                    break;
                case TYPE_ATTACH_RIGHT:
                    onBindViewAttachRightHolder(holder, chatMessage, position);
                    break;
                case TYPE_ATTACH_LEFT:
                    onBindViewAttachLeftHolder(holder, chatMessage, position);
                    break;
                default:
                    Log.d(TAG, "onBindViewHolder TYPE_ATTACHMENT_CUSTOM");
                    break;
            }
        }
    }

    private void onBindViewNotificationHolder(NewMessageViewHolder holder, QBChatMessage chatMessage) {
        holder.tvMessageBody.setText(chatMessage.getBody());
        holder.tvMessageTime.setText(getTime(chatMessage.getDateSent()));
    }

    private void onBindViewMsgRightHolder(NewMessageViewHolder holder, QBChatMessage chatMessage) {
        holder.tvUserName.setText(R.string.you);
        fillTextMessageHolder(holder, chatMessage, false);
    }

    private void onBindViewMsgLeftHolder(NewMessageViewHolder holder, QBChatMessage chatMessage) {
        holder.tvUserName.setText(getSenderName(chatMessage));
        fillTextMessageHolder(holder, chatMessage, true);
    }

    private void fillTextMessageHolder(NewMessageViewHolder holder, QBChatMessage chatMessage, boolean isIncomingMessage) {
        holder.rlImageAttachmentContainer.setVisibility(View.GONE);
        holder.rlVideoAttachmentContainer.setVisibility(View.GONE);
        holder.rlFileAttachmentContainer.setVisibility(View.GONE);
        holder.llMessageBodyContainer.setVisibility(View.VISIBLE);
        holder.tvMessageBody.setText(chatMessage.getBody());
        holder.tvMessageTime.setText(getTime(chatMessage.getDateSent()));

        String forwardedFromName = (String) chatMessage.getProperty(ChatActivity.PROPERTY_FORWARD_USER_NAME);
        if (forwardedFromName != null) {
            holder.llMessageBodyForwardContainer.setVisibility(View.VISIBLE);
            holder.tvTextForwardedFromUser.setText(forwardedFromName);
        } else {
            holder.llMessageBodyForwardContainer.setVisibility(View.GONE);
        }

        if (chatDialog.getType() != QBDialogType.PRIVATE) {
            fillAvatarHolder(holder, chatMessage);
        } else {
            holder.avatarContainer.setVisibility(View.GONE);
        }

        if (!isIncomingMessage) {
            holder.avatarContainer.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean read = isRead(chatMessage);
                boolean delivered = isDelivered(chatMessage);
                if (read) {
                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_read));
                } else if (delivered) {

                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_delivered));
                } else {
                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_sent));
                }
            }
        }
    }

    private void onBindViewAttachRightHolder(NewMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        holder.tvMessageTime.setText(getTime(chatMessage.getDateSent()));
        holder.tvUserName.setText(R.string.you);
        holder.avatarContainer.setVisibility(View.GONE);
        fillAttachHolder(holder, chatMessage, position, false);
    }

    private void onBindViewAttachLeftHolder(NewMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        holder.tvMessageTime.setText(getTime(chatMessage.getDateSent()));
        holder.tvUserName.setText(getSenderName(chatMessage));
        if (chatDialog.getType() != QBDialogType.PRIVATE) {
            fillAvatarHolder(holder, chatMessage);
        } else {
            holder.avatarContainer.setVisibility(View.GONE);
        }

        fillAttachHolder(holder, chatMessage, position, true);
    }

    private void fillAvatarHolder(NewMessageViewHolder holder, QBChatMessage chatMessage) {
        holder.avatarContainer.setVisibility(View.VISIBLE);
        holder.avatarTitle.setVisibility(View.VISIBLE);
        holder.avatar.setBackgroundDrawable(UiUtils.getColorCircleDrawable(chatMessage.getSenderId().hashCode()));

        QBUser user = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
        String avatarTitle = String.valueOf(user.getFullName().charAt(0));
        holder.avatarTitle.setText(avatarTitle);
    }

    private void fillAttachHolder(NewMessageViewHolder holder, QBChatMessage chatMessage, int position, boolean isIncomingMessage) {
        displayAttachment(holder, position, chatMessage);
        setItemAttachClickListener(getAttachListenerByType(position), holder, getAttachment(position), position);
        if (!isIncomingMessage) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean read = isRead(chatMessage);
                boolean delivered = isDelivered(chatMessage);
                if (read) {
                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_read));
                } else if (delivered) {
                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_delivered));
                } else {
                    holder.ivMessageStatus.setImageDrawable(context.getDrawable(R.drawable.ic_status_sent));
                }
            }
        }
    }

    private void displayAttachment(NewMessageViewHolder holder, int position, QBChatMessage chatMessage) {
        QBAttachment attachment = getAttachment(position);
        if (attachment != null) {
            boolean photoType = QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType());
            boolean imageType = QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType());
            boolean videoType = QBAttachment.VIDEO_TYPE.equalsIgnoreCase(attachment.getType()) || attachment.getType().contains("video");
            boolean fileType = attachment.getType().equals("file") || attachment.getType().contains("file") || attachment.getType().equals("");

            if (photoType || imageType) {
                holder.llMessageBodyContainer.setVisibility(View.GONE);
                holder.rlVideoAttachmentContainer.setVisibility(View.GONE);
                holder.rlFileAttachmentContainer.setVisibility(View.GONE);
                holder.rlImageAttachmentContainer.setVisibility(View.VISIBLE);

                String forwardedFromName = (String) chatMessage.getProperty(ChatActivity.PROPERTY_FORWARD_USER_NAME);
                if (forwardedFromName != null) {
                    holder.llImageForwardContainer.setVisibility(View.VISIBLE);
                    holder.tvImageForwardedFromUser.setText(forwardedFromName);
                } else {
                    holder.llImageForwardContainer.setVisibility(View.GONE);
                }

                String imageUrl = QBFile.getPrivateUrlForUID(attachment.getId());

                Glide.with(context)
                        .load(imageUrl)
                        .listener(getRequestListener(holder))
                        .into(holder.ivImageAttachPreview);

                makeRoundedCorners(holder.ivImageAttachPreview, false);

            } else if (videoType) {
                holder.llMessageBodyContainer.setVisibility(View.GONE);
                holder.rlVideoAttachmentContainer.setVisibility(View.VISIBLE);
                holder.rlFileAttachmentContainer.setVisibility(View.GONE);
                holder.rlImageAttachmentContainer.setVisibility(View.GONE);

                holder.tvVideoFileName.setText(attachment.getName());
                holder.tvVideoFileSize.setText(android.text.format.Formatter.formatShortFileSize(context, (long) attachment.getSize()));

                String forwardedFromName = (String) chatMessage.getProperty(ChatActivity.PROPERTY_FORWARD_USER_NAME);
                if (forwardedFromName != null) {
                    holder.llVideoForwardContainer.setVisibility(View.VISIBLE);
                    holder.llVideoForwardedFromUser.setText(forwardedFromName);
                } else {
                    holder.llVideoForwardContainer.setVisibility(View.GONE);
                }

                fileLoadingAttemptsMap.put(attachment.getId(), 0);

                if (attachment.getName() == null) {
                    return;
                }
                String fileName = attachment.getName();
                File videoFile = new File(context.getFilesDir(), fileName);

                if (videoFile.exists()) {
                    fillVideoFileThumb(videoFile, holder, position);
                } else {
                    loadFileFromQB(holder, attachment, videoFile, position);
                }
            } else if (fileType) {
                holder.llMessageBodyContainer.setVisibility(View.GONE);
                holder.rlVideoAttachmentContainer.setVisibility(View.GONE);
                holder.rlFileAttachmentContainer.setVisibility(View.VISIBLE);
                holder.rlImageAttachmentContainer.setVisibility(View.GONE);

                holder.tvFileName.setText(attachment.getName());
                holder.tvFileSize.setText(android.text.format.Formatter.formatShortFileSize(context, (long) attachment.getSize()));

                String forwardedFromName = (String) chatMessage.getProperty(ChatActivity.PROPERTY_FORWARD_USER_NAME);
                if (forwardedFromName != null) {
                    holder.llFileForwardContainer.setVisibility(View.VISIBLE);
                    holder.llFileForwardedFromUser.setText(forwardedFromName);
                } else {
                    holder.llFileForwardContainer.setVisibility(View.GONE);
                }

                fileLoadingAttemptsMap.put(attachment.getId(), 0);

                String fileName = attachment.getName();
                File file = new File(context.getFilesDir(), fileName);

                if (!file.exists()) {
                    loadFileFromQB(holder, attachment, file, position);
                }
            } else {
                ToastUtils.shortToast("Unknown Attachment Received");
            }
        }
    }

    private void loadFileFromQB(final NewMessageViewHolder holder, QBAttachment attachment, final File file, final int position) {
        holder.videoProgress.setVisibility(View.VISIBLE);
        String attachmentID = attachment.getId();
        Log.d(TAG, "Loading File as Attachment id = " + attachmentID);

        // to define download attempts count for each videofile
        Integer attempts = fileLoadingAttemptsMap.get(attachmentID);
        fileLoadingAttemptsMap.put(attachmentID, attempts != null ? attempts + 1 : 1);

        QBContent.downloadFile(attachmentID, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int progress) {
                holder.videoProgress.setProgress(progress);
                Log.d(TAG, "Loading progress updated: $progress");
            }
        }, null).performAsync(new QBEntityCallback<InputStream>() {
            @Override
            public void onSuccess(InputStream inputStream, Bundle bundle) {
                Log.d(TAG, "Loading File as Attachment Successful");
                if (inputStream != null) {
                    new LoaderAsyncTask(file, inputStream, holder, position).execute();
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                holder.videoProgress.setVisibility(View.GONE);
            }
        });
    }

    private void fillVideoFileThumb(File file, NewMessageViewHolder holder, int position) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
        QBAttachment attachment = getAttachment(position);
        if (attachment != null) {
            int attempts = 0;
            if (fileLoadingAttemptsMap.containsKey(attachment.getId())
                    && fileLoadingAttemptsMap.get(attachment.getId()) != null
                    && fileLoadingAttemptsMap.get(attachment.getId()) != 0) {
                attempts = fileLoadingAttemptsMap.get(attachment.getId());
            }

            if (bitmap == null && attempts <= FILE_DOWNLOAD_ATTEMPS_COUNT) {
                Log.d(TAG, "Thumbnail Bitmap is null from Downloaded File " + file.getPath());
                file.delete();
                Log.d(TAG, "Delete file and Reload");
                loadFileFromQB(holder, attachment, file, position);
            } else {
                holder.ivVideoAttachPreview.setImageBitmap(bitmap);
                holder.videoProgress.setVisibility(View.GONE);
                makeRoundedCorners(holder.ivImageAttachPreview, true);
            }
        }
    }

    private void makeRoundedCorners(ImageView imageView, final boolean onlyTopCorners) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && imageView != null) {
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    if (onlyTopCorners) {
                        outline.setRoundRect(0, 0, view.getWidth(), (int) (view.getHeight() + ATTACHMENT_CORNER_RADIUS), ATTACHMENT_CORNER_RADIUS);
                    } else {
                        outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), ATTACHMENT_CORNER_RADIUS);
                    }
                }
            });
            imageView.setClipToOutline(true);
        }
    }

    private void setItemAttachClickListener(AttachClickListener listener, NewMessageViewHolder holder, QBAttachment qbAttachment, int position) {
        if (listener != null) {
            holder.rootLayout.setOnClickListener(new ItemClickListenerFilter(getItemViewType(position), listener, holder, position));
        }
    }

    private AttachClickListener getAttachListenerByType(int position) {
        QBAttachment attachment = getAttachment(position);

        if (attachment != null) {
            if (QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType())
                    || QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType())) {
                return attachImageClickListener;
            } else if (QBAttachment.VIDEO_TYPE.equalsIgnoreCase(attachment.getType())) {
                return attachVideoClickListener;
            } else if (attachment.getType().equals("file") || attachment.getType().equals("") || attachment.getType().contains("file")) {
                return attachFileClickListener;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private String getSenderName(QBChatMessage chatMessage) {
        QBUser sender = QbUsersHolder.getInstance().getUserById(chatMessage.getSenderId());
        String fullName = "";
        if (sender != null && !TextUtils.isEmpty(sender.getFullName())) {
            fullName = sender.getFullName();
        }
        return fullName;
    }

    private void readMessage(QBChatMessage chatMessage) {
        try {
            chatDialog.readMessage(chatMessage);
        } catch (XMPPException | SmackException.NotConnectedException e) {
            Log.w(TAG, e);
        }
    }

    private boolean isReadByCurrentUser(QBChatMessage chatMessage) {
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        return !CollectionsUtil.isEmpty(chatMessage.getReadIds()) && chatMessage.getReadIds().contains(currentUserId);
    }

    private boolean isRead(QBChatMessage chatMessage) {
        boolean read = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        Collection<Integer> readIds = chatMessage.getReadIds();
        if (readIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && readIds.contains(recipientId)) {
            read = true;
        } else if (readIds.size() == 1 && readIds.contains(currentUserId)) {
            read = false;
        } else if (readIds.size() > 0) {
            read = true;
        }
        return read;
    }

    private boolean isDelivered(QBChatMessage chatMessage) {
        boolean delivered = false;
        Integer recipientId = chatMessage.getRecipientId();
        Integer currentUserId = ChatHelper.getCurrentUser().getId();
        Collection<Integer> deliveredIds = chatMessage.getDeliveredIds();
        if (deliveredIds == null) {
            return false;
        }
        if (recipientId != null && !recipientId.equals(currentUserId) && deliveredIds.contains(recipientId)) {
            delivered = true;
        } else if (deliveredIds.size() == 1 && deliveredIds.contains(currentUserId)) {
            delivered = false;
        } else if (deliveredIds.size() > 0) {
            delivered = true;
        }
        return delivered;
    }

    public void setPaginationHistoryListener(PaginationHistoryListener paginationListener) {
        this.paginationListener = paginationListener;
    }

    private void downloadMore(int position) {
        if (position == 0) {
            if (getItemCount() != previousGetCount) {
                paginationListener.downloadMore();
                previousGetCount = getItemCount();
            }
        }
    }

    @Override
    public long getHeaderId(int position) {
        QBChatMessage chatMessage = getItem(position);
        if (chatMessage != null) {
            return TimeUtils.getDateAsHeaderId(chatMessage.getDateSent() * 1000);
        } else {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.view_chat_message_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;
        TextView dateTextView = view.findViewById(R.id.header_date_textview);

        QBChatMessage chatMessage = getItem(position);
        if (chatMessage != null) {
            String title = "";
            long timeInMillis = chatMessage.getDateSent() * 1000;
            Calendar msgTime = Calendar.getInstance();
            msgTime.setTimeInMillis(timeInMillis);

            Calendar now = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM", Locale.ENGLISH);
            SimpleDateFormat lastYearFormat = new SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH);

            boolean sameDay = now.get(Calendar.DATE) == msgTime.get(Calendar.DATE);
            boolean lastDay = now.get(Calendar.DAY_OF_YEAR) - msgTime.get(Calendar.DAY_OF_YEAR) == 1;
            boolean sameYear = now.get(Calendar.YEAR) == msgTime.get(Calendar.YEAR);

            if (sameDay && sameYear) {
                title = context.getString(R.string.today);
            } else if (lastDay & sameYear) {
                title = context.getString(R.string.yesterday);
            } else if (sameYear) {
                title = dateFormat.format(new Date(timeInMillis));
            } else {
                title = lastYearFormat.format(new Date(timeInMillis));
            }

            dateTextView.setText(title);
        }

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) dateTextView.getLayoutParams();
        lp.topMargin = 0;
        dateTextView.setLayoutParams(lp);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    private QBChatMessage getItem(int position) {
        if (position <= getItemCount() - 1) {
            return chatMessages.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        QBChatMessage chatMessage = getItem(position);
        int itemViewType = CUSTOM_VIEW_TYPE;

        if (chatMessage != null) {
            if (chatMessage.getProperty(DialogsManager.PROPERTY_NOTIFICATION_TYPE) != null) {
                itemViewType = TYPE_NOTIFICATION_CENTER;
            } else if (chatMessage.getAttachments() != null && chatMessage.getAttachments().size() > 0) {
                QBAttachment attachment = getAttachment(position);
                boolean photo = QBAttachment.PHOTO_TYPE.equalsIgnoreCase(attachment.getType());
                boolean image = QBAttachment.IMAGE_TYPE.equalsIgnoreCase(attachment.getType());
                boolean video = QBAttachment.VIDEO_TYPE.equalsIgnoreCase(attachment.getType());
                boolean audio = QBAttachment.AUDIO_TYPE.equalsIgnoreCase(attachment.getType());
                boolean file = attachment.getType().equals("file") || attachment.getType().contains("file") || attachment.getType().equals("");

                if (photo || image || video || audio || file) {
                    if (isIncoming(chatMessage)) {
                        itemViewType = TYPE_ATTACH_LEFT;
                    } else {
                        itemViewType = TYPE_ATTACH_RIGHT;
                    }
                }
            } else if (isIncoming(chatMessage)) {
                itemViewType = TYPE_TEXT_LEFT;
            } else {
                itemViewType = TYPE_TEXT_RIGHT;
            }
        }
        return itemViewType;
    }

    private boolean isIncoming(QBChatMessage chatMessage) {
        QBUser currentUser = ChatHelper.getCurrentUser();
        return chatMessage.getSenderId() != null && !chatMessage.getSenderId().equals(currentUser.getId());
    }

    /**
     * @return string in "Hours:Minutes" format, i.e. <b>10:15</b>
     */
    private String getTime(long seconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(seconds * 1000));
    }

    /**
     * @return string in "Month Day" format, i.e. <b>APRIL 25</b>
     */
    public static String getDate(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        return dateFormat.format(new Date(milliseconds * 1000));
    }

    private QBAttachment getAttachment(int position) {
        QBChatMessage chatMessage = getItem(position);
        if (chatMessage != null && chatMessage.getAttachments() != null && chatMessage.getAttachments().iterator().hasNext()) {
            return chatMessage.getAttachments().iterator().next();
        } else {
            return null;
        }
    }

    private RequestListener getRequestListener(NewMessageViewHolder holder) {
        return new ImageLoadListener(holder);
    }

    public static class NewMessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout rootLayout = itemView.findViewById(R.id.ll_root_message_item);

        RelativeLayout avatarContainer = itemView.findViewById(R.id.rl_avatar_container);
        CircleImageView avatar = itemView.findViewById(R.id.civ_avatar);
        TextView avatarTitle = itemView.findViewById(R.id.tv_avatar_title);

        LinearLayout llMessageContainer = itemView.findViewById(R.id.ll_message_container);
        LinearLayout llMessageBodyContainer = itemView.findViewById(R.id.ll_message_body_container);
        LinearLayout llMessageBodyForwardContainer = itemView.findViewById(R.id.ll_forwarded_container);
        TextView tvTextForwardedFromUser = itemView.findViewById(R.id.tv_forwarded_from_user);
        TextView tvMessageBody = itemView.findViewById(R.id.tv_message_body);

        RelativeLayout rlImageAttachmentContainer = itemView.findViewById(R.id.rl_image_attach_container);
        ImageView ivImageAttachPreview = itemView.findViewById(R.id.iv_attach_image_preview);
        LinearLayout llImageForwardContainer = itemView.findViewById(R.id.ll_image_forwarded_container);
        TextView tvImageForwardedFromUser = itemView.findViewById(R.id.tv_image_forward_from_user);
        ProgressBar pbImageProgress = itemView.findViewById(R.id.pb_attach_image);

        RelativeLayout rlVideoAttachmentContainer = itemView.findViewById(R.id.rl_video_attach_container);
        LinearLayout llVideoForwardContainer = itemView.findViewById(R.id.ll_video_forwarded_container);
        TextView llVideoForwardedFromUser = itemView.findViewById(R.id.tv_video_forward_from_user);
        ImageView ivVideoAttachPreview = itemView.findViewById(R.id.iv_attach_video_preview);
        TextView tvVideoFileName = itemView.findViewById(R.id.tv_attach_video_name);
        TextView tvVideoFileSize = itemView.findViewById(R.id.tv_attach_video_size);
        ProgressBar videoProgress = itemView.findViewById(R.id.pb_attach_video);

        RelativeLayout rlFileAttachmentContainer = itemView.findViewById(R.id.rl_file_attach_container);
        LinearLayout llFileForwardContainer = itemView.findViewById(R.id.ll_file_forwarded_container);
        TextView llFileForwardedFromUser = itemView.findViewById(R.id.tv_file_forward_from_user);
        TextView tvFileName = itemView.findViewById(R.id.tv_attach_file_name);
        TextView tvFileSize = itemView.findViewById(R.id.tv_attach_file_size);

        TextView tvUserName = itemView.findViewById(R.id.tv_user_name);
        TextView tvMessageTime = itemView.findViewById(R.id.tv_time_sent);

        ImageView ivMessageStatus = itemView.findViewById(R.id.iv_message_status);


        public NewMessageViewHolder(View itemView) {
            super(itemView);
        }
    }

    protected static class ImageLoadListener<M, P> implements RequestListener<M, P> {
        private NewMessageViewHolder holder;

        private ImageLoadListener(NewMessageViewHolder holder) {
            this.holder = holder;
            holder.pbImageProgress.setVisibility(View.VISIBLE);
        }

        @Override
        public boolean onException(Exception e, M model, Target<P> target, boolean isFirstResource) {
            Log.e(TAG, "ImageLoadListener Exception= " + e.getMessage());
            holder.ivImageAttachPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.pbImageProgress.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(P resource, M model, Target<P> target, boolean isFromMemoryCache, boolean isFirstResource) {
            holder.ivImageAttachPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            holder.pbImageProgress.setVisibility(View.GONE);
            return false;
        }
    }

    private class ItemClickListenerFilter implements View.OnClickListener, View.OnLongClickListener {
        private AttachClickListener attachClickListener;
        private MessageLongClickListener messageLongClickListener;
        private NewMessageViewHolder holder;
        private int itemViewType;
        private int position;

        ItemClickListenerFilter(int itemViewType, AttachClickListener attachClickListener, NewMessageViewHolder holder, int position) {
            this.itemViewType = itemViewType;
            this.attachClickListener = attachClickListener;
            this.holder = holder;
            this.position = position;
        }

        ItemClickListenerFilter(int itemViewType, MessageLongClickListener messageLongClickListener, NewMessageViewHolder holder, int position) {
            this.itemViewType = itemViewType;
            this.messageLongClickListener = messageLongClickListener;
            this.holder = holder;
            this.position = position;
        }

        @Override
        public void onClick(View view) {
            if (holder.llMessageContainer != null && getItem(position) != null && getItem(position).getAttachments() != null) {
                Iterator<QBAttachment> iterator = getItem(position).getAttachments().iterator();
                if (iterator != null && iterator.hasNext()) {
                    QBAttachment attachment = iterator.next();
                    if (ValidationUtils.isAttachmentValid(attachment)) {
                        attachClickListener.onAttachmentClicked(itemViewType, holder.llMessageContainer, attachment);
                    } else {
                        ToastUtils.shortToast(context.getString(R.string.error_attachment_corrupted));
                    }
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (holder.llMessageContainer != null) {
                QBChatMessage message = getItem(position);
                messageLongClickListener.onMessageLongClicked(itemViewType, holder.llMessageContainer, message);
            }
            return true;
        }
    }

    private class LoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private File file;
        private InputStream inputStream;
        private NewMessageViewHolder holder;
        private int position;

        LoaderAsyncTask(File file, InputStream inputStream, NewMessageViewHolder holder, int position) {
            this.file = file;
            this.inputStream = inputStream;
            this.holder = holder;
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(TAG, "Downloading File as InputStream");
            try {
                FileOutputStream output = new FileOutputStream(file);

                if (inputStream != null) {
                    IOUtils.copy(inputStream, output);
                    inputStream.close();
                    output.close();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }


            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.d(TAG, "File Downloaded");
            fillVideoFileThumb(file, holder, position);
        }
    }
}