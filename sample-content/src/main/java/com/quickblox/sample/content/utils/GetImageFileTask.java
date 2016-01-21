package com.quickblox.sample.content.utils;

import com.quickblox.sample.core.async.BaseAsyncTask;

import java.io.File;

public class GetImageFileTask extends BaseAsyncTask<String, Void, File> {

    private OnGetImageFileListener listener;

    public GetImageFileTask(OnGetImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    public File performInBackground(String... params) throws Exception {
        String path = params[0];
        File file = new File(path);
        return file;
    }

    @Override
    public void onResult(File file) {
        listener.onGotImageFile(file);
    }
}