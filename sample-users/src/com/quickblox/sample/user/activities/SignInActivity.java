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

public class SignInActivity extends BaseActivity implements QBCallback {

    private EditText loginEditText;
    private EditText passwordEditText;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_sign_in);
        initUI();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                progressDialog.show();
                // Sign in application with user.
                // You can create user on admin.quickblox.com, Users module or through QBUsers.signUp method
                QBManager.singIn(loginEditText.getText().toString(), passwordEditText.getText().toString(), this, QBQueries.QB_QUERY_SIGN_IN_QB_USER);
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
                case QB_QUERY_SIGN_IN_QB_USER:
                    setResult(RESULT_OK);
                    // return QBUserResult for singIn query
                    QBUserResult qbUserResult = (QBUserResult) result;
                    DataHolder.getDataHolder().setSignInQbUser(qbUserResult.getUser());
                    // password does not come, so if you want use it somewhere else, try something like this:
                    DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());
                    DialogUtils.showLong(context, getResources().getString(R.string.user_successfully_sign_in));
                    finish();
                    break;
            }
        } else {
            DialogUtils.showLong(context, result.getErrors().get(0));
        }
        progressDialog.hide();
    }
}