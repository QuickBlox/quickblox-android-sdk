package com.quickblox.sample.conference.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.adapters.CheckboxUsersAdapter;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Roman on 05.03.2017.
 */

public class SelectUsersActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private static final String EXTRA_QB_DIALOG = "qb_dialog";

    private ListView usersListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;
    private long lastClickTime = 0l;
    private QBUser currentUser;

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

    public static void startForResult(Activity activity, int code, QBChatDialog dialog) {
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
        currentUser = sharedPrefsHelper.getQbUser();
        if (isEditingChat()) {
            setActionBarTitle(R.string.select_users_edit_dialog);
        } else {
            setActionBarTitle(R.string.select_users_create_dialog);
        }
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadUsersFromQb();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_select_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return super.onOptionsItemSelected(item);
        }
        lastClickTime = SystemClock.uptimeMillis();

        switch (item.getItemId()) {
            case R.id.menu_select_people_action_done:
                if (usersAdapter != null) {
                    List<QBUser> users = usersAdapter.getSelectedUsers();
                    if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        passResultToCallerActivity();
                    } else {
                        Toaster.shortToast(R.string.select_users_choose_users);
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
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
        progressBar.setVisibility(View.VISIBLE);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);

        requestExecutor.loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                QBChatDialog dialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);
                usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, result, currentUser);
                if (dialog != null) {
                    usersAdapter.addSelectedUsers(dialog.getOccupants());
                }
                usersListView.setAdapter(usersAdapter);

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                showErrorSnackbar(R.string.loading_users_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadUsersFromQb();
                    }
                });
            }
        });
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }
}
