package com.quickblox.chat_v2.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.activitys.SplashActivity;
import com.quickblox.chat_v2.others.ChatApplication;

/**
 * Created with IntelliJ IDEA. User: nickolas Date: 05.04.13 Time: 9:58
 */
public class ContactsFragment extends Fragment {
	
	private ChatApplication app;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_profile, null);
		app = ChatApplication.getInstance();
		
		ImageView profilePic = (ImageView) v.findViewById(R.id.profile_user_pic);
		TextView profileUserName = (TextView) v.findViewById(R.id.profile_username);
		Button profileExitButton = (Button) v.findViewById(R.id.profile_exit_button);
		
		profileUserName.setText(app.getAuthUser().getFullName());
		
		OnClickListener oclBtn = new OnClickListener() {
			
		
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.profile_exit_button :
						Session session = Session.getActiveSession();
						session.closeAndClearTokenInformation();
						
						
						Intent intent = new Intent(getActivity(), SplashActivity.class);
						startActivity(intent);
						
						getActivity().finish();
						break;
				
				}
			}
		};
		
		profileExitButton.setOnClickListener(oclBtn);
		
		return v;
	}
}
