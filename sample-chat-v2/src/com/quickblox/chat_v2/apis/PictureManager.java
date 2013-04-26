package com.quickblox.chat_v2.apis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class PictureManager {
	
	private Context context;
	private ImageLoader imageLoader;
	private String targetUrl;
	private ImageView targetView;
	
	public PictureManager(Context context) {
		this.context = context;
	}
	
	public Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		
		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);
		
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
		return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
		
	}
	
	public File convertBitmapToFile(Bitmap mypic) {
		
		File f = new File(context.getCacheDir(), "new_avtar.png");
		
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
		
		return f;
	}
	
	public void downloadPicFromFB(String url, ImageView targetImageView) {
		targetUrl = url;
		targetView = targetImageView;
		
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(2 * 1024 * 1024).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();
		
		ImageLoader.getInstance().init(configuration);
		imageLoader = ImageLoader.getInstance();
		
		((Activity) context).runOnUiThread(new Runnable() {	
			@Override
			public void run() {
				imageLoader.displayImage(targetUrl, targetView);	
			}
		});
	}
}
