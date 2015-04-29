package com.quickblox.sample.chat.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.core.ApplicationSessionStateCallback;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by igorkhomenko on 4/29/15.
 */
public class BaseActivity extends Activity implements ApplicationSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final String USER_LOGIN_KEY = "USER_LOGIN_KEY";
    private static final String USER_PASSWORD_KEY = "USER_PASSWORD_KEY";

    private boolean sessionActive = false;
    private boolean needToRecreateSession = false;

    private ProgressDialog progressDialog;

    public boolean isSessionActive() {
        return sessionActive;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 'initialised' will be true if it's the 1st start of the app or if the app's process was killed by OS(or user)
        //
        boolean initialised = ChatService.initIfNeed(this);
        if(initialised && savedInstanceState != null){
            Log.d(TAG, "needToRecreateSession");
            needToRecreateSession = true;
        }else{
            sessionActive = true;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(needToRecreateSession){
            needToRecreateSession = false;

            Log.d(TAG, "Need to restore chat connection");

            sessionActive = false;
            this.onStartSessionRecreation();

            showProgressDialog();

            // Restoring Chat session
            //
            QBUser user = new QBUser();
            user.setLogin(savedInstanceState.getString(USER_LOGIN_KEY));
            user.setPassword(savedInstanceState.getString(USER_PASSWORD_KEY));

            savedInstanceState.remove(USER_LOGIN_KEY);
            savedInstanceState.remove(USER_PASSWORD_KEY);

            ChatService.getInstance().login(user, new QBEntityCallbackImpl() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Chat login onSuccess");

                    progressDialog.dismiss();
                    progressDialog = null;

                    sessionActive = true;
                    BaseActivity.this.onFinishSessionRecreation(true);
                }

                @Override
                public void onError(List errors) {
                    progressDialog.dismiss();
                    progressDialog = null;

                    Log.d(TAG, "Chat login onError: " + errors);
                    BaseActivity.this.onFinishSessionRecreation(false);
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putString(USER_LOGIN_KEY, ChatService.getInstance().getCurrentUser().getLogin());
        outState.putString(USER_PASSWORD_KEY, ChatService.getInstance().getCurrentUser().getPassword());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    private void showProgressDialog(){
        if(progressDialog == null) {
            progressDialog = new ProgressDialog(BaseActivity.this);
            progressDialog.setTitle("Loading");
            progressDialog.setMessage("Restoring chat session...");
            progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        }
        progressDialog.show();
    }


    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onStartSessionRecreation() {
    }

    @Override
    public void onFinishSessionRecreation(boolean success) {
    }
}
