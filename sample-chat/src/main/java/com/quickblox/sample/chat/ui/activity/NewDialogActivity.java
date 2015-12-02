package com.quickblox.sample.chat.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class NewDialogActivity extends BaseActivity implements QBEntityCallback<ArrayList<QBUser>> {

    private static final int PAGE_SIZE = 10;

    private int listViewIndex;
    private int listViewTop;
    private int currentPage;
    private List<QBUser> users = new ArrayList<>();

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

        usersList = (PullToRefreshListView) findViewById(R.id.list_dialog_users);
        progressBar = (ProgressBar) findViewById(R.id.progress_chat_messages);

        Button createChatButton = (Button) findViewById(R.id.button_dialog_create_chat);
        createChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialogWithSelectedUsers();
            }
        });

        usersList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                loadNextPage();
                listViewIndex = usersList.getRefreshableView().getFirstVisiblePosition();
                View v = usersList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        if (isSessionActive()) {
            loadNextPage();
        }
    }

    private void createDialogWithSelectedUsers() {
        ChatHelper.getInstance().addDialogsUsers(usersAdapter.getSelectedUsers());

        // Create new group dialog
        QBDialog dialogToCreate = new QBDialog();
        dialogToCreate.setName(createChatNameFromUserList());
        if (usersAdapter.getSelectedUsers().size() == 1) {
            dialogToCreate.setType(QBDialogType.PRIVATE);
        } else {
            dialogToCreate.setType(QBDialogType.GROUP);
        }
        dialogToCreate.setOccupantsIds(getUserIds(usersAdapter.getSelectedUsers()));

        QBChatService.getInstance().getGroupChatManager().createDialog(dialogToCreate,
                new QBEntityCallbackImpl<QBDialog>() {
                    @Override
                    public void onSuccess(QBDialog dialog, Bundle args) {
                        ChatActivity.start(NewDialogActivity.this, dialog);
                    }

                    @Override
                    public void onError(List<String> errors) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(NewDialogActivity.this);
                        dialog.setMessage("dialog creation errors: " + errors).create().show();
                    }
                }
        );
    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }

    @Override
    public void onSuccess(ArrayList<QBUser> newUsers, Bundle bundle) {

        // save users
        //
        users.addAll(newUsers);

        // Prepare users list for simple adapter.
        //
        usersAdapter = new UsersAdapter(this, users);
        usersList.setAdapter(usersAdapter);
        usersList.onRefreshComplete();
        usersList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);

        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(List<String> errors) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("get users errors: " + errors).create().show();
    }

    private String createChatNameFromUserList() {
        String chatName = "";
        for (QBUser user : usersAdapter.getSelectedUsers()) {
            String prefix = chatName.equals("") ? "" : ", ";
            chatName = chatName + prefix + user.getLogin();
        }
        return chatName;
    }

    private void loadNextPage() {
        currentPage++;
        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), this);
    }

    public static ArrayList<Integer> getUserIds(List<QBUser> users) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (QBUser user : users) {
            ids.add(user.getId());
        }
        return ids;
    }

    //
    // ApplicationSessionStateCallback
    //

    @Override
    public void onSessionRecreationFinish(final boolean success) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (success) {
                    loadNextPage();
                }
            }
        });
    }
}
