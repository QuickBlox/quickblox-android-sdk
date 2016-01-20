package com.quickblox.sample.content.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.QBContentUtils;

public class ShowImageActivity extends BaseActivity {

    private static final String EXTRA_QBFILE_ID = "id";

    private ImageView imageView;
    private ProgressBar progressBar;

    private DisplayImageOptions displayImageOptions;

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, ShowImageActivity.class);
        intent.putExtra(EXTRA_QBFILE_ID, id);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        initUI();
//        initImageLoaderOptions();
//        showSelectedImage();
        setImageGlide();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        imageView = _findViewById(R.id.image_upload_view);
        progressBar = _findViewById(R.id.progress_bar);
    }

    private void initImageLoaderOptions() {
        // TODO All Builder classes should use method chain formatting
        displayImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).resetViewBeforeLoading(true).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true).displayer(new FadeInBitmapDisplayer(300))
                .build();
    }


    private void showSelectedImage() {
        int id = getIntent().getIntExtra(EXTRA_QBFILE_ID, 0);
        QBFile qbFile = DataHolder.getInstance().getQBFile(id);
        ImageLoader.getInstance().displayImage(
                QBContentUtils.getUrl(qbFile),
                imageView, displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        QBContentUtils.showTypeError(failReason);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }

    private void setImageGlide() {
        int id = getIntent().getIntExtra(EXTRA_QBFILE_ID, 0);
        QBFile qbFile = DataHolder.getInstance().getQBFile(id);
        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this).load(QBContentUtils.getUrl(qbFile)).preload();
        Glide
                .with(this)
                .load(QBContentUtils.getUrl(qbFile))
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.ic_error)
                .fitCenter()
                .override(300, 558)
                .into(imageView);
    }
}