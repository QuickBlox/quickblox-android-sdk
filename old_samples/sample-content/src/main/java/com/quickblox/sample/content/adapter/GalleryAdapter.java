package com.quickblox.sample.content.adapter;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DownloadMoreListener;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.content.utils.QBContentUtils;
import com.quickblox.sample.core.ui.adapter.BaseListAdapter;

public class GalleryAdapter extends BaseListAdapter<QBFile> {

    private SparseArray<QBFile> qbFileSparseArray;
    private DownloadMoreListener downloadListener;
    private int previousGetCount = 0;

    public GalleryAdapter(Context context, SparseArray<QBFile> qbFileSparseArray) {
        super(context);
        this.qbFileSparseArray = qbFileSparseArray;
    }

    @Override
    public int getCount() {
        return qbFileSparseArray.size();
    }

    @Override
    public QBFile getItem(int position) {
        return qbFileSparseArray.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return qbFileSparseArray.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_gallery, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_preview);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar_adapter);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        QBFile qbFile = getItem(position);
        loadImage(holder, qbFile);

        downloadMore(position);
        return convertView;
    }

    private void downloadMore(int position) {
        int count = getCount();
        if (count - 1 == position) {
            if (count != previousGetCount) {
                downloadListener.downloadMore();
                previousGetCount = count;
            }
        }
    }

    public void updateData(SparseArray<QBFile> qbFileSparseArray) {
        this.qbFileSparseArray = qbFileSparseArray;
        notifyDataSetChanged();
    }

    private void loadImage(final ViewHolder holder, QBFile qbFile) {
        holder.progressBar.setVisibility(View.VISIBLE);

        Priority customPriority = qbFile.getSize() > Consts.PRIORITY_MAX_IMAGE_SIZE
                ? Priority.LOW
                : Priority.NORMAL;

        Glide.with(context)
                .load(QBContentUtils.getUrl(qbFile))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(customPriority)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        return false;
                    }
                })
                .error(R.drawable.ic_error)
                .dontAnimate()
                .dontTransform()
                .override(Consts.PREFERRED_IMAGE_WIDTH_PREVIEW, Consts.PREFERRED_IMAGE_HEIGHT_PREVIEW)
                .into(holder.imageView);
    }

    public void setDownloadMoreListener(DownloadMoreListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}