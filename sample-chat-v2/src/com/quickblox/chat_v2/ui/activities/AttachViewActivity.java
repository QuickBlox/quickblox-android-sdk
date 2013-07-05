package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.chat_v2.widget.TopBar;

/**
 * Created by andrey on 06.06.13.
 */
public class AttachViewActivity extends Activity implements ImageLoadingListener {

    private TopBar topBar;
    private ImageView content;

    private ChatApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_attach_view);

        topBar = (TopBar) findViewById(R.id.top_bar);
        content = (ImageView) findViewById(R.id.chat_attach_view);

        app = ChatApplication.getInstance();

        initView();
    }

    private void initView() {
        topBar.setFragmentParams(getString(R.string.chat_activity_attach_view), View.INVISIBLE, false);
        app.getPicManager().downloadPicAndDisplay(getIntent().getStringExtra(GlobalConsts.ATTACH_URL), content, this);

        Toast.makeText(this, getString(R.string.chat_activity_attach_info_2), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onLoadingStarted() {
        topBar.swichProgressBarVisibility(View.VISIBLE);
    }

    @Override
    public void onLoadingFailed(FailReason failReason) {
        topBar.swichProgressBarVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoadingComplete(Bitmap bitmap) {
        topBar.swichProgressBarVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoadingCancelled() {
        topBar.swichProgressBarVisibility(View.INVISIBLE);
    }
}
