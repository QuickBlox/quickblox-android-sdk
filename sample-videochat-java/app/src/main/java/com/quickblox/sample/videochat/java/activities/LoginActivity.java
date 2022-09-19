package com.quickblox.sample.videochat.java.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.java.App;
import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.services.LoginService;
import com.quickblox.sample.videochat.java.utils.Consts;
import com.quickblox.sample.videochat.java.utils.KeyboardUtils;
import com.quickblox.sample.videochat.java.utils.QBEntityCallbackImpl;
import com.quickblox.sample.videochat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.java.utils.ToastUtils;
import com.quickblox.sample.videochat.java.utils.ValidationUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class LoginActivity extends BaseActivity {
    private String TAG = LoginActivity.class.getSimpleName();

    private EditText userLoginEditText;
    private EditText userDisplayNameEditText;

    private QBUser currentUser;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();
    }

    private void initUI() {
        setActionBarTitle(R.string.title_login_activity);
        userLoginEditText = findViewById(R.id.user_login);
        userLoginEditText.addTextChangedListener(new LoginEditTextWatcher(userLoginEditText));

        userDisplayNameEditText = findViewById(R.id.user_display_name);
        userDisplayNameEditText.addTextChangedListener(new LoginEditTextWatcher(userDisplayNameEditText));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                boolean isNotValidLogin = !ValidationUtils.isLoginValid(userLoginEditText.getText().toString());
                if (isNotValidLogin) {
                    userLoginEditText.setError(getApplicationContext().getString(R.string.error_login));
                    return false;
                }

                boolean isNotValidDisplayName = !ValidationUtils.isDisplayNameValid(userDisplayNameEditText.getText().toString());
                if (isNotValidDisplayName) {
                    userDisplayNameEditText.setError(getApplicationContext().getString(R.string.error_display_name));
                    return false;
                }

                hideKeyboard();
                String login = userLoginEditText.getText().toString();
                String fullName = userDisplayNameEditText.getText().toString();
                currentUser = createCurrentUser(login, fullName);
                signUp(currentUser);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userLoginEditText);
        KeyboardUtils.hideKeyboard(userDisplayNameEditText);
    }

    private void signUp(final QBUser user) {
        Log.d(TAG, "SignUp New User");
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(user, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        Log.d(TAG, "SignUp Successful");
                        saveUser(user);
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "Error SignUp" + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signIn(user);
                        } else {
                            hideProgressDialog();
                            ToastUtils.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    private void loginToChat(final QBUser user) {
        user.setPassword(App.USER_DEFAULT_PASSWORD);
        currentUser = user;
        startLoginService(user);
    }

    private void saveUser(QBUser user) {
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.saveUser(user);
    }

    private QBUser createCurrentUser(String userLogin, String userFullName) {
        QBUser user = null;
        if (!TextUtils.isEmpty(userLogin) && !TextUtils.isEmpty(userFullName)) {
            user = new QBUser();
            user.setLogin(userLogin);
            user.setFullName(userFullName);
            user.setPassword(App.USER_DEFAULT_PASSWORD);
        }
        return user;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE) {
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess) {
                saveUser(currentUser);
                signIn(currentUser);
            } else {
                ToastUtils.longToast(getString(R.string.login_chat_login_error) + errorMessage);
                userLoginEditText.setText(currentUser.getLogin());
                userDisplayNameEditText.setText(currentUser.getFullName());
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void signIn(final QBUser user) {
        Log.d(TAG, "SignIn Started");
        requestExecutor.signInUser(user, new QBEntityCallbackImpl<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                Log.d(TAG, "SignIn Successful");
                sharedPrefsHelper.saveUser(currentUser);
                updateUserOnServer(user);
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "Error SignIn" + responseException.getMessage());
                hideProgressDialog();
                ToastUtils.longToast(R.string.sign_in_error);
            }
        });
    }

    private void updateUserOnServer(QBUser user) {
        user.setPassword(null);
        QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                hideProgressDialog();
                OpponentsActivity.start(LoginActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                ToastUtils.longToast(R.string.update_user_error);
            }
        });
    }

    private void startLoginService(QBUser qbUser) {
        Intent tempIntent = new Intent(this, LoginService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        LoginService.start(this, qbUser, pendingIntent);
    }

    private class LoginEditTextWatcher implements TextWatcher {
        private final EditText editText;

        private LoginEditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // empty
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
            // empty
        }
    }
}