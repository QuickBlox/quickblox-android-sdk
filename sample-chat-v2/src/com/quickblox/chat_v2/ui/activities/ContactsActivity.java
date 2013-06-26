package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RadioGroup;

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

    private boolean isContactButtonEnable;

    private ArrayList<QBUser> contactsList;
    private ArrayList<QBUser> mContactsCandidateList;
    private RadioGroup mRadioGroup;

    private ListView contactsTable;
    private ContactsAdapter contactsAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        app = ChatApplication.getInstance();

        mRadioGroup = (RadioGroup) findViewById(R.id.contact_radio_group);

        contactsTable = (ListView) findViewById(android.R.id.list);
        contactsTable.setClickable(true);

        isContactButtonEnable = true;


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup pRadioGroup, int i) {
                switch (i) {
                    case R.id.contacts_contact_button:
                        setCurrentListInAdapter(true, contactsList);
                        isContactButtonEnable = true;
                        break;

                    case R.id.contacts_request_button:
                        setCurrentListInAdapter(false, mContactsCandidateList);
                        isContactButtonEnable = false;
                        break;
                }
            }
        });

        contactsTable.setOnItemClickListener(onClicListener);
    }

    private OnItemClickListener onClicListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
            int tmpId = 0;
            String tmpDialogId = null;
            String tmpFullName = null;
            QBUser qb = (QBUser) parent.getItemAtPosition(position);

            if (isContactButtonEnable) {
                i.putExtra(GlobalConsts.ARRAY_TYPE, GlobalConsts.CONTACTS_ARRAY);
                i.putExtra(GlobalConsts.USER_ID, String.valueOf(qb.getId()));
                tmpId = contactsList.get(position).getId();
            } else {
                i.putExtra(GlobalConsts.ARRAY_TYPE, GlobalConsts.CONTACTS_CANDIDATE_ARRAY);
                i.putExtra(GlobalConsts.USER_ID, String.valueOf(qb.getId()));
                tmpId = qb.getId();
            }

            for (QBCustomObject dialogs : app.getDialogList()) {
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

        contactsList = new ArrayList<QBUser>(app.getContactsMap().values());
        mContactsCandidateList = new ArrayList<QBUser>(app.getContactsCandidateMap().values());

        contactsAdapter = new ContactsAdapter(this, contactsList, true, false);
        setListAdapter(contactsAdapter);


        app.getQbm().setUserProfileListener(this);
        app.getRstManager().setOnContactRefreshListener(this);
        if (isContactButtonEnable) {
            setCurrentListInAdapter(true, contactsList);
        } else {
            setCurrentListInAdapter(false, mContactsCandidateList);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
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

    private void setCurrentListInAdapter(boolean pIsContacts, ArrayList<QBUser> pCurrentArrayList) {
        contactsAdapter = new ContactsAdapter(ContactsActivity.this, pCurrentArrayList, pIsContacts, false);
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
                mContactsCandidateList = new ArrayList<QBUser>(app.getContactsCandidateMap().values());
                setCurrentListInAdapter(false, mContactsCandidateList);
            }
        });
    }
}
