package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.adapters.DialogsAdapter;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnDialogListRefresh;
import com.quickblox.chat_v2.utils.GlobalConsts;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 11.04.13 Time: 9:58
 */
public class DialogsActivity extends Activity implements OnDialogListRefresh {
	
	private ProgressDialog progressDialog;
	
	private ListView dialogsListView;
	private DialogsAdapter dialogsAdapter;
	private Button newDialogButton;
	
	private ChatApplication app;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_list_layout);
		app = ChatApplication.getInstance();
		initialize();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		app.getMsgManager().setDialogRefreshListener(this);
		app.getMsgManager().downloadDialogList(true);
	}
	
	private void initialize() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setCancelable(false);
		progressDialog.setMessage(getString(R.string.loading));
		dialogsListView = (ListView) findViewById(R.id.dialogs_listView);
		newDialogButton = (Button) findViewById(R.id.new_dialog_button);
		newDialogButton.setOnClickListener(newDialogButtonClickListener);
	}
	
	View.OnClickListener newDialogButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getBaseContext(), NewDialogActivity.class);
			intent.putExtra(GlobalConsts.PREVIOUS_ACTIVITY, GlobalConsts.DIALOG_ACTIVITY);
			startActivity(intent);
		}
	};
	
	private void applyDialogList() {
		if (ChatApplication.getInstance().getDialogList() != null) {
			dialogsAdapter = new DialogsAdapter(DialogsActivity.this, ChatApplication.getInstance().getDialogList());
			dialogsListView.setAdapter(dialogsAdapter);

		} else {
			app.getMsgManager().downloadDialogList(true);
		}
	}
	
	@Override
	public void refreshList() {
		applyDialogList();
	}
}
