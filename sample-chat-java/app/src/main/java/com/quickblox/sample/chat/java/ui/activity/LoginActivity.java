package com.quickblox.sample.chat.java.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.SharedPrefsHelper;
import com.quickblox.sample.chat.java.utils.ValidationUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Timer;
import java.util.TimerTask;

public class LoginActivity extends BaseActivity {
    private static final int UNAUTHORIZED = 401;
    private static final String DRAFT_LOGIN = "draft_login";
    private static final String DRAFT_USERNAME = "draft_username";

    private EditText loginEt;
    private EditText usernameEt;
    private TextView loginHint;
    private TextView usernameHint;
    private TextView btnLogin;
    private CheckBox chbSave;
    private LinearLayout rootView;
    private LinearLayout hidableHolder;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        prepareListeners();
        fillViews();
        defineFocusedBehavior();
    }

    private void initViews() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(20f);
        }
        loginHint = findViewById(R.id.tv_login_hint);
        usernameHint = findViewById(R.id.tv_username_hint);
        btnLogin = findViewById(R.id.tv_btn_login);
        loginEt = findViewById(R.id.et_login);
        usernameEt = findViewById(R.id.et_user_name);
        chbSave = findViewById(R.id.chb_login_save);
        btnLogin = findViewById(R.id.tv_btn_login);
        hidableHolder = findViewById(R.id.ll_hidable_holder);
        rootView = findViewById(R.id.root_view_login_activity);
    }

    private void fillViews() {
        String draftLogin = SharedPrefsHelper.getInstance().get(DRAFT_LOGIN, null);
        String draftUserName = SharedPrefsHelper.getInstance().get(DRAFT_USERNAME, null);

        if (!TextUtils.isEmpty(draftLogin)) {
            loginEt.setText(draftLogin);
        }
        if (!TextUtils.isEmpty(draftUserName)) {
            usernameEt.setText(draftUserName);
        }

        validateFields();
    }

    private void defineFocusedBehavior() {
        loginHint.setVisibility(View.GONE);
        usernameHint.setVisibility(View.GONE);

        loginEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (hasFocus) {
                        loginEt.setTranslationZ(10f);
                    } else {
                        loginEt.setTranslationZ(0f);
                    }
                }
                if (ValidationUtils.isLoginValid(LoginActivity.this, loginEt)) {
                    loginHint.setVisibility(View.GONE);
                } else {
                    loginHint.setVisibility(View.VISIBLE);
                }
            }
        });

        usernameEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (hasFocus) {
                        usernameEt.setTranslationZ(10f);
                    } else {
                        usernameEt.setTranslationZ(0f);
                    }
                }
                if (ValidationUtils.isLoginValid(LoginActivity.this, usernameEt)) {
                    usernameHint.setVisibility(View.GONE);
                } else {
                    usernameHint.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void prepareListeners() {
        rootView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                chbSave.setChecked(!chbSave.isChecked());

                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (chbSave.isChecked()) {
                    if (vibrator != null) {
                        vibrator.vibrate(80);
                    }
                } else {
                    if (vibrator != null) {
                        vibrator.vibrate(250);
                    }
                }
                return true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnLogin.isActivated()) {
                    showProgressDialog(R.string.dlg_login);
                    prepareUser();
                }
            }
        });

        loginEt.addTextChangedListener(new TextWatcherListener(loginEt));
        usernameEt.addTextChangedListener(new TextWatcherListener(usernameEt));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_login_app_info:
                AppInfoActivity.start(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Boolean validateFields() {
        Boolean loginValid = ValidationUtils.isLoginValid(this, loginEt);
        Boolean userNameValid = ValidationUtils.isFullNameValid(this, usernameEt);

        if (loginValid) {
            loginHint.setVisibility(View.GONE);
        } else {
            loginHint.setVisibility(View.VISIBLE);
        }

        if (userNameValid) {
            usernameHint.setVisibility(View.GONE);
        } else {
            usernameHint.setVisibility(View.VISIBLE);
        }

        if (loginValid && userNameValid) {
            btnLogin.setActivated(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.setElevation(0F);
                btnLogin.setTranslationZ(10F);
            }
            return true;
        } else {
            btnLogin.setActivated(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btnLogin.setElevation(0F);
                btnLogin.setTranslationZ(0F);
            }
            return false;
        }
    }

    private void saveDrafts() {
        SharedPrefsHelper.getInstance().save(DRAFT_LOGIN, loginEt.getText().toString());
        SharedPrefsHelper.getInstance().save(DRAFT_USERNAME, usernameEt.getText().toString());
    }

    private void clearDrafts() {
        SharedPrefsHelper.getInstance().save(DRAFT_LOGIN, "");
        SharedPrefsHelper.getInstance().save(DRAFT_USERNAME, "");
    }

    private void prepareUser() {
        QBUser qbUser = new QBUser();
        qbUser.setLogin(loginEt.getText().toString().trim());
        qbUser.setFullName(usernameEt.getText().toString().trim());
        qbUser.setPassword(App.USER_DEFAULT_PASSWORD);
        signIn(qbUser);
    }

    private void signIn(final QBUser user) {
        showProgressDialog(R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser userFromRest, Bundle bundle) {
                if (userFromRest.getFullName().equals(user.getFullName())) {
                    loginToChat(user);
                } else {
                    //Need to set password NULL, because server will update user only with NULL password
                    user.setPassword(null);
                    updateUser(user);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                if (e.getHttpStatusCode() == UNAUTHORIZED) {
                    signUp(user);
                } else {
                    hideProgressDialog();
                    showErrorSnackbar(R.string.login_chat_login_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signIn(user);
                        }
                    });
                }
            }
        });
    }

    private void updateUser(final QBUser user) {
        ChatHelper.getInstance().updateUser(user, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                loginToChat(user);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void loginToChat(final QBUser user) {
        //Need to set password, because the server will not register to chat without password
        user.setPassword(App.USER_DEFAULT_PASSWORD);
        ChatHelper.getInstance().loginToChat(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                if (!chbSave.isChecked()) {
                    clearDrafts();
                }
                DialogsActivity.start(LoginActivity.this);
                finish();
                hideProgressDialog();
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_chat_login_error, e, null);
            }
        });
    }

    private void signUp(final QBUser newUser) {
        SharedPrefsHelper.getInstance().removeQbUser();
        QBUsers.signUp(newUser).performAsync(new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle bundle) {
                hideProgressDialog();
                signIn(newUser);
            }

            @Override
            public void onError(QBResponseException e) {
                hideProgressDialog();
                showErrorSnackbar(R.string.login_sign_up_error, e, null);
            }
        });
    }

    private class TextWatcherListener implements TextWatcher {
        private EditText editText;
        private Timer timer = new Timer();

        private TextWatcherListener(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().replace("  ", " ");
            if (!editText.getText().toString().equals(text)) {
                editText.setText(text);
                editText.setSelection(text.length());
            }
            validateFields();
        }

        @Override
        public void afterTextChanged(Editable s) {
            timer.cancel();
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    saveDrafts();
                }
            }, 300);
        }
    }
}