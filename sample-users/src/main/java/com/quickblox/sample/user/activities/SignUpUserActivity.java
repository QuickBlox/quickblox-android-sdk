package com.quickblox.sample.user.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

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
    private EditText confirmPasswordEditText;
    private Toast toast;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initUI();
    }

    @SuppressLint("ShowToast")
    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);

        toast = Toast.makeText(this, R.string.error, Toast.LENGTH_LONG);
        loginEditText = _findViewById(R.id.login_up_edittext);
        passwordEditText = _findViewById(R.id.password_up_edittext);
        confirmPasswordEditText = _findViewById(R.id.password_confirm_edittext);
    }

    public void signUp() {
        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        if (!isValidData(login, password, confirmPassword)) {
            return;
        }

        progressDialog.show();

        QBUser qbUser = new QBUser();
        qbUser.setLogin(login);
        qbUser.setPassword(password);
        QBUsers.signUpSignInTask(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                progressDialog.hide();

                DataHolder.getInstance().addQbUser(qbUser);
                DataHolder.getInstance().setSignInQbUser(qbUser);

                finish();
            }

            @Override
            public void onError(QBResponseException error) {
                progressDialog.hide();

                Toaster.longToast(error.getErrors().toString());
            }
        });
    }

    private boolean isValidData(String login, String password, String confirm) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            toast.setText(R.string.error_field_is_empty);
            toast.show();
            return false;
        }
        if (!TextUtils.equals(password, confirm)) {
            toast.setText(R.string.confirm_error);
            toast.show();
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast.cancel();
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