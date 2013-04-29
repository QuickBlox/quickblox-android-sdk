package com.quickblox.chat_v2.ui.activities;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnFriendProfileDownloaded;
import com.quickblox.chat_v2.interfaces.OnPictureDownloadComplete;
import com.quickblox.chat_v2.utils.GlobalConsts;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class FriendProfileActivity extends Activity implements OnFriendProfileDownloaded, OnPictureDownloadComplete {
	
	private ImageView userpic;
	private TextView username;
	private Bitmap userBitmap;
	
	private ChatApplication app;
	
	private Intent i;
	
	private QBUser friend;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		TableRow low = (TableRow) findViewById(R.id.low_table_row);
		low.setVisibility(View.GONE);
		
		app = ChatApplication.getInstance();
		app.getQbm().setPictureDownloadComplete(this);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		username = (TextView) findViewById(R.id.chat_dialog_view_profile);
		
		i = getIntent();
		getFullUserInfo();
	}
	
	private void getFullUserInfo() {
		int uid = i.getIntExtra(GlobalConsts.FRIEND_ID, 0);
		app.getQbm().setFriendProvileListener(this);
		if (uid != 0) {
			System.out.println("uid = "+uid);
			app.getQbm().getSingleUserInfo(uid);
		}
	}
	
	@Override
	public void downloadComlete(QBUser frien) {
		this.friend = frien;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("QBUSERlistener");
				
				if (friend != null) {
					app.getQbm().downloadQBFile(friend);
					
				}
				
				username.setText(friend.getFullName() != null ? friend.getFullName() : friend.getLogin());
				
			}
		});
		
	}
	
	@Override
	public void downloadComlete(Bitmap bitmap, File file) {
		userBitmap = bitmap;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				userpic.setImageBitmap(userBitmap);
			}
		});
	}
}
