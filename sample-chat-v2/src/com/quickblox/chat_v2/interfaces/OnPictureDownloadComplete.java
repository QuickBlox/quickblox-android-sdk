package com.quickblox.chat_v2.interfaces;

import java.io.File;

import android.graphics.Bitmap;

public interface OnPictureDownloadComplete {
	
	public void downloadComlete(Bitmap bitmap, File file);
}
