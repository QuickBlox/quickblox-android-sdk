package com.quickblox.sample.chat.java.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.GenericQueryRule;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.ui.adapter.CheckboxUsersAdapter;
import com.quickblox.sample.chat.java.ui.adapter.ScrollViewWithMaxHeight;
import com.quickblox.sample.chat.java.utils.ToastUtils;
import com.quickblox.sample.chat.java.utils.UiUtils;
import com.quickblox.sample.chat.java.utils.chat.ChatHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;

public class SelectUsersActivity extends BaseActivity {
    private static final String EXTRA_QB_DIALOG = "qb_dialog";
    public static final String EXTRA_QB_USERS = "qb_users";
    public static final String EXTRA_CHAT_NAME = "chat_name";
    private static final int REQUEST_DIALOG_NAME = 135;
    public static final int MINIMUM_CHAT_OCCUPANTS_SIZE = 1;
    public static final int PRIVATE_CHAT_OCCUPANTS_SIZE = 2;
    private static final int USERS_PAGE_SIZE = 100;
    private static final int MIN_SEARCH_QUERY_LENGTH = 3;
    private static final long SEARCH_DELAY = 600;
    private static final String ORDER_VALUE_UPDATED_AT = "desc string updated_at";

    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);

    private ListView usersListView;
    private ProgressBar progressBar;
    private SearchView searchView;
    private Menu menu;
    private ScrollViewWithMaxHeight scrollView;
    private ChipGroup chipGroup;
    private TextView tvNotFound;
    private CheckboxUsersAdapter usersAdapter;
    private Set<QBUser> existingUsers = new HashSet<>();
    private long lastClickTime = 0L;
    private QBChatDialog qbChatDialog = null;
    private String chatName = null;
    private int currentPage = 0;
    private Boolean isLoading = false;
    private String lastSearchQuery = "";
    private Boolean hasNextPage = true;

    public static void startForResult(Activity activity, int code, QBChatDialog dialog) {
        Intent intent = new Intent(activity, SelectUsersActivity.class);
        intent.putExtra(EXTRA_QB_DIALOG, dialog);
        activity.startActivityForResult(intent, code);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_users);

        if (getIntent() != null && getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null) {
            qbChatDialog = (QBChatDialog) getIntent().getSerializableExtra(EXTRA_QB_DIALOG);
        }
        initUi();

        if (qbChatDialog != null) {
            updateDialog();
        } else {
            loadUsersFromQB(null);
        }
    }

    private void initUi() {
        progressBar = findViewById(R.id.progress_select_users);
        usersListView = findViewById(R.id.list_select_users);
        searchView = findViewById(R.id.search);
        scrollView = findViewById(R.id.scroll_view);
        scrollView.setMaxHeight(225);
        chipGroup = findViewById(R.id.chips);
        tvNotFound = findViewById(R.id.tv_no_users_found);
        usersAdapter = new CheckboxUsersAdapter(this, new ArrayList<QBUser>());
        usersListView.setAdapter(usersAdapter);

        searchView.setOnQueryTextListener(new SearchQueryListener());

        boolean editingChat = getIntent().getSerializableExtra(EXTRA_QB_DIALOG) != null;
        if (editingChat) {
            getSupportActionBar().setTitle(getString(R.string.select_users_edit_chat));
        } else {
            getSupportActionBar().setTitle(getString(R.string.select_users_create_chat_title));
            getSupportActionBar().setSubtitle(getString(R.string.select_users_create_chat_subtitle, "0"));
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                usersAdapter.onItemClicked(position, view, parent);
                menu.getItem(0).setVisible(usersAdapter.getSelectedUsers().size() >= 1);

                String subtitle = "";
                if (usersAdapter.getSelectedUsers().size() != 1) {
                    subtitle = getString(R.string.select_users_create_chat_subtitle, String.valueOf(usersAdapter.getSelectedUsers().size()));
                } else {
                    subtitle = getString(R.string.select_users_create_chat_subtitle_single, "1");
                }
                getSupportActionBar().setSubtitle(subtitle);

                chipGroup.removeAllViews();
                for (QBUser user : usersAdapter.getSelectedUsers()) {
                    Chip chip = new Chip(SelectUsersActivity.this);
                    chip.setText(user.getFullName());
                    chip.setChipIcon(UiUtils.getColorCircleDrawable(user.getId().hashCode()));
                    chip.setCloseIconVisible(false);
                    chip.setCheckable(false);
                    chip.setClickable(false);
                    chipGroup.addView(chip);
                    chipGroup.setVisibility(View.VISIBLE);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                }
                if (usersAdapter.getSelectedUsers().size() == 0) {
                    chipGroup.setVisibility(View.GONE);
                }
            }
        });

        usersListView.setOnScrollListener(new ScrollListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_activity_select_users, menu);
        if (qbChatDialog != null) {
            menu.getItem(0).setTitle(R.string.menu_done);
        }
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
                    if (usersAdapter.getSelectedUsers().size() < MINIMUM_CHAT_OCCUPANTS_SIZE) {
                        ToastUtils.shortToast(R.string.select_users_choose_users);
                    } else {
                        if (qbChatDialog == null && usersAdapter.getSelectedUsers().size() >= PRIVATE_CHAT_OCCUPANTS_SIZE) {
                            NewGroupActivity.startForResult(SelectUsersActivity.this, REQUEST_DIALOG_NAME);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null && !TextUtils.isEmpty(data.getSerializableExtra(EXTRA_CHAT_NAME).toString())) {
                chatName = data.getSerializableExtra(EXTRA_CHAT_NAME).toString();
            }
            passResultToCallerActivity();
        }
    }

    private void passResultToCallerActivity() {
        Intent result = new Intent();
        ArrayList<QBUser> selectedUsers = new ArrayList<>(usersAdapter.getSelectedUsers());
        result.putExtra(EXTRA_QB_USERS, selectedUsers);
        if (!TextUtils.isEmpty(chatName)) {
            result.putExtra(EXTRA_CHAT_NAME, chatName);
        }
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    private void updateDialog() {
        showProgressDialog(R.string.dlg_loading);
        String dialogID = qbChatDialog.getDialogId();
        ChatHelper.getInstance().getDialogById(dialogID, new QBEntityCallback<QBChatDialog>() {
            @Override
            public void onSuccess(QBChatDialog qbChatDialog, Bundle bundle) {
                SelectUsersActivity.this.qbChatDialog = qbChatDialog;
                loadUsersFromDialog();
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                showErrorSnackbar(R.string.select_users_get_dialog_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDialog();
                    }
                });
            }
        });
    }

    private void loadUsersFromDialog() {
        ChatHelper.getInstance().getUsersFromDialog(qbChatDialog, new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> usersFromDialog, Bundle bundle) {
                if (usersFromDialog != null) {
                    existingUsers.addAll(usersFromDialog);
                }
                loadUsersFromQB(null);
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                showErrorSnackbar(R.string.select_users_get_users_dialog_error, e, null);
            }
        });
    }

    private void loadUsersFromQB(String query) {
        if (!isProgressDialogShowing()) {
            enableProgress();
        }
        currentPage += 1;
        ArrayList<GenericQueryRule> rules = new ArrayList<>();
        rules.add(new GenericQueryRule(ChatActivity.ORDER_RULE, ORDER_VALUE_UPDATED_AT));

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setRules(rules);
        requestBuilder.setPerPage(USERS_PAGE_SIZE);
        requestBuilder.setPage(currentPage);

        if (TextUtils.isEmpty(query)) {
            loadUsersWithoutQuery(requestBuilder);
        } else {
            loadUsersByQuery(query, requestBuilder);
        }
    }

    private void loadUsersWithoutQuery(final QBPagedRequestBuilder requestBuilder) {
        QBUsers.getUsers(requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> usersList, Bundle params) {
                tvNotFound.setVisibility(View.INVISIBLE);
                int totalPagesFromParams = (int) params.get(ChatHelper.TOTAL_PAGES_BUNDLE_PARAM);
                if (currentPage >= totalPagesFromParams) {
                    hasNextPage = false;
                }
                if (qbChatDialog != null) {
                    usersList.removeAll(existingUsers);
                }
                if (currentPage == 1) {
                    usersAdapter.addNewList(usersList);
                } else {
                    usersAdapter.addUsers(usersList);
                }
                disableProgress();
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                currentPage -= 1;
                showErrorSnackbar(R.string.select_users_get_users_error, e, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        loadUsersWithoutQuery(requestBuilder);
                    }
                });
            }
        });
    }

    private void loadUsersByQuery(final String query, final QBPagedRequestBuilder requestBuilder) {
        QBUsers.getUsersByFullName(query, requestBuilder).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> qbUsers, Bundle params) {
                int totalPagesFromParams = (int) params.get(ChatHelper.TOTAL_PAGES_BUNDLE_PARAM);
                if (currentPage >= totalPagesFromParams) {
                    hasNextPage = false;
                }
                if (qbUsers != null) {
                    tvNotFound.setVisibility(View.INVISIBLE);
                    if (qbChatDialog != null) {
                        qbUsers.removeAll(existingUsers);
                    }
                    if (currentPage == 1) {
                        usersAdapter.addNewList(qbUsers);
                        usersListView.smoothScrollToPosition(0);
                    } else {
                        usersAdapter.addUsers(qbUsers);
                    }
                } else {
                    usersAdapter.clearList();
                    tvNotFound.setVisibility(View.VISIBLE);
                }
                disableProgress();
            }

            @Override
            public void onError(QBResponseException e) {
                disableProgress();
                if (e.getHttpStatusCode() == 404) {
                    usersAdapter.clearList();
                    tvNotFound.setVisibility(View.VISIBLE);
                } else {
                    currentPage -= 1;
                    showErrorSnackbar(R.string.select_users_get_users_error, e, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadUsersByQuery(query, requestBuilder);
                        }
                    });
                }
            }
        });
    }

    private void enableProgress() {
        progressBar.setVisibility(View.VISIBLE);
        isLoading = true;
    }

    private void disableProgress() {
        hideProgressDialog();
        progressBar.setVisibility(View.GONE);
        isLoading = false;
    }

    private class SearchQueryListener implements SearchView.OnQueryTextListener {
        private Timer timer = new Timer();

        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(final String newText) {
            if (newText != null) {
                timer.cancel();
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                currentPage = 0;
                                hasNextPage = true;
                                lastSearchQuery = newText;
                                if (newText.length() >= MIN_SEARCH_QUERY_LENGTH) {
                                    loadUsersFromQB(newText);
                                }
                                if (newText.isEmpty()) {
                                    loadUsersFromQB(null);
                                }
                            }
                        });

                    }
                }, SEARCH_DELAY);
            }
            return false;
        }
    }

    private class ScrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (!isLoading && totalItemCount > 0 && (firstVisibleItem + visibleItemCount * 3) >= totalItemCount && hasNextPage) {
                if (TextUtils.isEmpty(lastSearchQuery)) {
                    loadUsersFromQB(null);
                } else {
                    loadUsersFromQB(lastSearchQuery);
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }
    }
}