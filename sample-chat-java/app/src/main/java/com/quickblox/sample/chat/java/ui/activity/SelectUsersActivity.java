package com.quickblox.sample.chat.java.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.ui.adapter.CheckboxUsersAdapter;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AlertDialog;

public class SelectUsersActivity extends BaseActivity {
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final String EXTRA_CHAT_NAME = "chat_name";
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 2;
    public static final int PRIVATE_CHAT_OCCUPANTS_SIZE = 2;

    private static final int PER_PAGE_SIZE = 100;

    private static final String ORDER_RULE = "order";
    private static final String ORDER_VALUE = "desc string updated_at";

    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private static final String EXTRA_QB_DIALOG = "qb_dialog";

    private ListView usersListView;
    private ProgressBar progressBar;
    private CheckboxUsersAdapter usersAdapter;
    private List<QBUser> users;
    private long lastClickTime = 0l;
    private QBChatDialog qbChatDialog;
    private String chatName;

    public static void start(Context context) {
        Intent intent = new Intent(context, SelectUsersActivity.class);
        context.startActivity(intent);
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
        qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);

        initUi();
        loadUsersFromQb();
    }

    private void initUi() {
        progressBar = findViewById(R.id.progress_select_users);
        usersListView = findViewById(R.id.list_select_users);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, usersListView, false);
        listHeader.setText(R.string.select_users_list_hint);
        usersListView.addHeaderView(listHeader, null, false);

        if (isEditingChat()) {
            setActionBarTitle(getString(R.string.select_users_edit_chat));
        } else {
            setActionBarTitle(getString(R.string.select_users_create_chat));
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
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
                    List<QBUser> users = new ArrayList<>(usersAdapter.getSelectedUsers());
                    if (users.size() < MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        ToastUtils.shortToast(R.string.select_users_choose_users);
                    } else {
                        if (qbChatDialog == null && users.size() > PRIVATE_CHAT_OCCUPANTS_SIZE) {
                            showChatNameDialog();
                        } else {
                            passResultToCallerActivity();
                        }
                    }
                }
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showChatNameDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_enter_chat_name, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextGroupName = dialogView.findViewById(R.id.edittext_dialog_name);

        dialogBuilder.setTitle(R.string.dialog_enter_chat_name);
        dialogBuilder.setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(editTextGroupName.getText())) {
                    ToastUtils.shortToast(R.string.dialog_enter_chat_name);
                } else {
                    chatName = editTextGroupName.getText().toString();
                    passResultToCallerActivity();
                    dialog.dismiss();
                }
            }
        });

        dialogBuilder.setNegativeButton(R.string.dialog_Cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.create().show();
    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        if (!TextUtils.isEmpty(chatName)) {
            result.putExtra(EXTRA_CHAT_NAME, chatName);
        }
        setResult(RESULT_OK, result);
        finish();
    }

    private void loadUsersFromQb() {
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ORDER_RULE, ORDER_VALUE));

        QBPagedRequestBuilder qbPagedRequestBuilder = new QBPagedRequestBuilder();
        qbPagedRequestBuilder.setRules(rules);
        qbPagedRequestBuilder.setPerPage(PER_PAGE_SIZE);

        progressBar.setVisibility(View.VISIBLE);
        QBUsers.getUsers(qbPagedRequestBuilder, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> users, Bundle params) {
                SelectUsersActivity.this.users = users;

                if (qbChatDialog != null) {
                    // update occupants list form server
                    getDialog();
                } else {
                    usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, users);
                    updateUsersAdapter();
                }
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_users_error, e,
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

    private void getDialog() {
        String dialogID = qbChatDialog.getDialogId();
        ChatHelper.getInstance().getDialogById(dialogID, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                SelectUsersActivity.this.qbChatDialog = qbChatDialog;
                loadUsersFromDialog(qbChatDialog.getOccupants());
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_dialog_error, e,
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

    private void updateUsersAdapter() {
        if (qbChatDialog != null) {
            usersAdapter.addSelectedUsers(qbChatDialog.getOccupants());
        }
        usersListView.setAdapter(usersAdapter);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isEditingChat() {
        return getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
    }

    private void loadUsersFromDialog(List<Integer> userIdsList) {
        QBUsers.getUsersByIDs(userIdsList, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                usersAdapter = new CheckboxUsersAdapter(SelectUsersActivity.this, users);
                for (QBUser user : qbUsers) {
                    usersAdapter.addUserToUserList(user);
                }
                updateUsersAdapter();
            }

            @Override
            public void onError(QBResponseException e) {
                showErrorSnackbar(R.string.select_users_get_users_dialog_error, e, null);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}