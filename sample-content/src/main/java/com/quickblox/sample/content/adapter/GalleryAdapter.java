package com.quickblox.sample.content.adapter;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;

public class GalleryAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private DisplayImageOptions displayImageOptions;

    public GalleryAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        initImageLoaderOptions();
    }

    public void initImageLoaderOptions() {
        displayImageOptions = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
                .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_error).cacheInMemory(
                        true).cacheOnDisc(true).considerExifParams(true).bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    @Override
    public int getCount() {
        return DataHolder.getDataHolder().getQbFileListSize();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.list_item_gallery, parent, false);
            holder = new ViewHolder();
            assert view != null;
            holder.imageView = (ImageView) view.findViewById(R.id.image_image_view);
            holder.progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        ImageLoader.getInstance().displayImage(
                DataHolder.getDataHolder().getUrl(position),
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

        return view;
    }

    private class ViewHolder {

        ImageView imageView;
        ProgressBar progressBar;
    }
}