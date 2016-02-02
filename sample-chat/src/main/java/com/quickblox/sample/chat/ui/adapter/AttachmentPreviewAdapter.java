package com.quickblox.sample.chat.ui.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBProgressCallback;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.adapter.BaseListAdapter;
import com.quickblox.sample.core.utils.ResourceUtils;
import com.quickblox.sample.core.utils.Toaster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttachmentPreviewAdapter extends BaseListAdapter<File> {

    private Map<File, QBAttachment> fileQBAttachmentMap;
    private Map<File, Integer> fileUploadProgressMap;

    public AttachmentPreviewAdapter(Context context) {
        super(context);
        fileQBAttachmentMap = new HashMap<>();
        fileUploadProgressMap = new HashMap<>();
    }

    @Override
    public void add(final File item) {
        super.add(item);

        ChatHelper.getInstance().loadFileAsAttachment(item, new QBEntityCallbackImpl<QBAttachment>() {
            @Override
            public void onSuccess(QBAttachment result, Bundle params) {
                fileUploadProgressMap.remove(item);
                fileQBAttachmentMap.put(item, result);
            }

            @Override
            public void onError(List<String> errors) {
                Toaster.shortToast(R.string.chat_attachment_error);
                remove(item);
            }
        }, new QBProgressCallback() {
            @Override
            public void onProgressUpdate(int i) {
                fileUploadProgressMap.put(item, i);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void remove(File item) {
        super.remove(item);
        fileUploadProgressMap.remove(item);
        fileQBAttachmentMap.remove(item);
    }

    public void remove(QBAttachment qbAttachment) {
        if (fileQBAttachmentMap.containsValue(qbAttachment)) {
            Set<File> files = fileQBAttachmentMap.keySet();
            for (File file : files) {
                QBAttachment attachment = fileQBAttachmentMap.get(file);
                if (attachment.equals(qbAttachment)) {
                    remove(file);
                }
            }
        }
    }

    public Collection<QBAttachment> getUploadedAttachments() {
        return fileQBAttachmentMap.values();
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
        Glide.with(context)
                .load(attachmentFile)
                .override(ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size),
                        ResourceUtils.getDimen(R.dimen.chat_attachment_preview_size))
                .into(holder.attachmentImageView);

        if (fileUploadProgressMap.containsKey(attachmentFile)) {
            int progress = fileUploadProgressMap.get(attachmentFile);
            holder.progressBar.setProgress(progress);

            holder.progressBar.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.deleteButton.setOnClickListener(null);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fileQBAttachmentMap.remove(attachmentFile);
                    remove(attachmentFile);
                }
            });
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView attachmentImageView;
        ProgressBar progressBar;
        ImageButton deleteButton;
    }
}
