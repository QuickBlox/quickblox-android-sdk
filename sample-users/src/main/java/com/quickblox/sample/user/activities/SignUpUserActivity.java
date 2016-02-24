package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class SignUpUserActivity extends BaseActivity {

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

    public void signUp() {
        progressDialog.show();

        // Sign Up user
        //
        QBUser qbUser = new QBUser();
        qbUser.setLogin(loginEditText.getText().toString());
        qbUser.setPassword(passwordEditText.getText().toString());
        QBUsers.signUpSignInTask(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                progressDialog.hide();

                DataHolder.getDataHolder().addQbUserToList(qbUser);
                DataHolder.getDataHolder().setSignInQbUser(qbUser);
                DataHolder.getDataHolder().setSignInUserPassword(passwordEditText.getText().toString());

                finish();
            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.hide();

                Toaster.longToast(error.getErrors().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sign_in_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_in_up:
                signUp();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}