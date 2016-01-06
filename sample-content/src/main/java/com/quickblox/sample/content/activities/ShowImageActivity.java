package com.quickblox.sample.content.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.core.utils.Toaster;

public class ShowImageActivity extends BaseActivity {

    private ImageView imageView;
    private ProgressBar progressBar;

    private DisplayImageOptions displayImageOptions;
    private int currentPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        currentPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        initUI();
        initImageLoaderOptions();
        showSelectedImage();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        imageView = _findViewById(R.id.image_upload_view);
        progressBar = _findViewById(R.id.progress_bar);
    }

    private void initImageLoaderOptions() {
        displayImageOptions = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.ic_empty)
                .showImageOnFail(R.drawable.ic_error).resetViewBeforeLoading(true).cacheOnDisc(true)
                .imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565)
                .considerExifParams(true).displayer(new FadeInBitmapDisplayer(300))
                .build();
    }

    private void showSelectedImage() {
        ImageLoader.getInstance().displayImage(
                DataHolder.getInstance().getUrl(currentPosition),
                imageView, displayImageOptions, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progressBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        String message = null;

                        switch (failReason.getType()) {
                            case IO_ERROR:
                                message = getString(R.string.mgs_io_error);
                                break;
                            case DECODING_ERROR:
                                message = getString(R.string.mgs_decode_error);
                                break;
                            case NETWORK_DENIED:
                                message = getString(R.string.mgs_denied_error);
                                break;
                            case OUT_OF_MEMORY:
                                message = getString(R.string.mgs_memory_error);
                                break;
                            case UNKNOWN:
                                message = getString(R.string.mgs_unknown_error);
                                break;
                        }
                        Toaster.longToast(message);
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );
    }
}