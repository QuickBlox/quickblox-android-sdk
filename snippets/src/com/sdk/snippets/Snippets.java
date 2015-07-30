package com.sdk.snippets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.result.Result;

import java.util.ArrayList;

public class Snippets {

    private static final String TAG = Snippet.class.getSimpleName();
    protected Context context;
    protected ArrayList<Snippet> snippets = new ArrayList<Snippet>();

    private Handler handler = new Handler(Looper.getMainLooper());

    public void printResultToConsole(Result result) {
        String message = "";
        if (result.isSuccess()) {
            message = "[OK] Result is successful! You can cast result to specific result and extract data.";
            Toast.makeText(context, "[OK] Result is successful!", Toast.LENGTH_SHORT).show();
        } else {
            message = String.format("[ERROR %s] Request has been completed with errors: %s",
                    result.getStatusCode(), result.getErrors());
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
        Log.i(TAG, message);
    }

    public void log(final String data){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
            }
        });

        Log.i(TAG, data);
    }

    public void handleErrors(QBResponseException exc) {
        String message = String.format("[ERROR] Request has been completed with errors: %s", exc.getErrors()
                + ", code: " + exc.getHttpStatusCode());
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
    }

    public ArrayList<Snippet> getSnippets() {
        return snippets;
    }

    public void setSnippets(ArrayList<Snippet> snippets) {
        this.snippets = snippets;
    }
}