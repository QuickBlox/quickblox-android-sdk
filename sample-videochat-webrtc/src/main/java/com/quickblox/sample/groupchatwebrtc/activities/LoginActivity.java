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

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.DeviceUtils;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.KeyboardUtils;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.definitions.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

/**
 * Created by tereha on 12.04.16.
 */
public class LoginActivity extends BaseLogginedUserActivity {

    private EditText usetNameEditText;
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
        usetNameEditText = (EditText) findViewById(R.id.user_name);
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
        if (TextUtils.isEmpty(usetNameEditText.getText())) {
            usetNameEditText.setError("Login can not be empty");
            return super.onOptionsItemSelected(item);
        }

        if (TextUtils.isEmpty(chatRoomNameEditText.getText())) {
            chatRoomNameEditText.setError("Chat room name can not be empty");
            return super.onOptionsItemSelected(item);
        }

        //TODO end

        switch (item.getItemId()) {
            case R.id.menu_login_user_done:
                KeyboardUtils.hideKeyboard(usetNameEditText);
                KeyboardUtils.hideKeyboard(chatRoomNameEditText);
                signInToQB(new QBUser(DeviceUtils.getDeviceUid(), Consts.DEFAULT_USER_PASSWORD));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signInToQB(final QBUser currentQbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_sign_in);
        QBUsers.signIn(currentQbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                updateUserIfNeed(qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                if (e.getMessage().equals("Unauthorized")){
                    signUpNewUser(currentQbUser);
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

    private void updateUserIfNeed(final QBUser qbUser){
        QBUser currentUser = getCurrentUser();
        if (qbUser.getTags().contains(chatRoomNameEditText.getText())
                && qbUser.getFullName().equals(currentUser.getFullName())){
            loginToChat(qbUser);
        } else {
            startUpdateUser(qbUser);
        }
    }

    private void startUpdateUser(final QBUser qbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_updating_user);
        qbUser.setOldPassword(Consts.DEFAULT_USER_PASSWORD);
        QBUsers.updateUser(qbUser, new QBEntityCallback<QBUser>() {
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

    private void signUpNewUser(final QBUser newQbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_creating_new_user);
        QBUsers.signUp(newQbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
//                newQbUser.setId(qbUser.getId());
                loginToChat(qbUser);
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                Log.d(TAG, e.getMessage());
            }
        });
    }

    private void loginToChat(final QBUser qbUser){
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_login);
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        QBChatService.getInstance().login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser loginedQbUser, Bundle bundle) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                startCallActivity(qbUser.getLogin());
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, e.getMessage());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                ErrorUtils.showSnackbar(getCurrentFocus(), R.string.login_chat_login_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loginToChat(qbUser);
                            }
                        });
            }
        });

    }

    private QBUser getCurrentUser(){
        StringifyArrayList <String> userTags = new StringifyArrayList<>();
        userTags.add(String.valueOf(chatRoomNameEditText.getText()));

        QBUser qbUser = new QBUser();
        qbUser.setFullName(String.valueOf(usetNameEditText.getText()));
        qbUser.setLogin(DeviceUtils.getDeviceUid());
        qbUser.setPassword(Consts.DEFAULT_USER_PASSWORD);
        qbUser.setTags(userTags);

        return qbUser;
    }

    private void startCallActivity(String login) {
        Intent intent = new Intent(LoginActivity.this, CallActivity.class);
        intent.putExtra("login", login);
        intent.putExtra(Consts.EXTRA_TAG, chatRoomNameEditText.getText());
        startActivityForResult(intent, Consts.CALL_ACTIVITY_CLOSE);
        finish();
    }


}
