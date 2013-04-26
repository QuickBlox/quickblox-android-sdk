package com.quickblox.chat_v2.interfaces;

import java.io.File;

import android.graphics.Bitmap;

public interface OnPictureConvertComplete {
	
	public void downloadComlete(Bitmap bitmap, File file);
}
