package com.quickblox.sample.chat.java.ui.activity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.quickblox.sample.chat.java.R;

import java.io.File;

import androidx.annotation.Nullable;

public class AttachmentVideoActivity extends BaseActivity {
    private static final String EXTRA_FILE_NAME = "video_file_name";
    private static final String EXTRA_FILE_URL = "video_file_URL";

    private RelativeLayout rootLayout;
    private VideoView videoView;
    private ProgressBar progressBar;
    private MediaController mediaController;
    private File file = null;

    public static void start(Context context, String attachmentName, String url) {
        Intent intent = new Intent(context, AttachmentVideoActivity.class);
        intent.putExtra(EXTRA_FILE_URL, url);
        intent.putExtra(EXTRA_FILE_NAME, attachmentName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        initUI();
        loadVideo();
    }

    private void initUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setBackgroundDrawable(getDrawable(R.drawable.toolbar_video_player_background));
            }
            getSupportActionBar().setTitle(getIntent().getStringExtra(EXTRA_FILE_NAME));
            getSupportActionBar().setElevation(0);
        }
        rootLayout = findViewById(R.id.layout_root);
        videoView = findViewById(R.id.vv_full_view);
        progressBar = findViewById(R.id.progress_show_video);

        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.show(2000);
            }
        });
    }

    private void loadVideo() {
        progressBar.setVisibility(View.VISIBLE);
        String filename = getIntent().getStringExtra(EXTRA_FILE_NAME);
        File file = new File(getApplication().getFilesDir(), filename);

        if (file != null) {
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.setVideoPath(file.getPath());
            videoView.start();
        }

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressBar.setVisibility(View.GONE);
                mediaController.show(2000);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                progressBar.setVisibility(View.GONE);
                mediaController.hide();
                showErrorSnackbar(R.string.error_load_video, null, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadVideo();
                    }
                });
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
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
        if (file != null) {
            try {
                String url = getIntent().getStringExtra(EXTRA_FILE_URL);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, file.getName());
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.allowScanningByMediaScanner();
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                if (manager != null) {
                    manager.enqueue(request);
                }
            } catch (SecurityException e) {
                if (e.getMessage() != null) {
                    Log.d("Security Exception", e.getMessage());
                }
            }
        }
    }
}