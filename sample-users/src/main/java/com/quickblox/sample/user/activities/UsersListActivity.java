package com.quickblox.sample.user.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.user.R;
import com.quickblox.sample.user.adapter.UserListAdapter;
import com.quickblox.sample.user.helper.DataHolder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class UsersListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final int LIMIT_USERS = 50;
    private static final String RULE_PARAM = "order";
    private static final String RULE_VALUE = "desc date created_at";
    private int currentPage = 1;
    private UserListAdapter usersListAdapter;
    private QBPagedRequestBuilder qbPagedBuilder;
    private SwipyRefreshLayout setOnRefreshListener;

    public static void start(Context context) {
        Intent intent = new Intent(context, UsersListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        DataHolderClear();
        initUI();
        getAllUsers(true);
    }

    private void initUI() {
        ListView usersListView = _findViewById(R.id.users_listview);
        setOnRefreshListener = _findViewById(R.id.swipy_refresh_layout);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_header, usersListView, false);

        usersListView.addHeaderView(listHeader, null, false);
        usersListAdapter = new UserListAdapter(this, DataHolder.getInstance().getQBUsers());
        usersListView.setAdapter(usersListAdapter);
        usersListView.setOnItemClickListener(this);

        setQBPagedBuilder();
        setOnRefreshListener.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh(SwipyRefreshLayoutDirection direction) {
                qbPagedBuilder.setPage(++currentPage);
                getAllUsers(false);
            }
        });
    }

    private void setTitle(boolean signIn) {
        if (getSupportActionBar() != null) {

            if (signIn) {
                getSupportActionBar().setTitle(R.string.signed_in);
            } else {
                getSupportActionBar().setTitle(R.string.not_signed_in);
            }
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

    private void DataHolderClear() {
        if (!DataHolder.getInstance().isEmpty()) {
            DataHolder.getInstance().clear();
        }
    }

    private void updateDataAfterLogOut() {
        DataHolder.getInstance().setSignInQbUser(null);
        invalidateOptionsMenu();
    }

    private void setQBPagedBuilder() {
        qbPagedBuilder = new QBPagedRequestBuilder();
        GenericQueryRule genericQueryRule = new GenericQueryRule(RULE_PARAM, RULE_VALUE);

        ArrayList<GenericQueryRule> rule = new ArrayList<>();
        rule.add(genericQueryRule);

        qbPagedBuilder.setPerPage(LIMIT_USERS);
        qbPagedBuilder.setRules(rule);
    }

    private void getAllUsers(boolean progress) {
        if (progress) {
            progressDialog.show();
        }

        QBUsers.getUsers(qbPagedBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                DataHolder.getInstance().addQbUsers(qbUsers);
                progressDialog.dismiss();
                setOnRefreshListener.setRefreshing(false);
                usersListAdapter.updateData(DataHolder.getInstance().getQBUsers());
            }

            @Override
            public void onError(QBResponseException e) {
                Toaster.longToast(e.getErrors().toString());
                progressDialog.dismiss();
                setOnRefreshListener.setRefreshing(false);
            }
        });
    }
}