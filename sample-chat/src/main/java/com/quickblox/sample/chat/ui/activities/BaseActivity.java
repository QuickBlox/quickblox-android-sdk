package com.quickblox.sample.chat.ui.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.core.ApplicationSessionStateCallback;
import com.quickblox.sample.chat.core.ChatService;
import com.quickblox.users.model.QBUser;

import java.util.List;

/**
 * Created by igorkhomenko on 4/29/15.
 */
public class BaseActivity extends AppCompatActivity implements ApplicationSessionStateCallback {
    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final String USER_LOGIN_KEY = "USER_LOGIN_KEY";
    private static final String USER_PASSWORD_KEY = "USER_PASSWORD_KEY";

    private boolean sessionActive = false;
    private boolean needToRecreateSession = false;

    private ProgressDialog progressDialog;
    private final Handler handler = new Handler();

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

            QBUser user = new QBUser();
            user.setLogin(savedInstanceState.getString(USER_LOGIN_KEY));
            user.setPassword(savedInstanceState.getString(USER_PASSWORD_KEY));

            savedInstanceState.remove(USER_LOGIN_KEY);
            savedInstanceState.remove(USER_PASSWORD_KEY);

            recreateSession(user);
        }
    }

    private void recreateSession(final QBUser user){
        sessionActive = false;
        this.onStartSessionRecreation();

        showProgressDialog();

        // Restoring Chat session
        //
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

                Log.d(TAG, "Chat login onError: " + errors);

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error in the recreate session request, trying again in 3 seconds.. Check you internet connection.", Toast.LENGTH_SHORT);
                toast.show();

                // try again
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        recreateSession(user);
                    }
                }, 3000);

                BaseActivity.this.onFinishSessionRecreation(false);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        QBUser currentUser = ChatService.getInstance().getCurrentUser();
        if(currentUser != null) {
            outState.putString(USER_LOGIN_KEY, currentUser.getLogin());
            outState.putString(USER_PASSWORD_KEY, currentUser.getPassword());
        }

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
