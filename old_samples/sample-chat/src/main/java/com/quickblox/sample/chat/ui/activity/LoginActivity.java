package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.ErrorUtils;
import com.quickblox.sample.core.utils.SharedPrefsHelper;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends CoreBaseActivity {

    private ListView userListView;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userListView = _findViewById(R.id.list_login_users);

        TextView listHeader = (TextView) LayoutInflater.from(this)
                .inflate(R.layout.include_list_hint_header, userListView, false);
        listHeader.setText(R.string.login_select_user_for_login);

        userListView.addHeaderView(listHeader, null, false);
        userListView.setOnItemClickListener(new OnUserLoginItemClickListener());

        buildUsersList();
    }

    private void buildUsersList() {
        List<String> tags = new ArrayList<>();

        tags.add(App.getSampleConfigs().getUsersTag());

        QBUsers.getUsersByTags(tags, null).performAsync(new QBEntityCallback<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                UsersAdapter adapter = new UsersAdapter(LoginActivity.this, result);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onError(QBResponseException e) {
                ErrorUtils.showSnackbar(userListView, R.string.login_cant_obtain_users, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                buildUsersList();
                            }
                        });
            }
        });
    }

    private void login(final QBUser user) {
        ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_login);
        ChatHelper.getInstance().login(user, new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void result, Bundle bundle) {
                SharedPrefsHelper.getInstance().saveQbUser(user);
                DialogsActivity.start(LoginActivity.this);
                finish();

                ProgressDialogFragment.hide(getSupportFragmentManager());
            }

            @Override
            public void onError(QBResponseException e) {
                ProgressDialogFragment.hide(getSupportFragmentManager());
                ErrorUtils.showSnackbar(userListView, R.string.login_chat_login_error, e,
                        R.string.dlg_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                login(user);
                            }
                        });
            }
        });
    }

    private class OnUserLoginItemClickListener implements AdapterView.OnItemClickListener {

        public static final int LIST_HEADER_POSITION = 0;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == LIST_HEADER_POSITION) {
                return;
            }


            final QBUser user = (QBUser) parent.getItemAtPosition(position);
            // We use hardcoded password for all users for test purposes
            // Of course you shouldn't do that in your app
            user.setPassword(App.getSampleConfigs().getUsersPassword());

            login(user);
        }

    }
}
