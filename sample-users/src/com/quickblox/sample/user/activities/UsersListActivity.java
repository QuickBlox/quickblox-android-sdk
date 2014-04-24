package com.quickblox.sample.user.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.adapter.UserListAdapter;
import com.quickblox.sample.user.definitions.QBQueries;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.sample.user.managers.QBManager;
import com.quickblox.sample.user.utils.DialogUtils;

import static com.quickblox.sample.user.definitions.Consts.POSITION;

public class UsersListActivity extends BaseActivity implements QBCallback, AdapterView.OnItemClickListener {

    private UserListAdapter usersListAdapter;
    private ListView usersList;
    private Button logOutButton;
    private Button signInButton;
    private Button selfEditButton;
    private Button singUpButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        initUI();
        initUsersList();
    }

    private void initUI() {
        logOutButton = (Button) findViewById(R.id.logout_button);
        signInButton = (Button) findViewById(R.id.sign_in_button);
        selfEditButton = (Button) findViewById(R.id.self_edit_button);
        singUpButton = (Button) findViewById(R.id.sign_up_button);
        usersList = (ListView) findViewById(R.id.users_listview);
    }

    private void initUsersList() {
        usersListAdapter = new UserListAdapter(this);
        usersList.setAdapter(usersListAdapter);
        usersList.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DataHolder.getDataHolder().getSignInQbUser() != null) {
            signInButton.setVisibility(View.GONE);
            singUpButton.setVisibility(View.GONE);
            logOutButton.setVisibility(View.VISIBLE);
            selfEditButton.setVisibility(View.VISIBLE);
        }
        usersListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // destroy session after app close
        DataHolder.getDataHolder().setSignInQbUser(null);
        QBAuth.deleteSession(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            signInButton.setVisibility(View.GONE);
            logOutButton.setVisibility(View.VISIBLE);
        }
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.sign_in_button:
                intent = new Intent(this, SignInActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.sign_up_button:
                intent = new Intent(this, SignUpUserActivity.class);
                startActivity(intent);
                break;
            case R.id.logout_button:
                progressDialog.show();
                // call query to sign out by current user
                QBManager.signOut(this, QBQueries.QB_QUERY_LOG_OUT_QB_USER);
                break;
            case R.id.self_edit_button:
                intent = new Intent(this, UpdateUserActivity.class);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onComplete(Result result) {
    }

    @Override
    public void onComplete(Result result, Object query) {
        QBQueries qbQueryType = (QBQueries) query;
        if (result.isSuccess()) {
            switch (qbQueryType) {
                case QB_QUERY_LOG_OUT_QB_USER:
                    DialogUtils.showLong(context, getResources().getString(R.string.user_log_out_msg));
                    updateDataAfterLogOut();
                    break;
            }
        } else {
            DialogUtils.showLong(context, result.getErrors().get(0));
            progressDialog.hide();
        }
        progressDialog.hide();
    }

    private void updateDataAfterLogOut() {
        DataHolder.getDataHolder().setSignInQbUser(null);
        signInButton.setVisibility(View.VISIBLE);
        logOutButton.setVisibility(View.GONE);
        selfEditButton.setVisibility(View.GONE);
        singUpButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        startShowUserActivity(position);
    }

    private void startShowUserActivity(int position) {
        Intent intent = new Intent(this, ShowUserActivity.class);
        intent.putExtra(POSITION, position);
        startActivity(intent);
    }
}