package com.quickblox.chat_v2.ui.activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.ContactsAdapter;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/12/13 Time: 4:39
 * PM
 */
public class ContactsActivity extends ListActivity{
	
	private ListView contactsTable;
	private ContactsAdapter contactsAdapter;
	private Button contactsButton;
	private Button requestButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacts);
		
		contactsTable = (ListView) findViewById(android.R.id.list);
		contactsTable.setClickable(true);
		
		contactsButton = (RadioButton) findViewById(R.id.contacts_contact_button);
		requestButton = (RadioButton) findViewById(R.id.contacts_request_button);
		
		//contactsAdapter = new ContactsAdapter(this, ChatApplication.getInstance().getContactUserList(), true);
		setListAdapter(contactsAdapter);
		
		OnClickListener oclBtn = new OnClickListener() {


			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.contacts_contact_button :
						System.out.println("Cont");
			//			contactsAdapter = new ContactsAdapter(ContactsActivity.this, ChatApplication.getInstance().getContactUserList(), true);
						contactsAdapter.notifyDataSetChanged();
						break;
					case R.id.contacts_request_button :
						System.out.println("req");
				//		contactsAdapter = new ContactsAdapter(ContactsActivity.this, ChatApplication.getInstance().getSubscribeUserList(), false);
						contactsAdapter.notifyDataSetChanged();
						break;
				}
			}
		};

		contactsButton.setOnClickListener(oclBtn);
		requestButton.setOnClickListener(oclBtn);
		contactsTable.setOnItemClickListener(onClicListener);
		
	}
	
	private OnItemClickListener onClicListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent i = new Intent(ContactsActivity.this, ChatActivity.class);
			startActivity(i);
		}
	};
}
