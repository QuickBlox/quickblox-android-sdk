package com.quickblox.sample.content.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.content.utils.QBContentUtils;
import com.quickblox.sample.core.utils.ResourceUtils;

public class GalleryAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private DisplayImageOptions displayImageOptions;
    private SparseArray<QBFile> qbFileSparseArray;
    private Activity activity;

    public GalleryAdapter(Activity activity, SparseArray<QBFile> qbFileSparseArray) {
        this.activity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.qbFileSparseArray = qbFileSparseArray;
        initImageLoaderOptions();
    }

    public void initImageLoaderOptions() {
        displayImageOptions = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public int getCount() {
        return qbFileSparseArray.size();
    }

    @Override
    public Object getItem(int position) {
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
            convertView = layoutInflater.inflate(R.layout.list_item_gallery, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_preview);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar_adapter);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        QBFile qbFile = (QBFile) getItem(position);
        setImageUniversal(holder, qbFile);
//        setImageGlide(holder, qbFile);
        return convertView;
    }

    private void setImageGlide(final ViewHolder holder, QBFile qbFile) {
        holder.progressBar.setVisibility(View.VISIBLE);
        Glide
                .with(activity)
                .load(QBContentUtils.getUrl(qbFile))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .placeholder(R.drawable.ic_stub)
                .error(R.drawable.ic_error)
                .dontAnimate()
                .fitCenter()
                .override(ResourceUtils.dpToPx(Consts.PREFER_IMAGE_HEIGHT), ResourceUtils.dpToPx(Consts.PREFER_IMAGE_WIDTH))
                .into(holder.imageView);
    }

    private void setImageUniversal(final ViewHolder holder, QBFile qbFile) {
        ImageLoader.getInstance().displayImage(QBContentUtils.getUrl(qbFile),
                holder.imageView, displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        holder.progressBar.setProgress(0);
                        holder.progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        holder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        holder.progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }

    public void updateData(SparseArray<QBFile> qbFileSparseArr) {
        this.qbFileSparseArray = qbFileSparseArr;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}