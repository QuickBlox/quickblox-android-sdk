package com.quickblox.sample.content.utils;

import android.widget.ImageView;

import com.quickblox.sample.core.async.BaseAsyncTask;

import java.io.File;

public class GetImageFileTask extends BaseAsyncTask<Object, Void, File> {

    private OnGetImageFileListener listener;

    public GetImageFileTask(OnGetImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    public File performInBackground(Object... params) throws Exception {
        ImageHelper imageHelper = (ImageHelper) params[0];
        ImageView imageView = (ImageView) params[1];

        File imageFile = imageHelper.getFileFromImageView(imageView);

        return imageFile;
    }

    @Override
    public void onResult(File imageFile) {
        listener.onGotImageFile(imageFile);
    }
}