package com.quickblox.sample.content.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;

import java.util.List;

public class GalleryAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private DisplayImageOptions displayImageOptions;
    private List<QBFile> qbFileList;

    public GalleryAdapter(Context context, List<QBFile> qbFileList) {
        layoutInflater = LayoutInflater.from(context);
        this.qbFileList = qbFileList;
        initImageLoaderOptions();
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
        return qbFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return qbFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        setImage(holder, position);

        return convertView;
    }

    private void setImage(final ViewHolder holder, int position) {

        ImageLoader.getInstance().displayImage(
                DataHolder.getInstance().getUrl(position),
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

    private static class ViewHolder {
        ImageView imageView;
        ProgressBar progressBar;
    }
}