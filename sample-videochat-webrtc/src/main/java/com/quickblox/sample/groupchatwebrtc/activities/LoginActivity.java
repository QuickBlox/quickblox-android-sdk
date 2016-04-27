package com.quickblox.sample.groupchatwebrtc.activities;

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
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.DeviceUtils;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.sample.groupchatwebrtc.util.QBRestUtils;
import com.quickblox.users.model.QBUser;

/**
 * Created by tereha on 12.04.16.
 */
public class LoginActivity extends BaseLogginedUserActivity {

    private EditText userNameEditText;
    private EditText chatRoomNameEditText;
    private String TAG = LoginActivity.class.getSimpleName();

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
        setActionbarTitle(getResources().getString(R.string.title_login_activity));
        userNameEditText = (EditText) findViewById(R.id.user_name);
        chatRoomNameEditText = (EditText) findViewById(R.id.chat_room_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO VT need to add advanced validator for release
        if (TextUtils.isEmpty(userNameEditText.getText())) {
            userNameEditText.setError(getString(R.string.error_empty_login));
            return super.onOptionsItemSelected(item);
        }

        if (TextUtils.isEmpty(chatRoomNameEditText.getText())) {
            chatRoomNameEditText.setError(getString(R.string.error_empty_chat_room_name));
            return super.onOptionsItemSelected(item);
        }
        //TODO end

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                KeyboardUtils.hideKeyboard(userNameEditText);
                KeyboardUtils.hideKeyboard(chatRoomNameEditText);
                signInToQB(new QBUser(DeviceUtils.getDeviceUid(), Consts.DEFAULT_USER_PASSWORD));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signInToQB(final QBUser currentQbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_sign_in);
        QBRestUtils.getInstance().signIn(currentQbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                processSigninedUser(qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                if (e.getMessage().equals("Unauthorized")){
                    QBRestUtils.getInstance().signUpNewUser(currentQbUser);
                } else {
                    ErrorUtils.showSnackbar(getCurrentFocus(), R.string.sign_in_error, e,
                            R.string.dlg_retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    signInToQB(currentQbUser);
                                }
                            });
                }
            }
        });
    }

    private void processSigninedUser(final QBUser qbUserFromServer){
        if (isUserCorrect(qbUserFromServer)){
            loginToChat(qbUserFromServer);
        } else {
            QBUser userForUpdate = createUserWithEnteredData();
            userForUpdate.setId(qbUserFromServer.getId());
            startUpdateUser(userForUpdate);
        }
    }

    private void startUpdateUser(final QBUser qbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_updating_user);
        QBRestUtils.getInstance().updateUserOnQBServer(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                loginToChat(qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                ErrorUtils.showSnackbar(getCurrentFocus(), R.string.update_user_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startUpdateUser(qbUser);
                            }
                        });
            }
        });
    }

    private boolean isUserCorrect(QBUser qbUserFromServer){
        if (qbUserFromServer.getTags() == null || qbUserFromServer.getFullName() == null){
            return false;
        }

        QBUser currentUser = createUserWithEnteredData();

        boolean isCurrentUserCorrect = currentUser != null && qbUserFromServer.getTags().contains(currentUser.getTags().get(0))
                && qbUserFromServer.getFullName().equals(currentUser.getFullName());

        return isCurrentUserCorrect;
    }

    //TODO этот метод я переименую согласной той логики, кторую он будет выполнять
    private void loginToChat(final QBUser qbUser){
        //TODO здесь будет логика запуска сервиса с логином в чат и переходом на следующее активити
        Log.d(TAG, "success signIn to QB");
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
}
