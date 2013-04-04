package com.quickblox.chat_v2.activitys;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.R.layout;
import com.quickblox.chat_v2.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MainTabActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_tab);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_tab, menu);
		return true;
	}
	
}
