package com.quickblox.chat_v2.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnFriendProfileDownloaded;
import com.quickblox.chat_v2.interfaces.OnPictureConvertComplete;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class FriendProfileActivity extends Activity implements OnFriendProfileDownloaded, OnPictureConvertComplete {
	
	private ImageView userpic;
	private TextView username;
	private Bitmap userBitmap;
	
	private ChatApplication app;
	
	private Intent i;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		app = ChatApplication.getInstance();
		app.getQbm().setPictureConvertListener(this);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		username = (TextView) findViewById(R.id.chat_dialog_view_profile);
		
		i = getIntent();
		getFullUserInfo();
	}
	
	private void getFullUserInfo() {
		int uid = i.getIntExtra(GlobalConsts.FRIEND_ID, 0);
		app.getQbm().setFriendProvileListener(this);
		app.getQbm().getSingleUserInfo(uid);
		
	}

	@Override
	public void downloadComlete(QBUser friend) {
		
		if (friend != null && friend.getFileId() != null) {
			app.getQbm().getQbFileToBitmap(friend.getFileId());
			
		}
		
		username.setText(friend.getFullName() != null ? friend.getFullName() : friend.getLogin());
		
	}

	@Override
	public void downloadComlete(Bitmap bitmap) {
		userBitmap = bitmap;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				userpic.setImageBitmap(userBitmap);					
			}
		});
	}
}
