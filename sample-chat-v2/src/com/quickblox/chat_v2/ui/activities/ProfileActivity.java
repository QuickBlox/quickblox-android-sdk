package com.quickblox.chat_v2.ui.activities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.Session;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.users.result.QBUserResult;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class ProfileActivity extends Activity {
	
	private ImageLoader imageLoader;
	private ImageView userpic;
	private ChatApplication app;
	
	private static final int SELECT_PHOTO = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		TextView username = (TextView) findViewById(R.id.chat_dialog_view_profile);
		
		app = ChatApplication.getInstance();
		
		if (app.getFbUser() != null) {
			downloadPicFromFB(app.getFbUser().getWebsite());
			username.setText(app.getFbUser().getFullName());
		} else {
			
			if (app.getQbUser() != null) {
				QBContent.downloadFileTask(app.getQbUser().getFileId(), new QBCallbackImpl() {
					
					@Override
					public void onComplete(Result result) {
						QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
						if (result.isSuccess()) {
							
							System.out.println("Вход в загрузку");
							
							InputStream is = qbFileDownloadResult.getContentStream();
							Bitmap b = BitmapFactory.decodeStream(is);
							userpic.setImageBitmap(b);
							ChatApplication.getInstance().setMyPic(b);
							setOnProfilePictureClicListener();
						}
						
					}
				});
			}
		}
		
		username.setText(app.getFbUser() !=null ? app.getFbUser().getFullName() : app.getQbUser() != null ? app.getQbUser().getFullName() : app.getQbUser().getLogin());
		
		Button exitButton = (Button) findViewById(R.id.exit_profile_button);
		exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				SharedPreferencesHelper.setLogin(ProfileActivity.this, "");
				SharedPreferencesHelper.setPassword(ProfileActivity.this, "");
				
				Session session = new Session(ProfileActivity.this);
				session.closeAndClearTokenInformation();
				
				Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
				startActivity(intent);
				
				getParent().finish();
			}
		});
	}
	
	private void downloadPicFromFB(String url) {
		
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		
		ImageLoader.getInstance().init(configuration);
		imageLoader = ImageLoader.getInstance();
		
		imageLoader.displayImage(url, userpic);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		System.out.println("Input parser");
		
		switch (requestCode) {
			case SELECT_PHOTO :
				if (resultCode == RESULT_OK) {
					
					try {
						
						Bitmap yourSelectedImage = decodeUri(imageReturnedIntent.getData());
						userpic.setImageBitmap(yourSelectedImage);
						
						ChatApplication.getInstance().setMyPic(yourSelectedImage);
						convertBitmapToFile(ChatApplication.getInstance().getMyPic());
						
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
	
	protected Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);
		
		// The new size we want to scale to
		final int REQUIRED_SIZE = 140;
		
		// Find the correct scale value. It should be the power of 2.
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		
		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
		
	}
	
	private void convertBitmapToFile(Bitmap mypic) {
		
		File f = new File(getCacheDir(), "new_avtar.png");
		
		Bitmap bitmap = mypic;
		
		try {
			FileOutputStream fos = new FileOutputStream(f);
			bitmap.compress(CompressFormat.PNG, 0, fos);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		uploadPic(f);
	}
	
	private void uploadPic(File file) {
		
		QBRequestCanceler requestCanceler = QBContent.uploadFileTask(file, true, new QBCallbackImpl() {
			@Override
			public void onComplete(Result result) {
				
				if (result.isSuccess()) {
					
					QBFileUploadTaskResult fileUploadTaskResultResult = (QBFileUploadTaskResult) result;
					if (app.getFbUser() != null) {
						app.getFbUser().setFileId(fileUploadTaskResultResult.getFile().getId());
						updateQBUser(app.getFbUser());
					} else {
						
						app.getQbUser().setFileId(fileUploadTaskResultResult.getFile().getId());
						updateQBUser(app.getQbUser());
					}
				}
			}
		});
	}
	
	private void updateQBUser(QBUser upadtedUser) {
		
		QBUsers.updateUser(upadtedUser, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				QBUserResult res = (QBUserResult) result;
				
				System.out.println("user file " + res.getUser().getFileId());
			}
			
		});
	}
}
