package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.adapter.UserListAdapter;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class UsersListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final String PERMANENT_MENU_KEY = "sHasPermanentMenuKey";
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
        getOverflowMenu();
        initUI();
        getAllUsers();
    }

    private void initUI() {
        usersList = _findViewById(R.id.users_listview);

        usersListAdapter = new UserListAdapter(this, DataHolder.getInstance().getQBUsers());
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
        setTitle(DataHolder.getInstance().getSignInQbUser() != null);

        usersListAdapter.updateData(DataHolder.getInstance().getQBUsers());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataHolder.getInstance().setSignInQbUser(null);
    }

    private void updateDataAfterLogOut() {
        DataHolder.getInstance().setSignInQbUser(null);
        invalidateOptionsMenu();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QBUser qbUser = (QBUser) adapterView.getItemAtPosition(position);
        ShowUserActivity.start(this, qbUser.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (DataHolder.getInstance().getSignInQbUser() == null) {
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
                        progressDialog.dismiss();
                        Toaster.longToast(R.string.user_log_out_msg);
                        updateDataAfterLogOut();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        progressDialog.dismiss();

                        Toaster.longToast(e.getErrors().toString());
                    }
                });
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getAllUsers() {
        progressDialog.show();
        QBUsers.getUsers(null, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                DataHolder.getInstance().addQbUsers(qbUsers);
                if (!DataHolder.getInstance().isEmpty()) {
                    DataHolder.getInstance().clear();
                }
                DataHolder.getInstance().addQbUsers(qbUsers);
                progressDialog.dismiss();
                usersListAdapter.updateData(DataHolder.getInstance().getQBUsers());
            }

            @Override
            public void onError(QBResponseException e) {
                Toaster.longToast(e.getErrors().toString());
                progressDialog.dismiss();
            }
        });
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField(PERMANENT_MENU_KEY);
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}