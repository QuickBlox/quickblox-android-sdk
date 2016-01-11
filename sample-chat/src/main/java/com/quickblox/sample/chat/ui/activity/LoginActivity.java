package com.quickblox.sample.chat.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.core.QBEntityCallbackImpl;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.adapter.UsersAdapter;
import com.quickblox.sample.chat.utils.Consts;
import com.quickblox.sample.chat.utils.SharedPreferencesUtil;
import com.quickblox.sample.chat.utils.chat.ChatHelper;
import com.quickblox.sample.core.ui.activity.CoreBaseActivity;
import com.quickblox.sample.core.ui.dialog.ProgressDialogFragment;
import com.quickblox.sample.core.utils.ErrorUtils;
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

        TextView listHeader = (TextView) LayoutInflater.from(this).inflate(R.layout.include_list_hint_header, userListView, false);
        listHeader.setText(R.string.login_select_user_for_login);

        userListView.addHeaderView(listHeader, null, false);
        userListView.setOnItemClickListener(new OnUserLoginItemClickListener());

        buildUsersList();
    }

    private void buildUsersList() {
        List<String> tags = new ArrayList<>();
        tags.add(Consts.USERS_TAG);

        QBUsers.getUsersByTags(tags, null, new QBEntityCallbackImpl<ArrayList<QBUser>>() {
            @Override
            public void onSuccess(ArrayList<QBUser> result, Bundle params) {
                UsersAdapter adapter = new UsersAdapter(LoginActivity.this, result);
                userListView.setAdapter(adapter);
            }

            @Override
            public void onError(List<String> errors) {
                ErrorUtils.showErrorDialog(LoginActivity.this, R.string.login_cant_obtain_users, errors);
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

            ProgressDialogFragment.show(getSupportFragmentManager(), R.string.dlg_login);

            final QBUser user = (QBUser) parent.getItemAtPosition(position);
            // We use hardcoded password for all users for test purposes
            // Of course you shouldn't do that in your app
            user.setPassword(Consts.USERS_PASSWORD);

            ChatHelper.getInstance().login(user, new QBEntityCallbackImpl<Void>() {
                @Override
                public void onSuccess() {
                    SharedPreferencesUtil.saveQbUser(user);

                    DialogsActivity.start(LoginActivity.this);
                    finish();

                    ProgressDialogFragment.hide(getSupportFragmentManager());
                }

                @Override
                public void onError(List<String> errors) {
                    ProgressDialogFragment.hide(getSupportFragmentManager());
                    ErrorUtils.showErrorDialog(LoginActivity.this, R.string.login_chat_login_error, errors);
                }
            });
        }
    }
}
