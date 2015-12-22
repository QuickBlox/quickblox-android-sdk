package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.List;

public class SignInActivity extends BaseActivity {

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

                // Sign in application with user
                //
                QBUser qbUser = new QBUser(loginEditText.getText().toString(), passwordEditText.getText().toString());
                QBUsers.signIn(qbUser, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        progressDialog.hide();

                        setResult(RESULT_OK);

                        DataHolder.getDataHolder().setSignInQbUser(qbUser);
                        // password does not come, so if you want use it somewhere else, try something like this:
                        DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());
                        Toaster.longToast(R.string.user_successfully_sign_in);

                        finish();
                    }

                    @Override
                    public void onError(List<String> errors) {
                        progressDialog.hide();
                        Toaster.longToast(errors.get(0));
                    }
                });

                break;
        }
    }
}