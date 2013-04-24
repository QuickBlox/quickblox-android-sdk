package com.quickblox.chat_v2.ui.activities;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.interfaces.OnPictureConvertComplete;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class ProfileActivity extends Activity implements OnPictureConvertComplete {
	
	private ImageView userpic;
	private ChatApplication app;
	
	private Bitmap userBitmap;
	
	private static final int SELECT_PHOTO = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		TextView username = (TextView) findViewById(R.id.chat_dialog_view_profile);
		
		app = ChatApplication.getInstance();
		
		if (app.getFbUser() != null) {
			app.getPicManager().downloadPicFromFB(app.getFbUser().getWebsite(), userpic);
			username.setText(app.getFbUser().getFullName());
		} else {
			
			if (app.getQbUser() != null) {
				app.getQbm().setPictureConvertListener(this);
				app.getQbm().getQbFileToBitmap(app.getQbUser().getFileId());
				setOnProfilePictureClicListener();	
			}
		}
		
		username.setText(app.getFbUser() != null ? app.getFbUser().getFullName() : app.getQbUser() != null ? app.getQbUser().getFullName() : app.getQbUser()
				.getLogin());
		
		Button exitButton = (Button) findViewById(R.id.exit_profile_button);
		exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				SharedPreferencesHelper.setLogin(ProfileActivity.this, null);
				SharedPreferencesHelper.setPassword(ProfileActivity.this, null);
				
				Session session = new Session(ProfileActivity.this);
				session.closeAndClearTokenInformation();
				
				Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
				startActivity(intent);
				
				getParent().finish();
			}
		});
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		
		switch (requestCode) {
			case SELECT_PHOTO :
				if (resultCode == RESULT_OK) {
					
					try {
						
						Bitmap yourSelectedImage = app.getPicManager().decodeUri(imageReturnedIntent.getData());
						userpic.setImageBitmap(yourSelectedImage);
						
						ChatApplication.getInstance().setMyPic(yourSelectedImage);
						app.getQbm().uploadPic(app.getPicManager().convertBitmapToFile(app.getMyPic()));
						
						
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
		}
	}
	
	private void setOnProfilePictureClicListener() {
		
		userpic.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, SELECT_PHOTO);
			}
		});
	}

	@Override
	public void downloadComlete(Bitmap bitmap) {
		userBitmap = bitmap;
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				userpic.setImageBitmap(userBitmap);
				ChatApplication.getInstance().setMyPic(userBitmap);
				
			}
		});
		
		
	}
	
}
