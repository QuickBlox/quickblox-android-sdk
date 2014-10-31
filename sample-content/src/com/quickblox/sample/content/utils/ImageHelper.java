package com.quickblox.sample.content.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {

    public static final int GALLERY_KITKAT_INTENT_CALLED = 2;
    public static final int GALLERY_INTENT_CALLED = 1;
    private final int PREF_WIDTH_IMAGE = 500;
    private Activity activity;

    public ImageHelper(Activity activity) {
        this.activity = activity;
    }

    public void getImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(intent, GALLERY_INTENT_CALLED);
        } else {
            showKitKatGallery();
        }
    }

    private void showKitKatGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
    }

    public File getFileFromImageView(ImageView imageView) throws IOException {
        int preferredWidth = PREF_WIDTH_IMAGE;
        Bitmap origBitmap = drawableToBitmap(imageView.getDrawable());
        int origWidth = origBitmap.getWidth();
        int origHeight = origBitmap.getHeight();

        int destHeight, destWidth;

        if (origWidth <= preferredWidth || origHeight <= preferredWidth) {
            destWidth = origWidth;
            destHeight = origHeight;
        } else {
            destWidth = PREF_WIDTH_IMAGE;
            destHeight = origHeight / (origWidth / destWidth);
        }

        File tempFile = new File(activity.getCacheDir(), "temp.png");
        tempFile.createNewFile();

        Bitmap bitmap = resizeBitmap(origBitmap, destWidth, destHeight);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(bitmapData);
        fos.close();

        return tempFile;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap resizeBitmap(Bitmap inputBitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(inputBitmap, newWidth, newHeight, true);
    }
}