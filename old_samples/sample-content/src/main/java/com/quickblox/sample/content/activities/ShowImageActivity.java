package com.quickblox.sample.content.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.content.utils.QBContentUtils;

public class ShowImageActivity extends BaseActivity {

    private static final String EXTRA_QB_FILE_ID = "qb_file_id";
    private static final int NO_ID = -1;

    private ImageView imageView;
    private ProgressBar progressBar;

    public static void start(Context context, int id) {
        Intent intent = new Intent(context, ShowImageActivity.class);
        intent.putExtra(EXTRA_QB_FILE_ID, id);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        initUI();
        loadImage();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        imageView = _findViewById(R.id.image_full_view);
        progressBar = _findViewById(R.id.progress_bar_show_image);
    }

    private void loadImage() {
        int id = getIntent().getIntExtra(EXTRA_QB_FILE_ID, NO_ID);
        QBFile qbFile = DataHolder.getInstance().getQBFile(id);
        if (qbFile == null) {
            imageView.setImageResource(R.drawable.ic_error);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(QBContentUtils.getUrl(qbFile))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target, boolean isFirstResource) {
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
                .override(Consts.PREFERRED_IMAGE_WIDTH_FULL, Consts.PREFERRED_IMAGE_HEIGHT_FULL)
                .into(imageView);
    }
}