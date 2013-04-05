package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.others.ChatApplication;

/**
 * Created with IntelliJ IDEA. User: nickolas Date: 05.04.13 Time: 9:58
 */
public class ContactsFragment extends Fragment {
	
	private ChatApplication app;
	
	private Button contactsButton;
	private Button requestButton;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_contacts, null);
		app = ChatApplication.getInstance();
		
		contactsButton = (Button) v.findViewById(R.id.contacts_contact_button);
		requestButton = (Button) v.findViewById(R.id.contacts_request_button);
		
		
		OnClickListener oclBtn = new OnClickListener() {
			
		
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.contacts_contact_button :
						contactsButton.setActivated(false);
						requestButton.setActivated(true);
						//to-do
						break;
					case R.id.contacts_request_button :
						contactsButton.setActivated(true);
						requestButton.setActivated(false);
						//to-do
						break;
				}
			}
		};
		
		contactsButton.setOnClickListener(oclBtn);
		requestButton.setOnClickListener(oclBtn);
		
		return v;
	}
}
