package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class FriendProfileActivity extends Activity {
	
	private ImageView userpic;
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		TextView username = (TextView) findViewById(R.id.chat_dialog_view_profile);
		
		
			}
}
