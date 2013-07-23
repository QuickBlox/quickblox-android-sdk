package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.gcm.GCMHelper;
import com.quickblox.chat_v2.interfaces.OnFileUploadComplete;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.users.model.QBUser;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class ProfileActivity extends Activity implements OnPictureDownloadComplete, OnFileUploadComplete {

    private ImageView userpic;
    private ChatApplication app;
    private QBUser tUser;

    private final int SELECT_PHOTO = 1;
    private boolean blockUiMode;
    private ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userpic = (ImageView) findViewById(R.id.profile_userpic);
        TextView username = (TextView) findViewById(R.id.chat_dialog_view_profile);

        TopBar tb = (TopBar) findViewById(R.id.top_bar);
        tb.setVisibility(View.GONE);
        tb.setFriendParams(new QBUser(), false);

        app = ChatApplication.getInstance();

        tUser = app.getQbUser();
        setOnProfilePictureClicListener();

        username.setText(tUser.getFullName() != null ? tUser.getFullName() : tUser.getLogin());

        Button exitButton = (Button) findViewById(R.id.exit_profile_button);
        exitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                SharedPreferencesHelper.setLogin(ProfileActivity.this, null);
                SharedPreferencesHelper.setPassword(ProfileActivity.this, null);


                app.clearAllData();
                GCMHelper.unregister(ProfileActivity.this);
                QBChat qbChat = QBChat.getInstance();
                qbChat.stopAutoSendPresence();
                qbChat.disconnect();

                Session session = new Session(ProfileActivity.this);
                session.closeAndClearTokenInformation();

                getParent().finish();

                Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {

                    try {

                        Bitmap yourSelectedImage = app.getPicManager().decodeUri(imageReturnedIntent.getData());
                        userpic.setImageBitmap(yourSelectedImage);

                        ChatApplication.getInstance().setMyPic(yourSelectedImage);
                        app.getQbm().setUploadListener(ProfileActivity.this);
                        app.getQbm().uploadPic(app.getPicManager().convertBitmapToFile(app.getMyPic()), false);
                        swichProgressDialog(true);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    private void setOnProfilePictureClicListener() {

        userpic.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, SELECT_PHOTO);
            }
        });
    }

    @Override
    public void downloadComlete(final Bitmap bitmap, File file) {
        ProfileActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                userpic.setImageBitmap(bitmap);
                app.setMyPic(bitmap);
                swichProgressDialog(false);
            }
        });

    }

    public void swichProgressDialog(boolean enable) {
        blockUiMode = enable;
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (blockUiMode) {
                    progress = ProgressDialog.show(ProfileActivity.this, getResources().getString(R.string.app_name), getResources().getString(R.string.profile_activity_photo_refresh), true);
                } else {
                    if (progress != null) {
                        progress.dismiss();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (app.getMyPic() != null) {
            userpic.setImageBitmap(app.getMyPic());
            return;
        }

        if (tUser != null && tUser.getFileId() != null) {
            app.getQbm().setPictureDownloadComplete(this);
            app.getQbm().downloadQBFile(tUser);
            return;

        }

        if (tUser != null && tUser.getWebsite() != null) {
            app.getPicManager().downloadPicAndDisplay(tUser.getWebsite(), userpic);
        }

    }

    @Override
    public void uploadComplete(int uploafFileId, String picUrl) {

        swichProgressDialog(false);
    }
}
