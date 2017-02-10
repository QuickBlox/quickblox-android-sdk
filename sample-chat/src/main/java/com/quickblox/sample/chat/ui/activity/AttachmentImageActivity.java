package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.utils.Consts;

public class AttachmentImageActivity extends BaseActivity {

    private static final String EXTRA_URL = "url";

    private ImageView imageView;
    private ProgressBar progressBar;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, AttachmentImageActivity.class);
        intent.putExtra(EXTRA_URL, url);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        initUI();
        loadImage();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return _findViewById(R.id.layout_root);
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        imageView = _findViewById(R.id.image_full_view);
        progressBar = _findViewById(R.id.progress_bar_show_image);
    }

    private void loadImage() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (TextUtils.isEmpty(url)) {
            imageView.setImageResource(R.drawable.ic_error_white);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
                        e.printStackTrace();
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .error(R.drawable.ic_error_white)
                .dontTransform()
                .override(Consts.PREFERRED_IMAGE_SIZE_FULL, Consts.PREFERRED_IMAGE_SIZE_FULL)
                .into(imageView);
    }

}