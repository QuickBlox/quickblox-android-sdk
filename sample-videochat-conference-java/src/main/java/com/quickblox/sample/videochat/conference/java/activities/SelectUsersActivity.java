package com.quickblox.sample.videochat.conference.java.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.sample.videochat.conference.java.adapters.CheckboxUsersAdapter;
import com.quickblox.sample.videochat.conference.java.db.QbUsersDbManager;
import com.quickblox.sample.videochat.conference.java.utils.Consts;
import com.quickblox.sample.videochat.conference.java.utils.SharedPrefsHelper;
import com.quickblox.sample.videochat.conference.java.utils.ToastUtils;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;


public class SelectUsersActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final String EXTRA_QB_OCCUPANTS_IDS = "qb_occupants_ids";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private static final String EXTRA_QB_DIALOG = "qb_dialog";

    private QbUsersDbManager dbManager;

    private ListView usersListView;
    private CheckboxUsersAdapter usersAdapter;
    private long lastClickTime = 0l;
    private QBUser currentUser;
    private QBChatDialog dialog;

    public static void startForResult(Fragment fragment, int code, QBChatDialog dialog) {
        Intent intent = new Intent(fragment.getContext(), SelectUsersActivity.class);

        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        fragment.startActivityForResult(intent, code);
    }

    /**
     * Start activity for picking users
     *
     * @param activity activity to return result
     * @param code     request code for onActivityResult() method
     *                 <p>
     *                 in onActivityResult there will be 'ArrayList<QBUser>' in the intent extras
     *                 which can be obtained with SelectPeopleActivity.EXTRA_QB_USERS key
     */
    public static void startForResult(Activity activity, int code) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);

        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);

        usersListView = findViewById(R.id.list_select_users);
        currentUser = sharedPrefsHelper.getQbUser();
        setActionbarTitle(isEditingChat() ? R.string.select_users_edit_dialog : R.string.select_users_create_dialog);

        actionBar.setDisplayHomeAsUpEnabled(true);

        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        initUsersAdapter();
    }

    @Override
    protected void onDestroy() {
        hideProgressDialog();
        super.onDestroy();
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
                if (isEditingChat()) {
                    addOccupantsToDialog();
                } else if (usersAdapter != null) {
                    List<QBUser> users = usersAdapter.getSelectedUsers();
                    if (users.size() >= MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        passResultToCallerActivity(null);
                    } else {
                        ToastUtils.shortToast(R.string.select_users_choose_users);
                    }
                }
                return true;
            case R.id.menu_refresh_users:
                if (isEditingChat()) {
                    updateDialogAndUsers();
                } else {
                    loadUsersFromQb();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addOccupantsToDialog() {
        showProgressDialogIfPossible(R.string.dlg_updating_dialog);

        List<QBUser> users = usersAdapter.getSelectedUsers();
        QBUser[] usersArray = users.toArray(new QBUser[users.size()]);
        Log.d("SelectedUsersActivity", "usersArray= " + Arrays.toString(usersArray));

        requestExecutor.updateDialog(dialog, usersArray, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle params) {
                dismissProgressDialogIfPossible();
                passResultToCallerActivity(dialog.getOccupants());
            }

            @Override
            public void onError(QBResponseException responseException) {
                dismissProgressDialogIfPossible();
                showErrorSnackbar(R.string.dlg_updating_dialog, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        addOccupantsToDialog();
                    }
                });
            }
        });
    }

    private void removeExistentOccupants(List<QBUser> users) {
        List<Integer> userIDs = dialog.getOccupants();
        if (userIDs == null) {
            return;
        }

        Iterator<QBUser> i = users.iterator();
        while (i.hasNext()) {
            QBUser user = i.next();

            for (Integer userID : userIDs) {
                if (user.getId().equals(userID)) {
                    Log.d("SelectedUsersActivity", "users.remove(user)= " + user);
                    i.remove();
                }
            }
        }
    }

    private void passResultToCallerActivity(List<Integer> occupantsIds) {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        if (occupantsIds != null) {
            result.putExtra(EXTRA_QB_OCCUPANTS_IDS, (Serializable) occupantsIds);
        }
        setResult(RESULT_OK, result);
        finish();
    }

    private void initUsersAdapter() {
        ArrayList<QBUser> users = dbManager.getAllUsers();
        dialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);
        if (dialog != null) {
            updateDialogAndUsers();
            usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, new ArrayList<QBUser>(), currentUser);
        } else {
            usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, users, currentUser);
        }
        usersListView.setAdapter(usersAdapter);
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }

    private void updateDialogAndUsers() {
        showProgressDialogIfPossible(R.string.dlg_loading_dialogs_users);
        requestExecutor.loadDialogByID(dialog.getDialogId(), new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog dialog, Bundle params) {
                SelectUsersActivity.this.dialog.setOccupantsIds(dialog.getOccupants());
                loadUsersFromQb();
                dismissProgressDialogIfPossible();
            }

            @Override
            public void onError(QBResponseException responseException) {
                dismissProgressDialogIfPossible();
                showErrorSnackbar(R.string.loading_dialog_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadUsersFromQb();
                    }
                });
            }
        });
    }

    private void showProgressDialogIfPossible(@StringRes int messageId) {
        if (!isFinishing()) {
            showProgressDialog(messageId);
        }
    }

    private void dismissProgressDialogIfPossible() {
        if (!isFinishing()) {
            hideProgressDialog();
        }
    }

    private void loadUsersFromQb() {
        showProgressDialogIfPossible(R.string.dlg_loading_dialogs_users);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);

        requestExecutor.loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                dbManager.saveAllUsers(users, true);
                if (isEditingChat()) {
                    users.remove(currentUser);
                    removeExistentOccupants(users);
                }
                usersAdapter.updateList(users);

                dismissProgressDialogIfPossible();
            }

            @Override
            public void onError(QBResponseException responseException) {
                dismissProgressDialogIfPossible();
                showErrorSnackbar(R.string.loading_users_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadUsersFromQb();
                    }
                });
            }
        });
    }
}