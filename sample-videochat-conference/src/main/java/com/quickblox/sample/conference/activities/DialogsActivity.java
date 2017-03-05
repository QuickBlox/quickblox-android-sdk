package com.quickblox.sample.conference.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.messages.services.SubscribeService;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.adapters.DialogsAdapter;
import com.quickblox.sample.conference.db.QbUsersDbManager;
import com.quickblox.sample.conference.services.CallService;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.conference.utils.PermissionsChecker;
import com.quickblox.sample.conference.utils.UsersUtils;
import com.quickblox.sample.conference.utils.WebRtcSessionManager;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Roman on 05.03.2017.
 */

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();


    private DialogsAdapter dialogsAdapter;
    private ListView dialogsListView;
    private QBUser currentUser;
    private ArrayList<QBChatDialog> chatDialogs;
    private QbUsersDbManager dbManager;
    private WebRtcSessionManager webRtcSessionManager;
    private ActionMode currentActionMode;
    private FloatingActionButton fab;

    private PermissionsChecker checker;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_dialogs);
        initFields();

        initDefaultActionBar();

        initUi();

        startLoadDialogs();

        checker = new PermissionsChecker(getApplicationContext());
    }

    @Override
    protected View getSnackbarAnchorView() {
        return findViewById(R.id.list_dialogs);
    }

    private void initFields() {
        Bundle extras = getIntent().getExtras();

        currentUser = sharedPrefsHelper.getQbUser();
        dbManager = QbUsersDbManager.getInstance(getApplicationContext());
        webRtcSessionManager = WebRtcSessionManager.getInstance(getApplicationContext());
    }

    private void initUi() {
        dialogsListView = (ListView) findViewById(R.id.list_dialogs);
        fab = _findViewById(R.id.fab_dialogs_new_chat);
    }

    private void startLoadDialogs() {
        showProgressDialog(R.string.dlg_loading_dialogs);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);
        requestExecutor.loadDialogs(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> result, Bundle params) {
                hideProgressDialog();
//                dbManager.saveAllUsers(result, true);
                chatDialogs = result;
                initDialogsList();
            }

            @Override
            public void onError(QBResponseException responseException) {
                hideProgressDialog();
                showErrorSnackbar(R.string.loading_users_error, responseException, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startLoadDialogs();
                    }
                });
            }
        });
    }
    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void initDialogsList() {
        Log.d(TAG, "proceedInitUsersList chatDialogs= " + chatDialogs);
        dialogsAdapter = new DialogsAdapter(this, chatDialogs);

        dialogsListView.setAdapter(dialogsAdapter);
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                if (currentActionMode == null) {
                    Log.d("AMBRA", "OPEN CALL ACTIVITY");
                } else {
                    dialogsAdapter.toggleSelection(selectedDialog);
                }
                updateActionBar(dialogsAdapter.getSelectedItems().size());
            }
        });
        dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                startSupportActionMode(new DeleteActionModeCallback());
                dialogsAdapter.selectItem(selectedDialog);
                return true;
            }
        });
    }

    private void updateActionBar(int countSelectedUsers) {
        if (countSelectedUsers < 1) {
            initDefaultActionBar();
        } else {
            removeActionbarSubTitle();
            initActionBarWithSelectedUsers(countSelectedUsers);
        }

        invalidateOptionsMenu();
    }

    private void initActionBarWithSelectedUsers(int countSelectedUsers) {
        setActionBarTitle(String.format(getString(
                countSelectedUsers > 1
                        ? R.string.tile_many_users_selected
                        : R.string.title_one_user_selected),
                countSelectedUsers));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.activity_opponents, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.update_opponents_list:
                startLoadDialogs();
                return true;

            case R.id.settings:
                showSettings();
                return true;

            case R.id.log_out:
                logOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void updateDialogsAdapter() {
//        dialogsAdapter.updateList(new ArrayList<>(QbDialogHolder.getInstance().getDialogs().values()));
        startLoadDialogs();
    }

    private void showSettings() {
        SettingsActivity.start(this);
    }
    private void logOut() {
        unsubscribeFromPushes();
        startLogoutCommand();
        removeAllUserData();
        startLoginActivity();
    }
    private void unsubscribeFromPushes() {
        SubscribeService.unSubscribeFromPushes(this);
    }

    private void startLogoutCommand() {
        CallService.logout(this);
    }

    private void removeAllUserData() {
        UsersUtils.removeUserData(getApplicationContext());
        requestExecutor.deleteCurrentUser(currentUser.getId(), new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                Log.d(TAG, "Current user was deleted from QB");
            }

            @Override
            public void onError(QBResponseException e) {
                Log.e(TAG, "Current user wasn't deleted from QB " + e);
            }
        });
    }

    private void startLoginActivity() {
        LoginActivity.start(this);
        finish();
    }

    public void onCreateNewDialog(View view) {
        Log.d("AMBRA", "Create new dialog");
    }

    private class DeleteActionModeCallback implements ActionMode.Callback {

        public DeleteActionModeCallback() {
            fab.hide();
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.activity_selected_dialogs, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete_dialog:
                    deleteSelectedDialogs();
                    if (currentActionMode != null) {
                        currentActionMode.finish();
                    }
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            currentActionMode = null;
            dialogsAdapter.clearSelection();
            fab.show();
        }


        private void deleteSelectedDialogs() {
            final Collection<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
            requestExecutor.deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                @Override
                public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
//                    QbDialogHolder.getInstance().deleteDialogs(dialogsIds);
                    updateDialogsAdapter();
                }

                @Override
                public void onError(QBResponseException e) {
                    showErrorSnackbar(R.string.dialogs_deletion_error, e,
                            new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteSelectedDialogs();
                                }
                            });
                }
            });
        }
    }
}
