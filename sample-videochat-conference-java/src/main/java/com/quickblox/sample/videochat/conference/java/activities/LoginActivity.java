package com.quickblox.sample.videochat.conference.java.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.ErrorUtils;
import com.quickblox.sample.videochat.conference.java.utils.KeyboardUtils;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;
import com.quickblox.sample.videochat.conference.java.utils.ValidationUtils;
import com.quickblox.users.model.QBUser;

import androidx.annotation.Nullable;

public class LoginActivity extends BaseActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    private EditText userNameEditText;
    private EditText chatRoomNameEditText;

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
        setActionbarTitle(R.string.title_login_activity);
        userNameEditText = findViewById(R.id.user_name);
        userNameEditText.addTextChangedListener(new LoginEditTextWatcher(userNameEditText));

        chatRoomNameEditText = findViewById(R.id.chat_room_name);
        chatRoomNameEditText.addTextChangedListener(new LoginEditTextWatcher(chatRoomNameEditText));
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
                if (isEnteredUserNameValid() && isEnteredRoomNameValid()) {
                    hideKeyboard();
                    startSignUpNewUser(createUserWithEnteredData());
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isEnteredRoomNameValid() {
        return ValidationUtils.isRoomNameValid(this, chatRoomNameEditText);
    }

    private boolean isEnteredUserNameValid() {
        return ValidationUtils.isUserNameValid(this, userNameEditText);
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userNameEditText);
        KeyboardUtils.hideKeyboard(chatRoomNameEditText);
    }

    private void startSignUpNewUser(final QBUser newUser) {
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser user, Bundle params) {
                        Log.d(TAG, "SignUp Successful");
                        saveUserData(newUser);
                        signInCreatedUser(newUser);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Log.d(TAG, "SignUp Error" + e.getMessage());
                        if (e.getHttpStatusCode() == Consts.ERR_LOGIN_ALREADY_TAKEN_HTTP_STATUS) {
                            signInCreatedUser(newUser);
                        } else {
                            hideProgressDialog();
                            ToastUtils.longToast(R.string.sign_up_error);
                        }
                    }
                }
        );
    }

    private void saveUserData(QBUser qbUser) {
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private QBUser createUserWithEnteredData() {
        String userName = userNameEditText.getText().toString();
        String chatRoomName = chatRoomNameEditText.getText().toString();
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(chatRoomName)) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(chatRoomName);

            qbUser = new QBUser();
            qbUser.setFullName(userName);
            qbUser.setLogin(userName);
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }

        return qbUser;
    }

    private void signInCreatedUser(final QBUser user) {
        requestExecutor.signInUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle params) {
                Log.d(TAG, "SignIn Successful");
                StringifyArrayList<String> tags = new StringifyArrayList<>();
                tags.add(chatRoomNameEditText.getText().toString());
                if (user.getTags().equals(tags)) {
                    user.setPassword(Consts.DEFAULT_USER_PASSWORD);
                    saveUserData(user);
                    hideProgressDialog();
                    DialogsActivity.start(LoginActivity.this);
                    finish();
                } else {
                    user.setTags(tags);
                    updateUser(user);
                }
            }

            @Override
            public void onError(QBResponseException responseException) {
                Log.d(TAG, "SignIn Error");
                hideProgressDialog();
                ToastUtils.longToast(R.string.sign_up_error);
            }
        });
    }

    private void updateUser(QBUser user) {
        // Hack, because the server isn't update model QBUser with password != null;
        user.setPassword(null);

        requestExecutor.updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "User Updated");
                user.setPassword(Consts.DEFAULT_USER_PASSWORD);
                saveUserData(user);
                hideProgressDialog();
                DialogsActivity.start(LoginActivity.this);
                finish();
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "Error Update User");
                ErrorUtils.showSnackbar(getWindow().getDecorView().findViewById(android.R.id.content),
                        R.string.sign_up_error, R.string.dialog_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateUser(user);
                            }
                        });
            }
        });
    }

    private class LoginEditTextWatcher implements TextWatcher {
        private EditText editText;

        private LoginEditTextWatcher(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            editText.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}