package com.quickblox.content.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.quickblox.content.R;
import com.quickblox.content.helper.DataHolder;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 04.12.12
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class GalleryAdapter extends BaseAdapter {

    private Context ctx;
    LayoutInflater inflater;


    public GalleryAdapter(Context ctx) {
        this.ctx = ctx;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return DataHolder.getDataHolder().getQbFileListSize();
    }

    public Object getItem(int position) {
        return 0;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.gallery_img, null);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.img);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        applyImg(viewHolder.img, position);
        return convertView;
    }


    private void applyImg(ImageView coverIv, final int position) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory()
                .cacheOnDisc()
                .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
                .displayer(new RoundedBitmapDisplayer(20))
                .build();
        // Load and display image
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx)
                .defaultDisplayImageOptions(options)
                .build();
        ImageLoader.getInstance().init(config);
        ImageLoader.getInstance().displayImage("http://qbprod.s3.amazonaws.com/" + DataHolder.getDataHolder().getPublicUrl(position), coverIv);
    }

    static class ViewHolder {
        ImageView img;
    }

}
