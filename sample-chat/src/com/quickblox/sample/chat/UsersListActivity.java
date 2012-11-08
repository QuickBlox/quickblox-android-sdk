package com.quickblox.sample.chat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.quickblox.core.QBCallback;
import com.quickblox.core.result.Result;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 24.10.12
 * Time: 22:16
 */

/**
 * List of users QuickBlox applications available to chat.
 *
 * @author <a href="mailto:oleg@quickblox.com">Oleg Soroka</a>
 */
public class UsersListActivity extends Activity implements QBCallback {

    private ListView usersList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.users_list);

        usersList = (ListView) findViewById(R.id.usersList);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading fiends list");
        progressDialog.show();

        // ================= QuickBlox ===== Step 4 =================
        // Get all users of QB application.
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
                umap.put("chatLogin", QBChat.getChatLoginFull(u));
                usersListForAdapter.add(umap);
            }

            // Put users list into adapter.
            SimpleAdapter usersAdapter = new SimpleAdapter(this, usersListForAdapter,
                    android.R.layout.simple_list_item_2,
                    new String[]{"userLogin", "chatLogin"},
                    new int[]{android.R.id.text1, android.R.id.text2});

            usersList.setAdapter(usersAdapter);
            usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    // Prepare QBUser objects to pass it into next activities using bundle.
                    QBUser friendUser = users.get(i);

                    Intent intent = new Intent(UsersListActivity.this, ChatActivity.class);
                    Bundle extras = getIntent().getExtras();
                    intent.putExtra("friendId", friendUser.getId());
                    intent.putExtra("friendLogin", friendUser.getLogin());
                    intent.putExtra("friendPassword", friendUser.getPassword());
                    // Add extras from previous activity.
                    intent.putExtras(extras);

                    startActivity(intent);
                }
            });
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Error(s) occurred. Look into DDMS log for details, " +
                    "please. Errors: " + result.getErrors()).create().show();
        }
    }

    @Override
    public void onComplete(Result result, Object context) { }
}