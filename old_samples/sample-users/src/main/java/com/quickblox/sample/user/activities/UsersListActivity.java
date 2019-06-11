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
import java.util.Collections;
import java.util.List;

public class UsersListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private static final int LIMIT_USERS = 50;
    private static final int REQUEST_CODE_SIGN_UP = 100;
    private static final String ORDER_RULE = "order";
    private static final String ORDER_VALUE = "desc date created_at";
    private int currentPage = 1;
    private UserListAdapter usersListAdapter;
    private QBPagedRequestBuilder qbPagedBuilder;
    private SwipyRefreshLayout setOnRefreshListener;
    private List<QBUser> qbUsersList;

    public static void start(Context context) {
        Intent intent = new Intent(context, UsersListActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        DataHolder.getInstance().clear();
        initUI();
        getAllUsers(true);
    }

    @Override
    protected void initUI() {
        ListView usersListView = _findViewById(R.id.users_listview);
        setOnRefreshListener = _findViewById(R.id.swipy_refresh_layout);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_header, usersListView, false);

        usersListView.addHeaderView(listHeader, null, false);
        qbUsersList = DataHolder.getInstance().getQBUsers();
        usersListAdapter = new UserListAdapter(this, qbUsersList);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_SIGN_UP) {
                qbUsersList = DataHolder.getInstance().getQBUsers();
                Collections.rotate(qbUsersList, 1);
                usersListAdapter.updateList(qbUsersList);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        usersListAdapter.notifyDataSetChanged();
        setActionBarTitle(DataHolder.getInstance().isSignedIn() ? DataHolder.getInstance().getSignInQbUser().getLogin()
                : getString(R.string.not_signed_in));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataHolder.getInstance().setSignInQbUser(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        QBUser qbUser = (QBUser) adapterView.getItemAtPosition(position);
        ShowUserActivity.start(this, qbUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_users_list, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!DataHolder.getInstance().isSignedIn()) {
            setActionBarTitle(R.string.not_signed_in);
            menu.getItem(0).setVisible(true);
            menu.getItem(1).setVisible(true);
            menu.getItem(2).setVisible(false);
            menu.getItem(3).setEnabled(false);
        } else {
            setActionBarTitle(DataHolder.getInstance().getSignInQbUser().getLogin());
            menu.getItem(0).setVisible(false);
            menu.getItem(1).setVisible(false);
            menu.getItem(2).setVisible(true);
            menu.getItem(3).setEnabled(true);
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
                startActivityForResult(intent, REQUEST_CODE_SIGN_UP);
                return true;

            case R.id.profile:
                ShowUserActivity.start(this, DataHolder.getInstance().getSignInQbUser());
                return true;

            case R.id.logout:
                progressDialog.show();
                QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
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

    private void updateDataAfterLogOut() {
        DataHolder.getInstance().setSignInQbUser(null);
        invalidateOptionsMenu();
    }

    private void setQBPagedBuilder() {
        qbPagedBuilder = new QBPagedRequestBuilder();
        GenericQueryRule genericQueryRule = new GenericQueryRule(ORDER_RULE, ORDER_VALUE);

        ArrayList<GenericQueryRule> rule = new ArrayList<>();
        rule.add(genericQueryRule);

        qbPagedBuilder.setPerPage(LIMIT_USERS);
        qbPagedBuilder.setRules(rule);
    }

    private void getAllUsers(boolean showProgress) {
        if (showProgress) {
            progressDialog.show();
        }

        QBUsers.getUsers(qbPagedBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                setOnRefreshListener.setEnabled(true);
                setOnRefreshListener.setRefreshing(false);

                DataHolder.getInstance().addQbUsers(qbUsers);
                qbUsersList = DataHolder.getInstance().getQBUsers();
                progressDialog.dismiss();
                usersListAdapter.updateList(qbUsersList);
            }

            @Override
            public void onError(QBResponseException e) {
                progressDialog.dismiss();
                setOnRefreshListener.setEnabled(false);
                setOnRefreshListener.setRefreshing(false);

                View rootLayout = findViewById(R.id.swipy_refresh_layout);
                showSnackbarError(rootLayout, R.string.errors, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        getAllUsers(false);
                    }
                });
            }
        });
    }
}