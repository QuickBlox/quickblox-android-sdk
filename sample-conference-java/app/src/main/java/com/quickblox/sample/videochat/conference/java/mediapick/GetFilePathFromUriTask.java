package com.quickblox.sample.videochat.conference.java.mediapick;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.sample.videochat.conference.java.async.BaseAsyncTask;
import com.quickblox.sample.videochat.conference.java.fragments.ProgressDialogFragment;
import com.quickblox.sample.videochat.conference.java.utils.MediaUtils;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import androidx.fragment.app.FragmentManager;

public class GetFilePathFromUriTask extends BaseAsyncTask<Intent, Void, File> {

    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_CONTENT_GOOGLE = "content://com.google.android";
    private static final String SCHEME_FILE = "file";

    private WeakReference<FragmentManager> fmWeakReference;
    private WeakReference<Context> contextWeakReference;
    private OnMediaPickedListener listener;
    private int requestCode;

    public GetFilePathFromUriTask(Context context, FragmentManager fm, OnMediaPickedListener listener, int requestCode) {
        this.fmWeakReference = new WeakReference<>(fm);
        this.contextWeakReference = new WeakReference<>(context);
        this.listener = listener;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgress();
    }

    @Override
    public File performInBackground(Intent... params) throws Exception {
        Intent data = params[0];

        String imageFilePath = null;
        Uri uri = data.getData();
        String uriScheme = uri.getScheme();

        boolean isFromGoogleApp = uri.toString().startsWith(SCHEME_CONTENT_GOOGLE);
        boolean isKitKatAndUpper = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (SCHEME_CONTENT.equalsIgnoreCase(uriScheme) && !isFromGoogleApp && !isKitKatAndUpper) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = contextWeakReference.get().getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageFilePath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
        } else if (SCHEME_FILE.equalsIgnoreCase(uriScheme)) {
            imageFilePath = uri.getPath();
        } else {
            imageFilePath = MediaUtils.getFilePath(contextWeakReference.get(), uri);
        }

        if (TextUtils.isEmpty(imageFilePath)) {
            throw new IOException("Can't find a filepath for URI " + uri.toString());
        }

        return new File(imageFilePath);
    }

    @Override
    public void onResult(File file) {
        hideProgress();
        Log.w(GetFilePathFromUriTask.class.getSimpleName(), "onResult listener = " + listener);
        if (listener != null) {
            listener.onMediaPicked(requestCode, file);
        }
    }

    @Override
    public void onException(Exception e) {
        hideProgress();
        Log.w(GetFilePathFromUriTask.class.getSimpleName(), "onException listener = " + listener);
        if (listener != null) {
            listener.onMediaPickError(requestCode, e);
        }
    }

    private void showProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.show(fm);
        }
    }

    private void hideProgress() {
        FragmentManager fm = fmWeakReference.get();
        if (fm != null) {
            ProgressDialogFragment.hide(fm);
        }
    }
}