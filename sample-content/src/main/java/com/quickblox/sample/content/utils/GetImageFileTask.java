package com.quickblox.sample.content.utils;

import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;

public class GetImageFileTask extends AsyncTask {

    private OnGetImageFileListener listener;

    public GetImageFileTask(OnGetImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        File imageFile = null;
        ImageHelper imageHelper = (ImageHelper) params[0];
        ImageView imageView = (ImageView) params[1];

        try {
            imageFile = imageHelper.getFileFromImageView(imageView);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile;
    }

    @Override
    protected void onPostExecute(Object imageFile) {
        listener.onGotImageFile((File) imageFile);
    }
}