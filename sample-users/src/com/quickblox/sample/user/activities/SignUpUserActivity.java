package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.managers.QBManager;
import com.quickblox.sample.user.utils.DialogUtils;

public class SignUpUserActivity extends BaseActivity implements QBCallback {

    private EditText loginEditText;
    private EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_up_button:
                progressDialog.show();
                // call singUp query
                QBManager.signUp(loginEditText.getText().toString(), passwordEditText.getText().toString(),
                        this, QBQueries.QB_QUERY_CREATE_QB_USER);
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
                    QBManager.singIn(DataHolder.getDataHolder().getLastQBUser().getLogin(), passwordEditText.getText().toString(), this, QBQueries.QB_QUERY_SIGN_IN_QB_USER);
                    break;
                case QB_QUERY_SIGN_IN_QB_USER:
                    // return QBUserResult for query signIn()
                    QBUserResult qbUserResult = (QBUserResult) result;
                    DataHolder.getDataHolder().setSignInQbUser(qbUserResult.getUser());
                    // password does not come, so if you want use it somewhere else, try something like this:
                    DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());
                    finish();
                    break;
            }
        } else {
            // print errors that came from server
            DialogUtils.showLong(context, result.getErrors().get(0));
        }
        progressDialog.hide();
    }
}