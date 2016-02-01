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

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.CheckboxUsersAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class SelectUsersActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;

    private static final String EXTRA_QB_DIALOG = "qb_dialog";

    private ListView usersListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectUsersActivity.class);
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
        startForResult(activity, code, null);
    }

    public static void startForResult(Activity activity, int code, QBDialog dialog) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);

        progressBar = _findViewById(R.id.progress_select_users);
        usersListView = _findViewById(R.id.list_select_users);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, usersListView, false);
        listHeader.setText(R.string.select_users_list_hint);
        usersListView.addHeaderView(listHeader, null, false);

        if (isEditingChat()) {
            actionBar.setTitle(R.string.select_users_edit_chat);
        } else {
            actionBar.setTitle(R.string.select_users_create_chat);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_select_people_action_done:
            if (usersAdapter != null) {
                List<QBUser> users = usersAdapter.getSelectedUsers();
                if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                    passResultToCallerActivity();
                }
            }
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            loadUsersFromQb();
        }
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.layout_root);
    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        setResult(RESULT_OK, result);
        finish();
    }

    private void loadUsersFromQb() {
        List<String> tags = new ArrayList<>();
        tags.add(Consts.USERS_TAG);

        progressBar.setVisibility(View.VISIBLE);
        QBUsers.getUsersByTags(tags, null, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                QBDialog dialog = (QBDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);

                usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, result);
                if (dialog != null) {
                    usersAdapter.addSelectedUsers(dialog.getOccupants());
                }
                usersListView.setAdapter(usersAdapter);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                showErrorSnackbar(R.string.select_users_get_users_error, errors,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                loadUsersFromQb();
                            }
                        });
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }
}
