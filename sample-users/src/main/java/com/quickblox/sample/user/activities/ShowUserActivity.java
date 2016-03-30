package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.databinding.ActivityShowUserBinding;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class ShowUserActivity extends BaseActivity {

    private static final String QB_USER = "qbUser";

    private QBUser qbUser;
    private boolean currentUserSignIn;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText phoneNumberEditText;
    private EditText tagsEditText;

    private ActivityShowUserBinding showUserBinding;

    public static void start(Context context, QBUser qbUser) {
        Intent intent = new Intent(context, ShowUserActivity.class);
        intent.putExtra(QB_USER, qbUser);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        initUI();

        qbUser = (QBUser) getIntent().getSerializableExtra(QB_USER);

        //TODO userFull sounds a little bit strange, why is it full ? Name it just User.
        showUserBinding.setUserFull(qbUser);

        setFieldsFocusable(qbUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sign_in_up, menu);
        menu.getItem(0).setVisible(currentUserSignIn);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_in_up:
                updateProfile();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO seems like this method can be moved to base activity, it appears almost in each activity
    private void initUI() {
        actionBar.setDisplayHomeAsUpEnabled(true);
        showUserBinding = DataBindingUtil.setContentView(this, R.layout.activity_show_user);

        fullNameEditText = _findViewById(R.id.full_name_textview);
        emailEditText = _findViewById(R.id.email_textview);
        phoneNumberEditText = _findViewById(R.id.phone_textview);
        tagsEditText = _findViewById(R.id.tag_textview);
    }

    private void setFieldsFocusable(QBUser qbUser) {
        if (DataHolder.getInstance().isSignedIn()) {
            currentUserSignIn = DataHolder.getInstance().getSignInQbUser().equals(qbUser);
        }
        //TODO it's better to use setEnabled method to enable/disable view for editing
        fullNameEditText.setFocusableInTouchMode(currentUserSignIn);
        emailEditText.setFocusableInTouchMode(currentUserSignIn);
        phoneNumberEditText.setFocusableInTouchMode(currentUserSignIn);
        tagsEditText.setFocusableInTouchMode(currentUserSignIn);
    }

    private void updateProfile() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        String tags = tagsEditText.getText().toString().trim();

        StringifyArrayList<String> tagsArray = new StringifyArrayList<>();
        tagsArray.add(tags);

        //TODO I think if some editText is empty it isn't necessary to update this field in user
        qbUser.setFullName(fullName);
        qbUser.setEmail(email);
        qbUser.setPhone(phoneNumber);
        qbUser.setTags(tagsArray);

        QBUsers.updateUser(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser user, Bundle args) {

                int location = DataHolder.getInstance().getQBUsers().indexOf(user);
                DataHolder.getInstance().setQbUser(location, user);

                Toaster.shortToast(R.string.update);
                progressDialog.dismiss();

                finish();
            }

            @Override
            public void onError(QBResponseException errors) {
                progressDialog.dismiss();
                View rootLayout = findViewById(R.id.activity_show_user);
                showSnackbarError(rootLayout, R.string.errors, errors, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateProfile();
                    }
                });
            }
        });
        progressDialog.show();
    }
}