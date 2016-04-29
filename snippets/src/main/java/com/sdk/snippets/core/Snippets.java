package com.sdk.snippets.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;

import java.util.ArrayList;

public class Snippets {

    private static final String TAG = Snippet.class.getSimpleName();
    protected Context context;
    protected ArrayList<Snippet> snippets = new ArrayList<Snippet>();

    private Handler handler = new Handler(Looper.getMainLooper());

    public void log(final String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toaster.shortToast(data);
            }
        });

        Log.i(TAG, data);
    }

    public void handleErrors(QBResponseException exc) {
        String message = String.format("[ERROR] Request has been completed with errors: %s", exc.getErrors()
                + ", code: " + exc.getHttpStatusCode());
        Toaster.shortToast(message);

        // print
        Log.i(TAG, message);
    }


    public Snippets(Context context) {
        this.context = context;
    }

    public ArrayList<Snippet> getSnippets() {
        return snippets;
    }

    public void setSnippets(ArrayList<Snippet> snippets) {
        this.snippets = snippets;
    }
}