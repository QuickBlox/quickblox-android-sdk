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
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.core.DataHolder;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.core.QBCallbackImpl;
import com.quickblox.core.QBRequestCanceler;
import com.quickblox.core.result.Result;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.result.QBFileDownloadResult;
import com.quickblox.module.content.result.QBFileUploadTaskResult;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;

/**
 * Created with IntelliJ IDEA. User: Andrew Dmitrenko Date: 08.04.13 Time: 8:58
 */
public class ProfileActivity extends Activity {
	
	private ImageLoader imageLoader;
	private ImageView userpic;
	private static final int SELECT_PHOTO = 1;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		
		userpic = (ImageView) findViewById(R.id.profile_userpic);
		TextView username = (TextView) findViewById(R.id.profile_username);
		
		switch (SharedPreferencesHelper.getUserPicID(getBaseContext())) {
			case 0 :
				System.out.println("Секция 0");
				// В случае с FB всегда будет дефолтное из глобальных
				// переменных
				downloadPicFromFB();
				username.setText(SharedPreferencesHelper.getFBUsername(getBaseContext()));
				
				break;
			
			case 1 :
				System.out.println("Секция 1");
				// меняется с нуля, если в QBколлбэке это значение = null,
				// ставится дефолтное из дравблов.
				userpic.setImageDrawable(getResources().getDrawable(R.drawable.com_facebook_profile_default_icon));
				
				setOnProfilePictureClicListener();
				
				break;
			
			default :
				// Если есть у юзера хоть что-то оно грузится и
				// отображается.
				System.out.println("Секция 00");
				if (DataHolder.getInstance().getMyPic() == null) {
					QBContent.downloadFileTask(SharedPreferencesHelper.getUserPicID(getBaseContext()), new QBCallbackImpl() {
						
						@Override
						public void onComplete(Result result) {
							QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
							if (result.isSuccess()) {
								
								System.out.println("Вход в загрузку");
								
								InputStream is = qbFileDownloadResult.getContentStream();
								Bitmap b = BitmapFactory.decodeStream(is);
								userpic.setImageBitmap(b);
								DataHolder.getInstance().setMyPic(b);
							}
							
						}
					});
				} else {
					userpic.setImageBitmap(DataHolder.getInstance().getMyPic());
				}
				setOnProfilePictureClicListener();
				break;
		}
		
		String fbUserName = SharedPreferencesHelper.getQBUsername(this);
		String qbUserName = SharedPreferencesHelper.getQBUsername(this);
		
		username.setText(!TextUtils.isEmpty(fbUserName) ? fbUserName : !TextUtils.isEmpty(qbUserName) ? qbUserName : SharedPreferencesHelper.getLogin(this));
		
		
		Button exitButton = (Button) findViewById(R.id.exit_profile_button);
		exitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				SharedPreferencesHelper.setFbUsername(ProfileActivity.this, "");
				SharedPreferencesHelper.setQbUsername(ProfileActivity.this, "");
				SharedPreferencesHelper.setLogin(ProfileActivity.this, "");
				SharedPreferencesHelper.setPassword(ProfileActivity.this, "");
				SharedPreferencesHelper.setUserPicURL(ProfileActivity.this, "");
				SharedPreferencesHelper.setUserPicID(ProfileActivity.this, 0);
				
				Intent intent = new Intent(ProfileActivity.this, SplashActivity.class);
				startActivity(intent);
				
				getParent().finish();				
			}
		});
	}
	
	private void downloadPicFromFB() {
				
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(this).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		
		ImageLoader.getInstance().init(configuration);
		imageLoader = ImageLoader.getInstance();
		
		imageLoader.displayImage(SharedPreferencesHelper.getUserPicURL(getBaseContext()), userpic);
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
						
						DataHolder.getInstance().setMyPic(yourSelectedImage);
						convertBitmapToFile(DataHolder.getInstance().getMyPic());
						
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
					SharedPreferencesHelper.setUserPicID(getBaseContext(), fileUploadTaskResultResult.getFile().getId());
					
					updateQBUser(SharedPreferencesHelper.getUserPicID(getBaseContext()));
				}
			}
		});
	}
	
	private void updateQBUser(int pictureId) {
		
		QBUser qbu = new QBUser();
		qbu.setFileId(pictureId);
		QBUsers.updateUser(qbu, new QBCallbackImpl() {
			
			@Override
			public void onComplete(Result result) {
				super.onComplete(result);
			}
			
		});
	}
}
