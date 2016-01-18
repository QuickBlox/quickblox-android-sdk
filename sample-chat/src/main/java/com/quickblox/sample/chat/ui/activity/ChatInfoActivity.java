package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.chat.utils.qb.QbUsersHolder;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class ChatInfoActivity extends BaseActivity {
    private static final String EXTRA_DIALOG = "dialog";

    private ListView usersListView;
    private QBDialog qbDialog;

    public static void start(Context context, QBDialog qbDialog) {
        Intent intent = new Intent(context, ChatInfoActivity.class);
        intent.putExtra(EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        qbDialog = (QBDialog) getIntent().getSerializableExtra(EXTRA_DIALOG);
        usersListView = _findViewById(R.id.list_login_users);

        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SELECT_PEOPLE) {
                ArrayList<QBUser> selectedUsers = (ArrayList<QBUser>) data.getSerializableExtra(SelectUsersActivity.EXTRA_QB_USERS);

                ChatHelper.getInstance().updateDialogUsers(qbDialog, selectedUsers,
                        new QBEntityCallbackImpl<QBDialog>() {
                            @Override
                            public void onSuccess(QBDialog dialog, Bundle args) {
                                qbDialog = dialog;
                                buildUserList();
                            }

                            @Override
                            public void onError(List<String> errors) {
                                ErrorUtils.showErrorDialog(ChatInfoActivity.this, R.string.chat_info_add_people_error, errors);
                            }
                        }
                );
            }
        }
    }

    @Override
    public void onSessionCreated(boolean success) {
        if (success) {
            buildUserList();
        }
    }

    private void buildUserList() {
        ArrayList<Integer> userIds = qbDialog.getOccupants();
        List<QBUser> users = QbUsersHolder.getInstance().getUsersByIds(userIds);

        UsersAdapter adapter = new UsersAdapter(this, users);
        usersListView.setAdapter(adapter);
    }
}
