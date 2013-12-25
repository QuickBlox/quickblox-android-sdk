package com.quickblox.sample.chat.ui.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.core.SingleChat;
import com.quickblox.sample.chat.ui.activities.ChatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UsersFragment extends Fragment implements QBCallback {

    private ListView usersList;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_users, container, false);
        usersList = (ListView) v.findViewById(R.id.usersList);
        return v;
    }

    public void updateData() {
        if (getActivity() != null) {
            progressDialog = ProgressDialog.show(getActivity(), null, "Loading fiends list");
        }
        QBUsers.getUsers(this);
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
            for (QBUser u : users) {
                Map<String, String> umap = new HashMap<String, String>();
                umap.put("userLogin", u.getLogin());
                usersListForAdapter.add(umap);
            }

            // Put users list into adapter.
            SimpleAdapter usersAdapter = new SimpleAdapter(getActivity(), usersListForAdapter,
                    R.layout.list_item_user,
                    new String[]{"userLogin"},
                    new int[]{R.id.userLogin});

            usersList.setAdapter(usersAdapter);
            usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    // Prepare QBUser objects to pass it into next activities using bundle.
                    QBUser friendUser = users.get(position);

                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra(ChatActivity.MODE, ChatActivity.Mode.SINGLE);
                    intent.putExtra(SingleChat.USER_ID, friendUser.getId());

                    startActivity(intent);
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
}
