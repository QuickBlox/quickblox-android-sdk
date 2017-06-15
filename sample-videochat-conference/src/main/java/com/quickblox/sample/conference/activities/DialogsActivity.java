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
import com.quickblox.conference.ConferenceClient;
import com.quickblox.conference.ConferenceSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.conference.R;
import com.quickblox.sample.conference.adapters.DialogsAdapter;
import com.quickblox.sample.conference.db.QbUsersDbManager;
import com.quickblox.sample.conference.utils.Consts;
import com.quickblox.sample.conference.utils.PermissionsChecker;
import com.quickblox.sample.conference.utils.UsersUtils;
import com.quickblox.sample.conference.utils.WebRtcSessionManager;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Roman on 05.03.2017.
 */

public class DialogsActivity extends BaseActivity {
    private static final String TAG = DialogsActivity.class.getSimpleName();
    private static final int REQUEST_SELECT_PEOPLE = 174;
    private static final int REQUEST_PERMISSION = 175;

    private DialogsAdapter dialogsAdapter;
    private ListView dialogsListView;
    private QBUser currentUser;
    private ArrayList<QBChatDialog> chatDialogs;
    private QbUsersDbManager dbManager;
    private WebRtcSessionManager webRtcSessionManager;
    private ActionMode currentActionMode;
    private FloatingActionButton fab;
    private String dialogID;
    private List<Integer> occupants;
    private boolean isVideoCall;

    private PermissionsChecker checker;

    public static void start(Context context) {
        Intent intent = new Intent(context, DialogsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);
        initFields();

        initDefaultActionBar();

        initUi();

        startLoadDialogs();
        loadUsersFromQb();

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
        showProgressDialog(R.string.dlg_loading_dialogs_users);
        requestExecutor.loadDialogs(new QBEntityCallback<ArrayList<QBChatDialog>>() {
            @Override
            public void onSuccess(ArrayList<QBChatDialog> result, Bundle params) {
                hideProgressDialog();
                chatDialogs = result;
                initDialogAdapter();
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

    private void loadUsersFromQb() {
        showProgressDialog(R.string.dlg_loading_dialogs_users);
        String currentRoomName = SharedPrefsHelper.getInstance().get(Consts.PREF_CURREN_ROOM_NAME);

        requestExecutor.loadUsersByTag(currentRoomName, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                hideProgressDialog();
                dbManager.saveAllUsers(result, true);
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

    @Override
    public ActionMode startSupportActionMode(ActionMode.Callback callback) {
        currentActionMode = super.startSupportActionMode(callback);
        return currentActionMode;
    }

    private void initDialogAdapter() {
        Log.d(TAG, "proceedInitUsersList chatDialogs= " + chatDialogs);
        if(dialogsAdapter == null) {
            dialogsAdapter = new DialogsAdapter(this, chatDialogs);
            dialogsListView.setAdapter(dialogsAdapter);
            dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                    if (currentActionMode == null) {
                        Log.d(TAG, "startConference selectedDialog.getDialogId()= " + selectedDialog.getDialogId()
                                + ", currentUser.getId()= " + currentUser.getId());
                        occupants = selectedDialog.getOccupants();
                        occupants.remove(currentUser.getId());
                        dialogID = selectedDialog.getDialogId();


                        dialogsAdapter.toggleOneItem(selectedDialog);
                        invalidateOptionsMenu();

                    } else {
                        dialogsAdapter.toggleSelection(selectedDialog);
                        updateActionBar(dialogsAdapter.getSelectedItems().size());
                    }
                }
            });
            dialogsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    QBChatDialog selectedDialog = (QBChatDialog) parent.getItemAtPosition(position);
                    startSupportActionMode(new DeleteActionModeCallback());
                    dialogsAdapter.selectItem(selectedDialog);
                    updateActionBar(dialogsAdapter.getSelectedItems().size());
                    return true;
                }
            });
        } else {
            dialogsAdapter.updateList(chatDialogs);
        }
    }

    private void startPermissionsActivity(boolean checkOnlyAudio) {
        PermissionsActivity.startForResult(this, REQUEST_PERMISSION, checkOnlyAudio, Consts.PERMISSIONS);
    }

