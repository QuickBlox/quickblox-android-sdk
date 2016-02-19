package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.adapter.UserListAdapter;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;

import static com.quickblox.sample.user.definitions.Consts.POSITION;

public class UsersListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private UserListAdapter usersListAdapter;
    private ListView usersList;

    public static void start(Context context) {
        Intent intent = new Intent(context, UsersListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        initUsersList();
    }

    private void initUsersList() {
        usersList = (ListView) findViewById(R.id.users_listview);

        usersListAdapter = new UserListAdapter(this);
        usersList.setAdapter(usersListAdapter);
        usersList.setOnItemClickListener(this);
    }

    private void setTitle(boolean signIn) {
        if (signIn) {
            getSupportActionBar().setTitle(R.string.singed_in);
        } else {
            getSupportActionBar().setTitle(R.string.not_singed_in);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle(DataHolder.getDataHolder().getSignInQbUser() != null);

        usersListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // destroy session after app close
        DataHolder.getDataHolder().setSignInQbUser(null);
    }

    private void updateDataAfterLogOut() {
        DataHolder.getDataHolder().setSignInQbUser(null);
        invalidateOptionsMenu();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (DataHolder.getDataHolder().getSignInQbUser() == null) {
            menu.getItem(2).setEnabled(false);
            setTitle(false);
        } else {
            setTitle(true);
            menu.getItem(2).setEnabled(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.sign_in:
                intent = new Intent(this, SignInActivity.class);
                startActivity(intent);
                return true;
            case R.id.sign_up:
                intent = new Intent(this, SignUpUserActivity.class);
                startActivity(intent);
                return true;
            case R.id.logout:
                progressDialog.show();
                QBUsers.signOut(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void result, Bundle bundle) {
                        progressDialog.hide();
                        Toaster.longToast(R.string.user_log_out_msg);
                        updateDataAfterLogOut();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.hide();

                        Toaster.longToast(e.getErrors().toString());
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}