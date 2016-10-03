package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.Collection;
import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    private static final String TAG = CustomMessageAdapter.class.getSimpleName();
    private RequestListener glideRequestListener;

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateCustomViewHolder viewType= " + viewType);
        return new ImageAttachHolder(inflater.inflate(com.quickblox.sample.core.R.layout.widget_attach_msg_left, parent, false), com.quickblox.sample.core.R.id.attach_imageview, com.quickblox.sample.core.R.id.centered_progressbar);
    }



    @Override
    public void displayAttachment(QBMessageViewHolder holder, int position) {
        int preferredImageSizePreview = (int) (80 * Resources.getSystem().getDisplayMetrics().density);
//        int valueType = getItemViewType(position);
//        Log.d(TAG, "displayAttachment valueType= " + valueType);
        initGlideRequestListener((ImageAttachHolder) holder);

        QBChatMessage chatMessage = getItem(position);

        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        Glide.with(context)
                .load(attachment.getUrl())
                .listener(glideRequestListener)
                .override(preferredImageSizePreview, preferredImageSizePreview)
                .dontTransform()
                .error(com.quickblox.sample.core.R.drawable.ic_error)
                .into(((ImageAttachHolder) holder).attachImageView);
    }

    private void initGlideRequestListener(final ImageAttachHolder holder) {
        glideRequestListener = new RequestListener() {

            @Override
            public boolean onException(Exception e, Object model, Target target, boolean isFirstResource) {
                e.printStackTrace();
                holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                holder.attachmentProgressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, boolean isFromMemoryCache, boolean isFirstResource) {
                holder.attachImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.attachmentProgressBar.setVisibility(View.GONE);
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
                .error(com.quickblox.sample.core.R.drawable.ic_error)
                .into(imageView);
    }

    @Override
    protected void onBindViewMsgOpponentHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        TextView view = (TextView) holder.itemView.findViewById(R.id.custom_text_view);
        view.setText("Don Juan");
        super.onBindViewMsgOpponentHolder(holder, chatMessage, position);

        }


//
//    //    если хотим отображать аватарку, определяем avatarUrl
//    @Override
//    public String obtainAvatarUrl(int valueType, QBChatMessage chatMessage) {
//        String avatarUrl = null;
//        return avatarUrl;
//    }
//
//    //    можно переопределить displayAvatarImage
//    @Override
//    public void displayAvatarImage(String uri, ImageView imageView) {
//     // logic
//    }
//
//    // пример переопределения ViewHolder в случае кастомного типа аттача
//    @Override
//    public QBMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//
//        int typeCustomAttachLayoutResource = com.quickblox.sample.core.R.layout.widget_attach_msg_left;
//        int customAttachID = R.id.attach_imageview;
//        int customProgressBar = R.id.centered_progressbar;
//
//        if (viewType > TYPE_OPPONENT_ATTACH) {
//            Log.d(TAG, "Override TYPE_ATTACHMENT_CUSTOM");
//            return new CustomViewHolder(inflater.inflate(typeCustomAttachLayoutResource, parent, false), customAttachID, customProgressBar);
//        }
//        return super.onCreateViewHolder(parent, viewType);
//    }
//
//    @Override
//    protected void onBindViewCustomHolder(QBMessageViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewCustomHolder");
//    }
//
//    @Override
//    public void onBindViewHolder(QBMessageViewHolder holder, int position) {
////      примеры того, что можно сетить
//
////        setOwnMessageLayoutResource(R.layout.item_text_message_own);
////        setMsgLayoutResourceByType(ViewTypes.TYPE_OWN_MESSAGE, R.layout.item_text_message_own);
////
////        setTextOwnText("Mine text");
////
////        setTextOwnSize(16);
////        setTextOwnColor(Color.BLUE);
//
//        setWidgetOwnId(R.id.custom_text_view);
//        super.onBindViewHolder(holder, position);
//    }
//
//    @Override
//    protected void onBindViewMsgOwnHolder(TextMsgOwnHolder holder, int position) {
//
//        TextView textView = (TextView) holder.widgetOwn;
//        textView.setText("My name");
//        super.onBindViewMsgOwnHolder(holder, position);
//    }
//
//    //    кастомный ViewHolder для аттачей
//    protected static class CustomViewHolder extends QBMessageViewHolder {
//        public View attach;
//        public ProgressBar attachmentProgressBar;
//
//        public CustomViewHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
//            super(itemView);
//            attach = itemView.findViewById(attachId);
//            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
//        }
//    }

}
