package com.quickblox.chat_v2.ui.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.ui.activities.SplashActivity;
import com.quickblox.core.QBCallback;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

public class SplashDialog extends DialogFragment implements OnClickListener {
	
	private EditText inputNameField;
	private EditText inputPasswordField;
	
	private boolean mode;
	private Context context;
	
	public SplashDialog(boolean isRegistration, Context context) {
		mode = isRegistration;
		this.context = context;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		getDialog().setTitle(mode ? R.string.splash_dialog_name_1 : R.string.splash_dialog_name_2);
		
		View v = inflater.inflate(R.layout.dialog_splash_login, null);
		
		inputNameField = (EditText) v.findViewById(R.id.username_input);
		inputPasswordField = (EditText) v.findViewById(R.id.password_input);
		
		v.findViewById(R.id.ok_button).setOnClickListener(this);
		v.findViewById(R.id.cancel_button).setOnClickListener(this);
		
		return v;
	}
	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
			case R.id.cancel_button :
				dismiss();
				break;
			
			case R.id.ok_button :
				if (mode) {					
					QBUser tempUser = new QBUser();
					tempUser.setLogin(inputNameField.getText().toString());
					tempUser.setPassword(inputPasswordField.getText().toString());
					tempUser.setFullName(tempUser.getLogin());
					((SplashActivity)context).switchProgressDialog(true);
					
					QBUsers.signUpSignInTask(tempUser, (QBCallback) getActivity(), tempUser.getPassword());
					
					
				} else {
					QBUsers.signIn(inputNameField.getText().toString(), inputPasswordField.getText().toString(), (QBCallback) getActivity(), inputPasswordField.getText().toString());
					
					((SplashActivity)context).switchProgressDialog(true);
				}
				
				dismiss();
				break;
		}
	}
	
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}
	
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
	}
}
