package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.ContactsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnContactRefreshListener;
import com.quickblox.chat_v2.interfaces.OnUserProfileDownloaded;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.model.QBUser;

import java.util.ArrayList;

public class ContactsActivity extends ListActivity implements OnUserProfileDownloaded, OnContactRefreshListener {

    private ChatApplication app;

    private ArrayList<QBUser> contactsList;

    private ListView contactsTable;
    private ContactsAdapter contactsAdapter;
    private TextView mEmtyListLabel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        app = ChatApplication.getInstance();
        contactsTable = (ListView) findViewById(android.R.id.list);
        mEmtyListLabel = (TextView) findViewById(R.id.emty_contactList);
        contactsTable.setClickable(true);

        contactsTable.setOnItemClickListener(onClicListener);
        mEmtyListLabel.setText(getString(R.string.empty_contacts));
    }

    private OnItemClickListener onClicListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
            int tmpId = 0;
            String tmpDialogId = null;
            QBUser qb = (QBUser) parent.getItemAtPosition(position);

            i.putExtra(GlobalConsts.USER_ID, String.valueOf(qb.getId()));
            tmpId = contactsList.get(position).getId();

            for (QBCustomObject dialogs : new ArrayList<QBCustomObject>(app.getDialogMap().values())) {
                if (Integer.parseInt(dialogs.getFields().get(GlobalConsts.RECEPIENT_ID_FIELD).toString()) == tmpId) {
                    tmpDialogId = dialogs.getCustomObjectId();
                }

            }
            i.putExtra(GlobalConsts.DIALOG_ID, tmpDialogId);
            i.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.CONTACTS_ACTIVITY);

            startActivity(i);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        rebuildAdapterData();
    }

    private void rebuildAdapterData() {
        contactsList = new ArrayList<QBUser>(app.getContactsMap().values());
        app.getQbm().setUserProfileListener(this);
        app.getRstManager().setOnContactRefreshListener(this);

        setCurrentListInAdapter(contactsList);

        if (app.getContactsMap().isEmpty()) {
            contactsTable.setVisibility(View.INVISIBLE);
            mEmtyListLabel.setVisibility(View.VISIBLE);
        } else {
            contactsTable.setVisibility(View.VISIBLE);
            mEmtyListLabel.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void downloadComlete(QBUser friend) {

        if (friend == null) {
            return;
        }

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                contactsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setCurrentListInAdapter(ArrayList<QBUser> pCurrentArrayList) {
        contactsAdapter = new ContactsAdapter(ContactsActivity.this, pCurrentArrayList, false);
        setListAdapter(contactsAdapter);
    }

    @Override
    public void reSetCurrentList() {

    }

    @Override
    public void reFreshCurrentList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rebuildAdapterData();
            }
        });
    }
}
