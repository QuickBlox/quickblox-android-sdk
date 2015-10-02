package com.sdk.snippets.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;

import java.util.ArrayList;
import java.util.List;

public class Snippets {

    private static final String TAG = Snippet.class.getSimpleName();
    protected Context context;
    protected ArrayList<Snippet> snippets = new ArrayList<Snippet>();

    private Handler handler = new Handler(Looper.getMainLooper());

    public void log(final String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
            }
        });

        Log.i(TAG, data);
    }

    public void handleErrors(List<String> errors) {
        String message = String.format("[ERROR] Request has been completed with errors: %s", errors);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        // print
        Log.i(TAG, message);
    }


    public Snippets(Context context) {
        this.context = context;

        ApplicationConfig.init(context);

        // App credentials from QB Admin Panel
        QBSettings.getInstance().fastConfigInit(ApplicationConfig.getInstance().getAppId(),
                ApplicationConfig.getInstance().getAuthKey(), ApplicationConfig.getInstance().getAuthSecret());
//
        // specify custom domains
        QBSettings.getInstance().setServerApiDomain(ApplicationConfig.getInstance().getApiDomain());
        QBSettings.getInstance().setChatServerDomain(ApplicationConfig.getInstance().getChatDomain());
        QBSettings.getInstance().setContentBucketName(ApplicationConfig.getInstance().getBucketName());

        QBSettings.getInstance().setTransferProtocol(TransferProtocol.HTTP);
    }

    public ArrayList<Snippet> getSnippets() {
        return snippets;
    }

    public void setSnippets(ArrayList<Snippet> snippets) {
        this.snippets = snippets;
    }
}