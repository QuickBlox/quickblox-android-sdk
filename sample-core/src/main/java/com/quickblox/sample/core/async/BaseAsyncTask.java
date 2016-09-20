package com.quickblox.sample.core.async;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private static final String TAG = BaseAsyncTask.class.getSimpleName();

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private boolean isExceptionOccurred;

    @Override
    protected final Result doInBackground(Params... params) {
        try {
            return performInBackground(params);
        } catch (final Exception e) {
            isExceptionOccurred = true;
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    onException(e);
                }
            });
            return null;
        }
    }

    @Override
    protected final void onPostExecute(Result result) {
        if (!isExceptionOccurred) {
            onResult(result);
        }
    }

    public abstract Result performInBackground(Params... params) throws Exception;

    public abstract void onResult(Result result);

    public void onException(Exception e) {
        Log.w(TAG, e);
    }
}

