package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.NewDialogAdapter;
import com.quickblox.chat_v2.widget.TopBar;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.result.Result;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserPagedResult;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 4/11/13 Time: 5:07
 * PM
 */
public class NewDialogActivity extends Activity {
	
	private TopBar topBar;
	private ListView contactList;
	private Button searchBtn;
	private TextView contactName;
	
	private NewDialogAdapter newDialogAdapter;
	
	private ProgressDialog progress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_dialog_layout);
		initViews();
	}
	
	private void initViews() {
		topBar = (TopBar) findViewById(R.id.top_bar);
		topBar.setFragmentParams(TopBar.NEW_DIALOG_ACTIVITY, false);
		contactList = (ListView) findViewById(R.id.contacts_listView);
		searchBtn = (Button) findViewById(R.id.search_button);
		searchBtn.setOnClickListener(searchBtnClickListener);
		contactName = (EditText) findViewById(R.id.contact_name);
	}
	
	View.OnClickListener searchBtnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!TextUtils.isEmpty(contactName.getText().toString())) {
				getContactList();
				blockUi(true);
			}
		}
	};
	
	private void getContactList() {
		QBUsers.getUsersByFullName(contactName.getText().toString(), new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				if (result.isSuccess()) {
					refreshContactList(((QBUserPagedResult) result).getUsers());
					blockUi(false);
				} else {
					blockUi(false);
				    Toast.makeText(NewDialogActivity.this, getResources().getString(R.string.dialog_activity_reject), Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private void refreshContactList(ArrayList<QBUser> qbUsers) {
		if (qbUsers != null) {
			newDialogAdapter = new NewDialogAdapter(this, qbUsers);
			contactList.setAdapter(newDialogAdapter);
		}
	}
	
	public void blockUi(boolean enable) {
		if (enable) {
			progress = ProgressDialog.show(this, getResources().getString(R.string.app_name), getResources().getString(R.string.dialog_activity_search_dialog), true);
		} else {
			progress.dismiss();
		}
	}
}
