package com.quickblox.sample.content.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.content.model.QBFile;
import com.quickblox.sample.content.R;
import com.quickblox.sample.content.helper.DataHolder;
import com.quickblox.sample.content.utils.Consts;
import com.quickblox.sample.content.utils.QBContentUtils;

public class ShowImageActivity extends BaseActivity {

    private static final String EXTRA_QBFILE_ID = "id";

    private ImageView imageView;
    private ProgressBar progressBar;

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
        setImageGlide();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        imageView = _findViewById(R.id.image_full_view);
        progressBar = _findViewById(R.id.progress_bar_show_image);
    }

    private void setImageGlide() {
        int id = getIntent().getIntExtra(EXTRA_QBFILE_ID, 0);
        QBFile qbFile = DataHolder.getInstance().getQBFile(id);

        progressBar.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(QBContentUtils.getUrl(qbFile))
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
                .dontTransform()
                .override(Consts.PREFER_IMAGE_WIDTH, Consts.PREFER_IMAGE_HEIGHT)
                .into(imageView);
    }
}