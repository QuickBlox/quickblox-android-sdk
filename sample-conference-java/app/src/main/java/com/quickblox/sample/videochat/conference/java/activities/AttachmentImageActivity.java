package com.quickblox.sample.videochat.conference.java.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;

public class AttachmentImageActivity extends BaseActivity {
    private static final String TAG = AttachmentImageActivity.class.getSimpleName();
    private static final String EXTRA_URL = "url";

    private ImageView imageView;
    private ProgressBar progressBar;
    private boolean imageLoaded = false;

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

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.toolbar_video_player_background));
            }
            getSupportActionBar().setElevation(0f);
        }
        imageView = findViewById(R.id.iv_full_view);
        progressBar = findViewById(R.id.progress_show_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_video_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_player_save:
                saveFileToGallery();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveFileToGallery() {
        if (imageLoaded) {
            try {
                Bitmap bitmapToSave = ((BitmapDrawable) imageView.getDrawable().getCurrent()).getBitmap();
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmapToSave, "attachment", "");
                ToastUtils.shortToast(getApplicationContext(), "Image saved to the Gallery");
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                }
                ToastUtils.shortToast(getApplicationContext(), "Unable to save image");
            }
        } else {
            ToastUtils.shortToast(getApplicationContext(), "Image not yet downloaded");
        }
    }

    private void loadImage() {
        String url = getIntent().getStringExtra(EXTRA_URL);
        progressBar.setVisibility(View.VISIBLE);

        int preferredImageSizeFull = 320 * (int) getApplicationContext().getResources().getDisplayMetrics().density;
        Glide.with(this)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new DrawableListener())
                .error(R.drawable.ic_error_white)
                .dontTransform()
                .override(preferredImageSizeFull, preferredImageSizeFull)
                .into(imageView);
    }

    private class DrawableListener implements RequestListener<Drawable> {
        @Override
        public boolean onLoadFailed(@Nullable GlideException exception, Object model, Target<Drawable> target, boolean isFirstResource) {
            if (exception == null) {
                exception = new GlideException("Unable to load image");
            }

            Log.d("Glide Drawable", exception.getMessage());
            showErrorSnackbar(R.string.error_load_image, exception, null);
            progressBar.setVisibility(View.GONE);
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            progressBar.setVisibility(View.GONE);
            imageLoaded = true;
            return false;
        }
    }
}