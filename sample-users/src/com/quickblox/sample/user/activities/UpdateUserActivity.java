package com.quickblox.sample.user.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.result.QBUserResult;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.managers.QBManager;
import com.quickblox.sample.user.utils.DialogUtils;

import static com.quickblox.sample.user.definitions.Consts.EMPTY_STRING;

public class UpdateUserActivity extends BaseActivity implements QBCallback {

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
                // call query to update user
                QBManager.updateUser(DataHolder.getDataHolder().getSignInUserId(),
                        loginEditText.getText().toString(),
                        DataHolder.getDataHolder().getSignInUserOldPassword(),
                        passwordEditText.getText().toString(), fullNameEditText.getText().toString(),
                        emailEditText.getText().toString(), phoneEditText.getText().toString(),
                        webSiteEditText.getText().toString(), tagsEditText.getText().toString(), this,
                        QBQueries.QB_QUERY_UPDATE_QB_USER);
                break;
        }
    }

    @Override
    public void onComplete(Result result) {

    }

    @Override
    public void onComplete(Result result, Object data) {
        QBQueries qbQueryType = (QBQueries) data;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case QB_QUERY_UPDATE_QB_USER:
                    // return QBUserResult for updateUser query
                    QBUserResult qbUserResult = (QBUserResult) result;
                    DataHolder.getDataHolder().setSignInQbUser(qbUserResult.getUser());
                    if (!passwordEditText.equals(EMPTY_STRING)) {
                        DataHolder.getDataHolder().setSignInUserPassword(
                                passwordEditText.getText().toString());
                    }
                    DialogUtils.showLong(context, getResources().getString(
                            R.string.user_successfully_updated));
                    finish();
                    break;
            }
        } else {
            DialogUtils.showLong(context, result.getErrors().get(0));
        }
        progressDialog.hide();
    }
}