package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.adapter.BaseListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class AttachmentPreviewAdapter extends BaseListAdapter<File> {

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Map<File, QBAttachment> fileQBAttachmentMap;
    private Map<File, Integer> fileUploadProgressMap;

    private OnAttachmentCountChangedListener onAttachmentCountChangedListener;
    private OnAttachmentUploadErrorListener onAttachmentUploadErrorListener;

    public AttachmentPreviewAdapter(Context context,
                                    OnAttachmentCountChangedListener countChangedListener,
                                    OnAttachmentUploadErrorListener errorListener) {
        super(context);
        fileQBAttachmentMap = Collections.synchronizedMap(new HashMap<File, QBAttachment>());
        fileUploadProgressMap = Collections.synchronizedMap(new HashMap<File, Integer>());
        onAttachmentCountChangedListener = countChangedListener;
        onAttachmentUploadErrorListener = errorListener;
    }

    @Override
    public void add(final File item) {
        fileUploadProgressMap.put(item, 1);
        ChatHelper.getInstance().loadFileAsAttachment(item, new QBEntityCallback<QBAttachment>() {
            @Override
            public void onSuccess(QBAttachment result, Bundle params) {
                fileUploadProgressMap.remove(item);
                fileQBAttachmentMap.put(item, result);
                notifyDataSetChanged();
            }

            @Override
            public void onError(QBResponseException e) {
                onAttachmentUploadErrorListener.onAttachmentUploadError(e);
                remove(item);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(final int progress) {
                fileUploadProgressMap.put(item, progress);
                mainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        });

        super.add(item);
        onAttachmentCountChangedListener.onAttachmentCountChanged(getCount());
    }

    @Override
    public void remove(File item) {
        fileUploadProgressMap.remove(item);
        fileQBAttachmentMap.remove(item);

        super.remove(item);
        onAttachmentCountChangedListener.onAttachmentCountChanged(getCount());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.item_attachment_preview, parent, false);
            holder.attachmentImageView = (ImageView) convertView.findViewById(R.id.image_attachment_preview);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_attachment_preview);
            holder.deleteButton = (ImageButton) convertView.findViewById(R.id.button_attachment_preview_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final File attachmentFile = getItem(position);
        Glide.with(App.getInstance())
                .load(attachmentFile)
                .override(ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size),
                        ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size))
                .into(holder.attachmentImageView);

        if (isFileUploading(attachmentFile)) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null);

            int progress = fileUploadProgressMap.get(attachmentFile);
            holder.progressBar.setProgress(progress);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    remove(attachmentFile);
                }
            });
        }

        return convertView;
    }

    public void remove(QBAttachment qbAttachment) {
        if (fileQBAttachmentMap.containsValue(qbAttachment)) {
            for (File file : fileQBAttachmentMap.keySet()) {
                QBAttachment attachment = fileQBAttachmentMap.get(file);
                if (attachment.equals(qbAttachment)) {
                    remove(file);
                    break;
                }
            }
        }
    }

    public Collection<QBAttachment> getUploadedAttachments() {
        return new HashSet<>(fileQBAttachmentMap.values());
    }

    private boolean isFileUploading(File attachmentFile) {
        return fileUploadProgressMap.containsKey(attachmentFile) && !fileQBAttachmentMap.containsKey(attachmentFile);
    }

    private static class ViewHolder {
        ImageView attachmentImageView;
        ProgressBar progressBar;
        ImageButton deleteButton;
    }

    public interface OnAttachmentCountChangedListener {
        void onAttachmentCountChanged(int count);
    }

    public interface OnAttachmentUploadErrorListener {
        void onAttachmentUploadError(QBResponseException e);
    }
}
