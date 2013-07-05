package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.ContactsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnContactRefreshListener;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.ContextForDownloadUser;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends ListActivity implements OnUserProfileDownloaded, OnContactRefreshListener {

    private ChatApplication app;

    private List<QBUser> contactsList;

    private ContactsAdapter contactsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        app = ChatApplication.getInstance();

        View textView = findViewById(android.R.id.empty);
        getListView().setEmptyView(textView);
        rebuildAdapterData();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        QBUser user = (QBUser) l.getItemAtPosition(position);

        int userId = user.getId();


        String dialog = app.getDialogByUser(userId);

        Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
        i.putExtra(GlobalConsts.USER_ID, String.valueOf(userId));
        i.putExtra(GlobalConsts.DIALOG_ID, dialog);
        i.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.CONTACTS_ACTIVITY);

        startActivity(i);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    private void rebuildAdapterData() {
        contactsList = new ArrayList<QBUser>(app.getContactsMap().values());
        contactsAdapter = new ContactsAdapter(this, contactsList, false);
        setListAdapter(contactsAdapter);
    }


    @Override
    public void downloadComplete(QBUser friend, ContextForDownloadUser pContextForDownloadUser) {

        if (pContextForDownloadUser == ContextForDownloadUser.DOWNLOAD_FOR_CONTACTS) {
            this.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    contactsAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onRefreshCurrentList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                contactsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        app.getQbm().addUserProfileListener(this);
        app.getRstManager().setOnContactRefreshListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        app.getQbm().removeUserProfileListener(this);
        app.getRstManager().setOnContactRefreshListener(null);
    }

    private void refreshList() {
        contactsList.clear();
        contactsList.addAll(app.getContactsMap().values());
        contactsAdapter.notifyDataSetChanged();
    }
}
