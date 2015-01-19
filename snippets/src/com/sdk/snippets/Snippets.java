package com.sdk.snippets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;
import com.quickblox.core.result.Result;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Oleg Soroka
 * Date: 02.10.12
 * Time: 11:02
 */
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

    public void handleErrors(Result result) {
        String message = String.format("[ERROR %s] Request has been completed with errors: %s",
                result.getStatusCode(), result.getErrors());
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

        // print
        Log.i(TAG,message);
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

    public class QBEmptyCallback extends QBEntityCallbackImpl<Void>{

        private String successMsg;

        public QBEmptyCallback(String successMsg){
            this.successMsg = successMsg;
        }

        @Override
        public void onSuccess() {
            Log.i(TAG, successMsg);
        }

        @Override
        public void onError(List<String> errors) {
            handleErrors(errors);
        }
    }
}