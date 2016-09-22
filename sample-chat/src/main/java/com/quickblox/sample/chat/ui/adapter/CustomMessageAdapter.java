package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.adapter.QBMessagesAdapter;

import java.util.List;

public class CustomMessageAdapter extends QBMessagesAdapter {

    private static final String TAG = CustomMessageAdapter.class.getSimpleName();

    public CustomMessageAdapter(Context context, List<QBChatMessage> chatMessages) {
        super(context, chatMessages);
    }

    //    если хотим отображать аватарку, определяем avatarUrl
    @Override
    public String obtainAvatarUrl(ViewTypes valueType, QBChatMessage chatMessage) {
        String avatarUrl = null;
        return avatarUrl;
    }

    //    можно переопределить displayAvatarImage
    @Override
    public void displayAvatarImage(String uri, ImageView imageView) {
     // logic
    }

    // пример переопределения ViewHolder в случае кастомного типа аттача
    @Override
    public QBMessagesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int typeCustomAttachLayoutResource = com.quickblox.sample.core.R.layout.item_attachment_message_opponent;
        int customAttachID = R.id.attach_imageview;
        int customProgressBar = R.id.centered_progressbar;

        if (ViewTypes.values()[viewType] == ViewTypes.TYPE_ATTACHMENT_CUSTOM) {
            Log.d(TAG, "Override TYPE_ATTACHMENT_CUSTOM");
            return new CustomViewHolder(inflater.inflate(typeCustomAttachLayoutResource, parent, false), customAttachID, customProgressBar);
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    protected void onBindViewAttachCustomHolder(QBMessagesAdapterViewHolder holder, int position) {
        Log.d(TAG, "onBindViewAttachCustomHolder");
    }

    @Override
    public void onBindViewHolder(QBMessagesAdapterViewHolder holder, int position) {
//      примеры того, что можно сетить

//        setOwnMessageLayoutResource(R.layout.item_text_message_own);
//        setMessageLayoutResourceByType(ViewTypes.TYPE_OWN_MESSAGE, R.layout.item_text_message_own);
//
//        setTextOwnText("Mine text");
//
//        setTextOwnSize(16);
//        setTextOwnColor(Color.BLUE);

        setWidgetOwnId(R.id.custom_text_view);
        super.onBindViewHolder(holder, position);
    }

    @Override
    protected void onBindViewMsgOwnHolder(MessageOwnHolder holder, int position) {

        TextView textView = (TextView) holder.widgetOwn;
        textView.setText("My name");
        super.onBindViewMsgOwnHolder(holder, position);
    }

    //    кастомный ViewHolder для аттачей
    protected static class CustomViewHolder extends QBMessagesAdapterViewHolder {
        public View attach;
        public ProgressBar attachmentProgressBar;

        public CustomViewHolder(View itemView, @IdRes int attachId, @IdRes int progressBarId) {
            super(itemView);
            attach = itemView.findViewById(attachId);
            attachmentProgressBar = (ProgressBar) itemView.findViewById(progressBarId);
        }
    }

}
