package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.utils.DialogUtils;

import java.util.List;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

public class UpdateUserActivity extends BaseActivity {

    private EditText loginEditText;
    private EditText passwordEditText;
    private EditText emailEditText;
    private EditText fullNameEditText;
    private EditText phoneEditText;
    private EditText webSiteEditText;
    private EditText tagsEditText;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.activity_update_user);

        initUI();
        fillAllFields();
    }

    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        loginEditText = (EditText) findViewById(R.id.login_edittext);
        passwordEditText = (EditText) findViewById(R.id.password_edittext);
        emailEditText = (EditText) findViewById(R.id.email_edittext);
        fullNameEditText = (EditText) findViewById(R.id.full_name_edittext);
        phoneEditText = (EditText) findViewById(R.id.phone_edittext);
        webSiteEditText = (EditText) findViewById(R.id.web_site_edittext);
        tagsEditText = (EditText) findViewById(R.id.tags_edittext);
    }

    private void fillAllFields() {
        fillField(loginEditText, DataHolder.getDataHolder().getSignInUserLogin());
        fillField(emailEditText, DataHolder.getDataHolder().getSignInUserEmail());
        fillField(fullNameEditText, DataHolder.getDataHolder().getSignInUserFullName());
        fillField(phoneEditText, DataHolder.getDataHolder().getSignInUserPhone());
        fillField(webSiteEditText, DataHolder.getDataHolder().getSignInUserWebSite());
        fillField(tagsEditText, DataHolder.getDataHolder().getSignInUserTags());
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_button:
                progressDialog.show();

                // Update user
                //
                // create QBUser object
                QBUser qbUser = new QBUser();
                if (DataHolder.getDataHolder().getSignInUserId() != -1) {
                    qbUser.setId(DataHolder.getDataHolder().getSignInUserId());
                }
                if (!DataHolder.getDataHolder().getSignInUserLogin().equals(loginEditText.getText().toString())) {
                    qbUser.setLogin(loginEditText.getText().toString());
                }
                if (!passwordEditText.getText().toString().equals(EMPTY_STRING)) {
                    qbUser.setPassword(passwordEditText.getText().toString());
                    qbUser.setOldPassword(DataHolder.getDataHolder().getSignInUserOldPassword());
                }
                qbUser.setFullName(fullNameEditText.getText().toString());
                qbUser.setEmail(emailEditText.getText().toString());
                qbUser.setPhone(phoneEditText.getText().toString());
                qbUser.setWebsite(webSiteEditText.getText().toString());
                StringifyArrayList<String> tagList = new StringifyArrayList<String>();
                for (String tag : tagsEditText.getText().toString().toString().split(",")) {
                    tagList.add(tag);
                }
                qbUser.setTags(tagList);

                QBUsers.updateUser(qbUser, new QBEntityCallbackImpl<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {

                        DataHolder.getDataHolder().setSignInQbUser(qbUser);
                        if (!passwordEditText.equals(EMPTY_STRING)) {
                            DataHolder.getDataHolder().setSignInUserPassword(
                                    passwordEditText.getText().toString());
                        }
                        DialogUtils.showLong(context, getResources().getString(
                                R.string.user_successfully_updated));
                        finish();
                    }

                    @Override
                    public void onError(List<String> strings) {

                    }
                });

                break;
        }
    }
}