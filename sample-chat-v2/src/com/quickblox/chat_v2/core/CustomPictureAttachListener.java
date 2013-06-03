package com.quickblox.chat_v2.core;

import android.content.DialogInterface;
import android.view.View;


public class CustomPictureAttachListener implements View.OnClickListener {

    private String pictureUrl;

    @Override
    public void onClick(View view) {

    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