    private void updateActionBar(int countSelectedUsers) {
        currentActionMode.setSubtitle(null);
        currentActionMode.setTitle(String.format(getString(
                countSelectedUsers > 1
                        ? R.string.tile_many_dialogs_selected
                        : R.string.title_one_dialog_selected),
                countSelectedUsers));
        currentActionMode.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (dialogsAdapter != null && !dialogsAdapter.getSelectedItems().isEmpty()) {
            getMenuInflater().inflate(R.menu.activity_selected_opponents, menu);
        } else {
            getMenuInflater().inflate(R.menu.activity_opponents, menu);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.update_opponents_list:
                updateDialogsAdapter();
                loadUsersFromQb();
                return true;

            case R.id.settings:
                showSettings();
                return true;

            case R.id.log_out:
                logOut();
                return true;

            case R.id.start_video_call:
                isVideoCall = true;
                startConference();
                return true;

            case R.id.start_audio_call:
                isVideoCall = false;
                startConference();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startConference() {
        if (checker.lacksPermissions(Consts.PERMISSIONS)) {
            startPermissionsActivity(!isVideoCall);
        } else {
            startConference(dialogID, currentUser.getId(), isVideoCall, occupants);
        }
    }

    private void updateDialogsAdapter() {
        startLoadDialogs();
    }

    private void showSettings() {
        SettingsActivity.start(this);
    }

    private void logOut() {
        removeAllUserData();
        startLoginActivity();
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
        SelectUsersActivity.startForResult(this, REQUEST_SELECT_PEOPLE);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data
                        .getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                ProgressDialogFragment.show(getSupportFragmentManager(), R.string.create_dialog);
                createDialog(selectedUsers);
            } if(requestCode == REQUEST_PERMISSION) {
                startConference(dialogID, currentUser.getId(), isVideoCall, occupants);
            }
            else {
                updateDialogsAdapter();
            }
        }
    }

    private void createDialog(final ArrayList<QBUser> selectedUsers) {
        requestExecutor.createDialogWithSelectedUsers(selectedUsers, currentUser,
                new QBEntityCallback<QBChatDialog>() {
                    @Override
                    public void onSuccess(QBChatDialog dialog, Bundle args) {
                        //                        dialogsManager.sendSystemMessageAboutCreatingDialog(systemMessagesManager, dialog);
                        Log.d(TAG, "createDialogWithSelectedUsers dialog name= " + dialog.getName());
                        updateDialogsAdapter();
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        ProgressDialogFragment.hide(getSupportFragmentManager());
                        showErrorSnackbar(R.string.dialogs_creation_error, null, null);
                    }
                }
        );
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
                invalidateOptionsMenu();
                fab.show();
            }


            private void deleteSelectedDialogs() {
                final Collection<QBChatDialog> selectedDialogs = dialogsAdapter.getSelectedItems();
                requestExecutor.deleteDialogs(selectedDialogs, new QBEntityCallback<ArrayList<String>>() {
                    @Override
                    public void onSuccess(ArrayList<String> dialogsIds, Bundle bundle) {
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

    private void startConference(final String dialogID, int userID, boolean isVideoCall, final List<Integer> occupants) {
        Log.d(TAG, "startConference()");
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.join_conference);
        ConferenceClient client = ConferenceClient.getInstance(getApplicationContext());

        QBRTCTypes.QBConferenceType conferenceType = isVideoCall
                ? QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO
                : QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

        client.createSession(userID, conferenceType, new QBEntityCallback<ConferenceSession>() {
            @Override
            public void onSuccess(ConferenceSession session, Bundle params) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                webRtcSessionManager.setCurrentSession(session);
                Log.d(TAG, "DialogActivity setCurrentSession onSuccess() session getCurrentUserID= " + session.getCurrentUserID());

                CallActivity.start(DialogsActivity.this, dialogID, occupants);
            }

            @Override
            public void onError(QBResponseException responseException) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                showErrorSnackbar(R.string.join_conference_error, null, null);
            }
        });
    }
}
