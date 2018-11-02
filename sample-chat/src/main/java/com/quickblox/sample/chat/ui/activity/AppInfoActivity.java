package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.sample.chat.BuildConfig;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.core.utils.VersionUtils;

/**
 * Created by pavlov egor on 11/1/18.
 */

public class AppInfoActivity extends BaseActivity {

    private TextView tvAppVersion;
    private TextView tvSDKVersion;
    private TextView tvAppID;
    private TextView tvAuthKey;
    private TextView tvAuthSecret;
    private TextView tvAccountKey;
    private TextView tvApiDomain;
    private TextView tvChatDomain;

    public static void start(Context context) {
        Intent intent = new Intent(context, AppInfoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected View getSnackbarAnchorView() {
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appinfo);

        initUI();
        fillUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        tvAppVersion = _findViewById(R.id.tv_app_version);
        tvSDKVersion = _findViewById(R.id.tv_sdk_version);
        tvAppID = _findViewById(R.id.tv_app_id);
        tvAuthKey = _findViewById(R.id.tv_auth_key);
        tvAuthSecret = _findViewById(R.id.tv_auth_secret);
        tvAccountKey = _findViewById(R.id.tv_account_key);
        tvApiDomain = _findViewById(R.id.tv_api_domain);
        tvChatDomain = _findViewById(R.id.tv_chat_domain);
    }

    public void fillUI() {
        tvAppVersion.setText(VersionUtils.getAppVersionName());
        tvSDKVersion.setText(BuildConfig.qbSdkVer);
        tvAppID.setText(VersionUtils.getAppId());
        tvAuthKey.setText(VersionUtils.getAuthorizationKey());
        tvAuthSecret.setText(VersionUtils.getAuthorizationSecret());
        tvAccountKey.setText(VersionUtils.getAccountKey());
        tvApiDomain.setText(VersionUtils.getApiDomain());
        tvChatDomain.setText(VersionUtils.getChatDomain());
    }
}