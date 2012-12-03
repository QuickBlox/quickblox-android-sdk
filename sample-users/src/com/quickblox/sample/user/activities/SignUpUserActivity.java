package com.quickblox.sample.user.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.managers.QBManager;

/**
 * Created with IntelliJ IDEA.
 * User: android
 * Date: 20.11.12
 * Time: 15:30
 */
public class SignUpUserActivity extends Activity implements QBCallback {

    EditText login;
    EditText password;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        initialize();
    }

    private void initialize() {
        login = (EditText) findViewById(R.id.login);
        password = (EditText) findViewById(R.id.password);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.please_wait));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_up_btn:
                progressDialog.show();
                // call singUp query
                QBManager.signUp(login.getText().toString(), password.getText().toString(), this, QBQueries.QB_QUERY_CREATE_QB_USER);
                break;
        }
    }

    @Override
    public void onComplete(Result result) {
    }

    @Override
    public void onComplete(Result result, Object query) {
        QBQueries qbQueryType = (QBQueries) query;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case QB_QUERY_CREATE_QB_USER:
                    //Result for QBUsers.signUp() ----> QBUserResult
                    DataHolder.getDataHolder().addQbUserToList(((QBUserResult) result).getUser());
                    // after You sign up user, You must sign in by this user
                    QBManager.singIn(DataHolder.getDataHolder().getLastQBUser().getLogin(), password.getText().toString(), this, QBQueries.QB_QUERY_SIGN_IN_QB_USER);
                    break;
                case QB_QUERY_SIGN_IN_QB_USER:
                    // return QBUserResult for query signIn()
                    QBUserResult qbUserResult = (QBUserResult) result;
                    DataHolder.getDataHolder().setSignInQbUser(qbUserResult.getUser());
                    // password does not come, so if you want use it somewhere else, try something like this:
                    DataHolder.getDataHolder().setSignInUserPassword(password.getText().toString());
                    finish();
                    break;
            }
        } else {
            // print errors that came from server
            Toast.makeText(getBaseContext(), result.getErrors().get(0), Toast.LENGTH_SHORT).show();
        }
        progressDialog.hide();
    }
}
