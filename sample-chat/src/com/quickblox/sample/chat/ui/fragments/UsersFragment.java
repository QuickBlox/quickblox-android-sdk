package com.quickblox.sample.chat.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;
import com.quickblox.sample.chat.App;
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.core.SingleChat;
import com.quickblox.sample.chat.ui.activities.ChatActivity;
import com.quickblox.sample.chat.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersFragment extends Fragment implements UpdateableFragment, QBCallback {

    private static final String KEY_USER_LOGIN = "userLogin";
    private ListView usersList;
    private ProgressDialog progressDialog;
    private QBUser companionUser;

    public static UsersFragment getInstance() {
        UsersFragment usersFragment = new UsersFragment();
        return usersFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        usersList = (ListView) v.findViewById(R.id.usersList);
        return v;
    }

    @Override
    public void onComplete(Result result) {
        if (result.isSuccess()) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            // Cast 'result' to specific result class QBUserPagedResult.
            QBUserPagedResult pagedResult = (QBUserPagedResult) result;
            final ArrayList<QBUser> users = pagedResult.getUsers();

            // Prepare users list for simple adapter.
            ArrayList<Map<String, String>> usersListForAdapter = new ArrayList<Map<String, String>>();
            for (QBUser user : users) {
                Map<String, String> userMap = new HashMap<String, String>();
                userMap.put(KEY_USER_LOGIN, user.getLogin());
                usersListForAdapter.add(userMap);
            }

            // Put users list into adapter.
            SimpleAdapter usersAdapter = new SimpleAdapter(getActivity(), usersListForAdapter,
                    R.layout.list_item_user,
                    new String[]{KEY_USER_LOGIN},
                    new int[]{R.id.userLogin});

            usersList.setAdapter(usersAdapter);
            usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    companionUser = users.get(position);
                    if (App.getInstance().getQbUser() != null) {
                        startChat();
                    } else {
                        MainActivity activity = (MainActivity) getActivity();
                        activity.setLastAction(MainActivity.Action.CHAT);
                        activity.showAuthenticateDialog();
                    }
                }
            });
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
    }

    @Override
    public void onComplete(Result result, Object context) {

    }

    @Override
    public void updateData() {
        if (getActivity() != null) {
            progressDialog = ProgressDialog.show(getActivity(), null, "Loading fiends list");
        }
        QBUsers.getUsers(this);
    }

    public void startChat() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ChatActivity.EXTRA_MODE, ChatActivity.Mode.SINGLE);
        bundle.putInt(SingleChat.EXTRA_USER_ID, companionUser.getId());
        ChatActivity.start(getActivity(), bundle);
    }
}
