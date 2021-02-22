package com.quickblox.sample.chat.java.utils;

import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.core.io.IOUtils;
import com.quickblox.sample.chat.java.App;
import com.quickblox.sample.chat.java.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";
    private static final String FILES_MIME = "*/*";
    private static final String VIDEO_OR_IMAGE_MIME = "image/* video/*";
    private static final String IMAGE_MIME = "image/*";

    private static final String EXTERNAL_STORAGE_URI = "com.android.externalstorage.documents";
    private static final String DOWNLOADS_URI = "com.android.providers.downloads.documents";
    private static final String MEDIA_URI = "com.android.providers.media.documents";
    private static final String GOOGLE_PHOTOS_URI = "com.google.android.apps.photos.content";
    private static final String GOOGLE_DOCS_URI = "com.google.android.apps.docs.storage";

    public static final int GALLERY_REQUEST_CODE = 183;
    public static final int CAMERA_REQUEST_CODE = 212;
    public static final int FILE_REQUEST_CODE = 189;

    public static final int PREFERRED_IMAGE_SIZE_FULL = ResourceUtils.dpToPx(320);

    private static final boolean isKitkatSupportDevice = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    private ImageUtils() {
    }

    private static boolean isExtStorageDocument(Uri uri) {
        return EXTERNAL_STORAGE_URI.equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return DOWNLOADS_URI.equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return MEDIA_URI.equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri) {
        return GOOGLE_PHOTOS_URI.equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveUri(Uri uri) {
        if (uri.getAuthority() != null) {
            return uri.getAuthority().contains(GOOGLE_DOCS_URI);
        } else {
            return false;
        }
    }

    public static String getFilePath(Context context, Uri uri) {
        return isKitkatSupportDevice ? getFilePathFromUriForNewAPI(context, uri) : getFilePathFromUriForOldAPI(context, uri);
    }

    private static String getFilePathFromUriForOldAPI(Context context, Uri uri) {
        String[] projection = new String[]{MediaStore.Images.Media.DATA};
        String result = "";
        CursorLoader cursorLoader = new CursorLoader(context, uri, projection, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(columnIndex);
            cursor.close();
        }
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String getFilePathFromUriForNewAPI(Context context, Uri uri) {
        if (isKitkatSupportDevice && DocumentsContract.isDocumentUri(context, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            String[] split = docId.split(":");
            String type = split[0];

            if (isExtStorageDocument(uri)) {
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                Cursor cursor = null;
                try {
                    cursor = context.getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                    String path = "";
                    if (cursor != null) {
                        cursor.moveToNext();
                        String fileName = cursor.getString(0);
                        path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName;
                    }
                    if (!TextUtils.isEmpty(path)) {
                        return path;
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
                String id = DocumentsContract.getDocumentId(uri);
                if (id.startsWith("raw:")) {
                    return id.replaceFirst("raw:", "");
                }
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(uri)) {
                Uri contentUri = null;
                switch (type) {
                    case "audio":
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "video":
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        break;
                    case "image":
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        break;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (isGoogleDriveUri(uri)) {
            File file = loadFileFromGoogleDocs(uri, context);
            return file.getPath();
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            } else {
                return getDataColumn(context, uri,
                        null, null);
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        if (isKitkatSupportDevice && DocumentsContract.isDocumentUri(context, uri) && isGoogleDriveUri(uri)) {
            File driveFile = loadFileFromGoogleDocs(uri, context);
            return driveFile.getPath();
        } else {
            return null;
        }
    }

    private static File loadFileFromGoogleDocs(Uri uri, Context context) {
        File driveFile = null;
        Cursor cursor = App.getInstance().getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();

            String name = cursor.getString(nameIndex);
            driveFile = new File(context.getCacheDir(), name);

            try {
                InputStream input = context.getContentResolver().openInputStream(uri);
                FileOutputStream output = new FileOutputStream(driveFile);
                if (input != null) {
                    IOUtils.copy(input, output);
                    input.close();
                    output.close();
                }
            } catch (IOException e) {
                Log.d("ImageUtils", e.getMessage());
            } finally {
                cursor.close();
            }
        }
        return driveFile;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = new String[]{column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    public static void startFilePicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(FILES_MIME);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fragment.startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_REQUEST_CODE);
    }

    public static void startMediaPicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        intent.setType(VIDEO_OR_IMAGE_MIME);

        // TODO Files: Delete further single string to add sending all file types
        intent.setType(IMAGE_MIME);
        //

        intent.setAction(Intent.ACTION_GET_CONTENT);

        fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_file_from)), GALLERY_REQUEST_CODE);
    }

    public static void startCameraForResult(Fragment fragment) {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // TODO Files: Uncomment to add sending all file types
        //Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        Intent chooser = Intent.createChooser(pictureIntent, "Capture with");

        // TODO Files: Uncomment to add sending all file types
        //chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(videoIntent));

        ComponentName component = chooser.resolveActivity(App.getInstance().getPackageManager());
        if (component != null) {
            File mediaFile = getTemporaryCameraFile(fragment.getContext());
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getValidUri(mediaFile, fragment.getContext()));
            fragment.startActivityForResult(chooser, CAMERA_REQUEST_CODE);
        }
    }

    public static File getTemporaryCameraFile(Context context) {
        String imageFileName = getTemporaryCameraFileName();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        boolean createdSuccessful = true;
        File image = null;

        try {
            image = File.createTempFile(imageFileName,".jpg", storageDir);
        } catch (IOException e) {
            Log.e(TAG, "Cannot create file: " + e.getMessage());
            e.printStackTrace();
            createdSuccessful = false;
        }

        if (createdSuccessful) {
            return image;
        } else {
            return null;
        }
    }

    public static File getLastUsedCameraFile(Context context) {
        File dataDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dataDir != null) {
            File[] files = dataDir.listFiles();
            List<File> filteredFiles = new ArrayList<>();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().startsWith(CAMERA_FILE_NAME_PREFIX)) {
                        filteredFiles.add(file);
                    }
                }
            }

            Collections.sort(filteredFiles);
            if (!filteredFiles.isEmpty()) {
                return filteredFiles.get(filteredFiles.size() - 1);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static Uri getValidUri(File file, Context context) {
        Uri outputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String authority = context.getPackageName() + ".provider";
            outputUri = FileProvider.getUriForFile(context, authority, file);
        } else {
            outputUri = Uri.fromFile(file);
        }
        return outputUri;
    }

    private static String getTemporaryCameraFileName() {
        return CAMERA_FILE_NAME_PREFIX + System.currentTimeMillis() + ".jpg";
    }
}