package com.quickblox.sample.chat.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.CheckboxUsersAdapter;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class SelectPeopleActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    private static final int USERS_ITEMS_PER_PAGE = 10;

    private int firstVisibleItemPosition;
    private int firstVisibleItemOffset;
    private int currentUsersQbPage;

    private boolean isHeaderAdded;

    private PullToRefreshListView userPullToRefreshListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectPeopleActivity.class);
        context.startActivity(intent);
    }

    /**
     * Start activity for picking users
     *
     * @param activity activity to return result
     * @param code request code for onActivityResult() method
     *
     * in onActivityResult there will be 'ArrayList<QBUser>' in the intent extras
     * which can be obtained with SelectPeopleActivity.EXTRA_QB_USERS key
     */
    public static void startForResult(Activity activity, int code) {
        Intent intent = new Intent(activity, SelectPeopleActivity.class);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_people);

        progressBar = (ProgressBar) findViewById(R.id.progress_chat);
        userPullToRefreshListView = (PullToRefreshListView) findViewById(R.id.list_dialog_users);

        userPullToRefreshListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNextPageWithUsers();

                View v = userPullToRefreshListView.getRefreshableView().getChildAt(0);
                firstVisibleItemPosition = userPullToRefreshListView.getRefreshableView().getFirstVisiblePosition();
                firstVisibleItemOffset = (v == null) ? 0 : v.getTop();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_people, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_action_done:
            if (usersAdapter != null && !usersAdapter.getSelectedUsers().isEmpty()) {
                passResultToCallerActivity();
            }
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSessionCreated(final boolean success) {
        if (success) {
            loadNextPageWithUsers();
        }
    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }

    private void loadNextPageWithUsers() {
        currentUsersQbPage++;
        QBUsers.getUsers(getQBPagedRequestBuilder(currentUsersQbPage), new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                List<QBUser> users = new ArrayList<>();
                if (usersAdapter != null) {
                    users.addAll(usersAdapter.getUsers());
                }
                users.addAll(qbUsers);

                usersAdapter = new CheckboxUsersAdapter(SelectPeopleActivity.this, users);
                userPullToRefreshListView.setAdapter(usersAdapter);
                userPullToRefreshListView.onRefreshComplete();
                userPullToRefreshListView.getRefreshableView().setSelectionFromTop(firstVisibleItemPosition, firstVisibleItemOffset);

                // FIXME Adding header to the PullToRefreshListView before setting adapter causes crash
                if (!isHeaderAdded) {
                    TextView listHeader = (TextView) LayoutInflater.from(SelectPeopleActivity.this).inflate(R.layout.include_list_hint_header, userPullToRefreshListView, false);
                    listHeader.setText(R.string.select_people_list_hint);
                    userPullToRefreshListView.getRefreshableView().addHeaderView(listHeader);
                    isHeaderAdded = true;
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                // If it's not the first page requested — we need to decrease currentUsersQbPage value in onError()
                // since if we're are there it means we didn't request users successfully and the next time
                // we need to request the same page to receive all users without missing anyone
                if (currentUsersQbPage != 0) {
                    currentUsersQbPage--;
                }
                progressBar.setVisibility(View.GONE);
                ErrorUtils.showErrorDialog(SelectPeopleActivity.this, getString(R.string.select_people_get_users_error), errors);
            }
        });
    }

    private QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(USERS_ITEMS_PER_PAGE);

        return pagedRequestBuilder;
    }
}
