package com.quickblox.chat_v2.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.danikula.aibolit.Aibolit;
import com.danikula.aibolit.annotation.InjectView;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.widget.TopBar;

/**
 * Created with IntelliJ IDEA. User: nickolas Date: 05.04.13 Time: 9:58
 */
public class ContactsFragment extends Fragment {


    private static final String FRAGMENT_NAME = "Contacts";
    @InjectView(R.id.top_bar)
    TopBar topBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        View view = inflater.inflate(R.layout.fragment_contacts, null);
        Aibolit.doInjections(this, view);
        topBar.setFragmentName(FRAGMENT_NAME);
        return view;
    }

//	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		View v = inflater.inflate(R.layout.fragment_contacts, null);
//		app = ChatApplication.getInstance();
//
//		contactsButton = (Button) v.findViewById(R.id.dialogs_new_button);
//		requestButton = (Button) v.findViewById(R.id.contacts_request_button);
//
//
//		OnClickListener oclBtn = new OnClickListener() {
//
//
//			public void onClick(View v) {
//				switch (v.getId()) {
//					case R.id.dialogs_new_button :
//						contactsButton.setActivated(false);
//						requestButton.setActivated(true);
//						//to-do
//						break;
//					case R.id.contacts_request_button :
//						contactsButton.setActivated(true);
//						requestButton.setActivated(false);
//						//to-do
//						break;
//				}
//			}
//		};
//
//		contactsButton.setOnClickListener(oclBtn);
//		requestButton.setOnClickListener(oclBtn);
//
//		return v;
//	}
}
