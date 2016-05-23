package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.sample.core.utils.DeviceUtils;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.App;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.Consts;
import com.quickblox.sample.groupchatwebrtc.services.CallService;
import com.quickblox.sample.groupchatwebrtc.util.QBResRequestExecutor;
import com.quickblox.users.model.QBUser;

/**
 * Created by tereha on 12.04.16.
 */
public class LoginActivity extends BaseActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    private EditText userNameEditText;
    private EditText chatRoomNameEditText;

    private QBResRequestExecutor requestExecutor;
    private QBUser userForSave;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        requestExecutor = App.getInstance().getQbResRequestExecutor();

        if (sharedPrefsHelper.hasQbUser()){
            startWithRestoredUser();
        }

        initUI();
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.root_view_login_activity);
    }

    private void startWithRestoredUser() {
        QBUser restoredUser = sharedPrefsHelper.getQbUser();
        signInToQB(restoredUser, false);
    }

    private void initUI() {
        setActionBarTitle(R.string.title_login_activity);
        userNameEditText = (EditText) findViewById(R.id.user_name);
        chatRoomNameEditText = (EditText) findViewById(R.id.chat_room_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                if (isEnteredRoomNameValid() && isEnteredUserNameValid()) {
                    hideKeyboard();
                    signInToQB(createUserWithEnteredData(), true);
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signInToQB(final QBUser currentQbUser, final boolean isFirstSignin) {
        showProgressDialog(R.string.dlg_sign_in);
        requestExecutor.signIn(currentQbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                hideProgressDialog();

                if (isFirstSignin) {
                    processSigninedUser(qbUser);
                } else {
                    loginToChat(qbUser);
                }
            }

            @Override
            public void onError(QBResponseException e) {
                String errorMessage = e.getMessage();
                Log.d(TAG, errorMessage != null ? errorMessage : getString(R.string.sign_in_error_without_error));
                hideProgressDialog();
                if (Consts.UNAUTHORIZED_ERROR_CODE == e.getHttpStatusCode()) {
                    startSignUpNewUser(currentQbUser);
                } else {
                    showErrorSnackbar(R.string.sign_in_error_with_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            signInToQB(currentQbUser, isFirstSignin);
                        }
                    });
                }
            }
        });
    }

    private boolean isEnteredRoomNameValid() {
        //TODO VT need to add advanced validator for release
        if (TextUtils.isEmpty(chatRoomNameEditText.getText())) {
            chatRoomNameEditText.setError(getString(R.string.error_empty_chat_room_name));
            return false;
        }

        return true;
    }

    private boolean isEnteredUserNameValid() {
        //TODO VT need to add advanced validator for release
        if (TextUtils.isEmpty(userNameEditText.getText())) {
            userNameEditText.setError(getString(R.string.error_empty_login));
            return false;
        }

        return true;
    }

    private void hideKeyboard() {
        KeyboardUtils.hideKeyboard(userNameEditText);
        KeyboardUtils.hideKeyboard(chatRoomNameEditText);
    }

    private void processSigninedUser(final QBUser qbUserFromServer){
        if (!isNeedUpdateUser(qbUserFromServer)){
            loginToChat(qbUserFromServer);
        } else {
            QBUser userForUpdate = createUserWithEnteredData();
            userForUpdate.setId(qbUserFromServer.getId());
            startUpdateUser(userForUpdate);
        }
    }

    private void startUpdateUser(final QBUser qbUser) {
        showProgressDialog(R.string.dlg_updating_user);
        qbUser.setOldPassword(Consts.DEFAULT_USER_PASSWORD);
        requestExecutor.updateUserOnQBServer(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                hideProgressDialog();
                loginToChat(qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                hideProgressDialog();
                showErrorSnackbar(R.string.update_user_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startUpdateUser(qbUser);
                    }
                });
            }
        });
    }

    private void startSignUpNewUser(QBUser newUser){
        showProgressDialog(R.string.dlg_creating_new_user);
        requestExecutor.signUpNewUser(newUser, new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser result, Bundle params) {
                        hideProgressDialog();
                        loginToChat(result);
                    }

                    @Override
                    public void onError(QBResponseException responseException) {
                        hideProgressDialog();
                        Toaster.longToast(R.string.sign_up_error);
                    }
                }
        );
    }

    private boolean isNeedUpdateUser(QBUser qbUserFromServer){
        if (qbUserFromServer.getTags() == null || qbUserFromServer.getFullName() == null){
            return true;
        }

        QBUser currentUser = createUserWithEnteredData();

        boolean needUpdateUser = currentUser != null && (!qbUserFromServer.getTags().contains(currentUser.getTags().get(0))
                || !qbUserFromServer.getFullName().equals(currentUser.getFullName()));

        return needUpdateUser;
    }

    private void loginToChat(final QBUser qbUser) {
        showProgressDialog(R.string.dlg_login);

        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);

        userForSave = qbUser;
        startLoginService(qbUser);
    }

    private void startOpponentsActivity() {
        OpponentsActivity.start(LoginActivity.this);
        finish();
    }

    private void saveUserData(QBUser qbUser){
        SharedPrefsHelper sharedPrefsHelper = SharedPrefsHelper.getInstance();
        sharedPrefsHelper.save(Consts.PREF_CURREN_ROOM_NAME, qbUser.getTags().get(0));
        sharedPrefsHelper.saveQbUser(qbUser);
    }

    private QBUser createUserWithEnteredData(){
        return createQBUserWithCurrentData(String.valueOf(userNameEditText.getText()),
                String.valueOf(chatRoomNameEditText.getText()));
    }

    private QBUser createQBUserWithCurrentData(String userName, String chatRoomName){
        QBUser qbUser = null;
        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(chatRoomName)) {
            StringifyArrayList<String> userTags = new StringifyArrayList<>();
            userTags.add(String.valueOf(chatRoomName));

            qbUser = new QBUser();
            qbUser.setFullName(String.valueOf(userName));
            qbUser.setLogin(DeviceUtils.getDeviceUid());
            qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
            qbUser.setTags(userTags);
        }

        return qbUser;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.EXTRA_LOGIN_RESULT_CODE){
            hideProgressDialog();
            boolean isLoginSuccess = data.getBooleanExtra(Consts.EXTRA_LOGIN_RESULT, false);
            String errorMessage = data.getStringExtra(Consts.EXTRA_LOGIN_ERROR_MESSAGE);

            if (isLoginSuccess){
                saveUserData(userForSave);
                startOpponentsActivity();
            } else {
                Toaster.longToast(getString(R.string.login_chat_login_error) + errorMessage);
                userNameEditText.setText(userForSave.getFullName());
                chatRoomNameEditText.setText(userForSave.getTags().get(0));
            }

        }
    }

    private void startLoginService(QBUser qbUser){
        Intent tempIntent = new Intent(this, CallService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.EXTRA_LOGIN_RESULT_CODE, tempIntent, 0);
        CallService.start(this, qbUser, pendingIntent);
    }
}
