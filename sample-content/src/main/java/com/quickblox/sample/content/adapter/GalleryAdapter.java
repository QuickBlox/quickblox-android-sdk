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
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.QBContentUtils;

public class GalleryAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private DisplayImageOptions displayImageOptions;
    // TODO Rename field without shortening
    private SparseArray<QBFile> qbFileSparseArr;
    private Activity activity;

    public GalleryAdapter(Activity activity, SparseArray<QBFile> qbFileSparseArr) {
        this.activity = activity;
        layoutInflater = LayoutInflater.from(activity);
        this.qbFileSparseArr = qbFileSparseArr;
//        initImageLoaderOptions();
    }

    public void initImageLoaderOptions() {
        displayImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error)
                .cacheInMemory(true).cacheOnDisc(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public int getCount() {
        return qbFileSparseArr.size();
    }

    @Override
    public Object getItem(int position) {
        return qbFileSparseArr.valueAt(position);
    }

    @Override
    public long getItemId(int position) {
        return qbFileSparseArr.keyAt(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_gallery, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.image_show_view);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

//        setImageUniversal(holder, position);
        setImageGlide(holder, position);
        return convertView;
    }
    private void setImageGlide(final ViewHolder holder, int position) {
        QBFile qbFile = DataHolder.getInstance().getQBFileSparseArray().valueAt(position);
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
                .override(300, 558)
                .into(holder.imageView);
    }
    private void setImage(final ViewHolder holder, int position) {
        // TODO Adapter shouldn't know about DataHolder existence, it has it's own data set and should work only with it
        QBFile qbFile = DataHolder.getInstance().getQBFileSparseArray().valueAt(position);
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

    // TODO We're not updating adapter, we're updating data. Method name should told us about it's purpose
    public void updateAdapter(SparseArray<QBFile> qbFileSparseArr) {
        this.qbFileSparseArr = qbFileSparseArr;
        notifyDataSetChanged();
    }

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}