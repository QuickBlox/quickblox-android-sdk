package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.ChatUtils;
import com.quickblox.sample.chat.utils.ErrorUtils;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class NewDialogActivity extends BaseActivity {
    private static final int USERS_ITEMS_PER_PAGE = 10;

    private int firstVisibleItemPosition;
    private int firstVisibleItemOffset;
    private int currentUsersQbPage;

    private PullToRefreshListView usersList;
    private ProgressBar progressBar;
    private UsersAdapter usersAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewDialogActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_new);

        progressBar = (ProgressBar) findViewById(R.id.progress_chat);
        usersList = (PullToRefreshListView) findViewById(R.id.list_dialog_users);

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNextPageWithUsers();

                View v = usersList.getRefreshableView().getChildAt(0);
                firstVisibleItemPosition = usersList.getRefreshableView().getFirstVisiblePosition();
                firstVisibleItemOffset = (v == null) ? 0 : v.getTop();
            }
        });

        if (isSessionActive()) {
            loadNextPageWithUsers();
        }
    }

    @Override
    public void onSessionCreated(final boolean success) {
        if (success) {
            loadNextPageWithUsers();
        }
    }

    public void onCreateChatClick(View view) {
        createDialogWithSelectedUsers();
    }

    private void createDialogWithSelectedUsers() {
        List<QBUser> selectedUsers = usersAdapter.getSelectedUsers();

        ChatHelper chatHelper = ChatHelper.getInstance();
        chatHelper.addDialogsUsers(selectedUsers);

        // Create new dialog to start a chat
        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName(ChatUtils.createChatNameFromUserList(selectedUsers));
        if (selectedUsers.size() == 1) {
            dialogToCreate.setType(QBDialogType.PRIVATE);
        } else {
            dialogToCreate.setType(QBDialogType.GROUP);
        }
        dialogToCreate.setOccupantsIds(ChatUtils.getUserIds(selectedUsers));

        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate,
                new QBEntityCallbackImpl<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        ChatActivity.start(NewDialogActivity.this, dialog);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        ErrorUtils.showErrorDialog(NewDialogActivity.this, getString(R.string.new_dialog_creation_error), errors);
                    }
                }
        );
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

                usersAdapter = new UsersAdapter(NewDialogActivity.this, users);
                usersList.setAdapter(usersAdapter);
                usersList.onRefreshComplete();
                usersList.getRefreshableView().setSelectionFromTop(firstVisibleItemPosition, firstVisibleItemOffset);

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(List<String> errors) {
                // If it's not the first page requested — we need to decrease currentUsersQbPage value in onError()
                // since if we're are there it means we didn't request users successfully and the next time
                // we need to request the same page to receive all users
                if (currentUsersQbPage != 0) {
                    currentUsersQbPage--;
                }
                ErrorUtils.showErrorDialog(NewDialogActivity.this, getString(R.string.new_dialog_get_users_error), errors);
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
