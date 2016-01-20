package com.quickblox.sample.content.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.quickblox.sample.content.R;
import com.quickblox.sample.core.utils.Toaster;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

// TODO AsyncTask is parametrized class, need use generic types to avoid casting all parameters to Object
// TODO Extend from BaseAsyncTask as it has more convenient exception handling
public class GetImageFileTask extends AsyncTask {

    private OnGetImageFileListener listener;

    public GetImageFileTask(OnGetImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    protected Object doInBackground(Object... params) {
        File imageFile = null;
        String path = (String)params[0];
//        ImageHelper imageHelper = (ImageHelper) params[0];
        ImageView imageView = (ImageView) params[1];

        //            imageFile = imageHelper.getFileFromImageView(imageView);

        File file = new File(path);
        return file;
    }

    @Override
    protected void onPostExecute(Object file) {
        listener.onGotImageFile((File) file);
    }
}