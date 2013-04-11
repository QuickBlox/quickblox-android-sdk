package com.quickblox.chat_v2.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.danikula.aibolit.Aibolit;
import com.danikula.aibolit.annotation.InjectView;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.others.ChatApplication;
import com.quickblox.chat_v2.utils.SharedPreferencesHelper;
import com.quickblox.chat_v2.widget.TopBar;
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
public class ProfileFragment extends Fragment {
	
	private ImageLoader imageLoader;
	private ImageView userpic;
	private ChatApplication app;
	private static final int SELECT_PHOTO = 1;
	
	private static final String FRAGMENT_NAME = "Profile";
	@InjectView(R.id.top_bar)
	TopBar topBar;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
		View view = inflater.inflate(R.layout.fragment_profile, null);
		Aibolit.doInjections(this, view);
		topBar.setFragmentName(FRAGMENT_NAME);
		
		app = ChatApplication.getInstance();
		
		userpic = (ImageView) view.findViewById(R.id.profile_userpic);
		TextView username = (TextView) view.findViewById(R.id.profile_username);
		
		switch (SharedPreferencesHelper.getUserPicID()) {
			case 0 :
				System.out.println("Секция 0");
				// В случае с FB всегда будет дефолтное из глобальных
				// переменных
				downloadPicFromFB();
				username.setText(SharedPreferencesHelper.getFBUsername());
				
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
				if (app.getMyPic() == null) {
					QBContent.downloadFileTask(SharedPreferencesHelper.getUserPicID(), new QBCallbackImpl() {
						
						@Override
						public void onComplete(Result result) {
							QBFileDownloadResult qbFileDownloadResult = (QBFileDownloadResult) result;
							if (result.isSuccess()) {
								
								System.out.println("Вход в загрузку");
								
								InputStream is = qbFileDownloadResult.getContentStream();
								Bitmap b = BitmapFactory.decodeStream(is);
								userpic.setImageBitmap(b);
								app.setMyPic(b);
							}
							
						}
					});
				} else {
					userpic.setImageBitmap(app.getMyPic());
				}
				setOnProfilePictureClicListener();
				break;
		}
		
		username.setText(SharedPreferencesHelper.getLogin());
		return view;
	}
	private void downloadPicFromFB() {
		
		// Я не против его убрать на вариант с прямой закачой, но пока пусть
		// будет так.
		
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(getActivity()).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		
		ImageLoader.getInstance().init(configuration);
		imageLoader = ImageLoader.getInstance();
		
		imageLoader.displayImage(SharedPreferencesHelper.getUserPicURL(), userpic);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
		System.out.println("Input parser");
		
		switch (requestCode) {
			case SELECT_PHOTO :
				if (resultCode == getActivity().RESULT_OK) {
					
					try {
						
						Bitmap yourSelectedImage = decodeUri(imageReturnedIntent.getData());
						userpic.setImageBitmap(yourSelectedImage);
						
						app.setMyPic(yourSelectedImage);
						convertBitmapToFile(app.getMyPic());
						
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
		BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o);
		
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
		return BitmapFactory.decodeStream(getActivity().getContentResolver().openInputStream(selectedImage), null, o2);
		
	}
	
	private void convertBitmapToFile(Bitmap mypic) {
		
		File f = new File(getActivity().getCacheDir(), "new_avtar.png");
		
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
					System.out.println("test = "+fileUploadTaskResultResult.getFile().getId());
					SharedPreferencesHelper.setUserPicID(fileUploadTaskResultResult.getFile().getId());
					updateQBUser(SharedPreferencesHelper.getUserPicID());
				}
			}
		});
	}
	
	private void updateQBUser(int pictureId){
		
		QBUser qbu = new QBUser();
		qbu.setFileId(pictureId);
		QBUsers.updateUser(qbu, new QBCallbackImpl(){

			@Override
			public void onComplete(Result result) {
				// TODO Auto-generated method stub
				super.onComplete(result);
				
				System.out.println("test");
			}
			
			
		});
	}
}
