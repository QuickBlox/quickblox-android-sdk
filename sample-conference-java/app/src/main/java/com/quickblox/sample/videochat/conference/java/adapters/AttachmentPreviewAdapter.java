package com.quickblox.sample.videochat.conference.java.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.conference.java.App;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class AttachmentPreviewAdapter extends BaseAdapter {
    private static final String TAG = AttachmentPreviewAdapter.class.getSimpleName();
    private static final long MAX_FILE_SIZE_100MB = 104857600;
    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private List<File> fileList = new ArrayList<>();

    private Map<File, QBAttachment> fileQBAttachmentMap;
    private Map<File, Integer> fileUploadProgressMap;

    private AttachmentCountChangedListener countChangedListener;
    private AttachmentUploadErrorListener errorListener;

    private Context context;

    public AttachmentPreviewAdapter(Context context,
                                    AttachmentCountChangedListener countChangedListener,
                                    AttachmentUploadErrorListener errorListener) {
        this.context = context;
        fileQBAttachmentMap = Collections.synchronizedMap(new HashMap<>());
        fileUploadProgressMap = Collections.synchronizedMap(new HashMap<>());
        this.countChangedListener = countChangedListener;
        this.errorListener = errorListener;
    }

    public void add(final File item) {
        if (item.length() <= MAX_FILE_SIZE_100MB) {
            fileUploadProgressMap.put(item, 1);
            ((App) context.getApplicationContext()).getChatHelper().loadFileAsAttachment(item, new QBEntityCallback<QBAttachment>() {
                @Override
                public void onSuccess(QBAttachment result, Bundle params) {
                    fileUploadProgressMap.remove(item);
                    fileQBAttachmentMap.put(item, result);
                    notifyDataSetChanged();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.e(TAG, "Error loading file: " + e.getMessage());
                    errorListener.onAttachmentUploadError(e);
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
            fileList.add(item);
            countChangedListener.onAttachmentCountChanged(getCount());
        } else {
            ToastUtils.shortToast(context, R.string.error_attachment_size);
        }
    }

    private void remove(File item) {
        fileUploadProgressMap.remove(item);
        fileQBAttachmentMap.remove(item);

        fileList.remove(item);
        countChangedListener.onAttachmentCountChanged(getCount());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_attachment_preview, parent, false);
            holder.attachmentImageView = (ImageView) convertView.findViewById(R.id.image_attachment_preview);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_attachment_preview);
            holder.deleteButton = (ImageButton) convertView.findViewById(R.id.button_attachment_preview_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final File attachmentFile = getItem(position);

        try {
            Glide.with(context)
                    .load(attachmentFile)
                    .override((int) context.getResources().getDimension(R.dimen.chat_attachment_preview_size),
                            (int) context.getResources().getDimension(R.dimen.chat_attachment_preview_size))
                    .into(holder.attachmentImageView);
        } catch (Resources.NotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        if (isFileUploading(attachmentFile)) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null);

            int progress = fileUploadProgressMap.get(attachmentFile);
            holder.progressBar.setProgress(progress);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(v -> remove(attachmentFile));
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

    private boolean isFileUploading(File attachmentFile) {
        return fileUploadProgressMap.containsKey(attachmentFile) && !fileQBAttachmentMap.containsKey(attachmentFile);
    }

    @Override
    public File getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    public Collection<QBAttachment> getUploadedAttachments() {
        return new HashSet<>(fileQBAttachmentMap.values());
    }

    private static class ViewHolder {
        ImageView attachmentImageView;
        ProgressBar progressBar;
        ImageButton deleteButton;
    }

    public interface AttachmentCountChangedListener {

        void onAttachmentCountChanged(int count);
    }

    public interface AttachmentUploadErrorListener {

        void onAttachmentUploadError(QBResponseException e);
    }
}