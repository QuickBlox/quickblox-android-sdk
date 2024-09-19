package com.quickblox.sample.videochat.conference.java.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class SystemPermissionsHelper {
    private static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010;
    public static final int REQUEST_CADE_FOR_NOTIFICATION = 1020;
    public static final int PERMISSIONS_FOR_MEDIA_REQUEST = 1030;

    private Activity activity;

    public SystemPermissionsHelper(Activity activity) {
        this.activity = activity;
    }

    public boolean isSaveImagePermissionGranted() {
        return isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
                && isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && isPermissionGranted(Manifest.permission.CAMERA);
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionsForSaveFileImage() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.CAMERA);
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, permissions);
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean isMediaPermissionsGranted() {
        return isPermissionGranted(Manifest.permission.READ_MEDIA_IMAGES)
                && isPermissionGranted(Manifest.permission.READ_MEDIA_VIDEO)
                && isPermissionGranted(Manifest.permission.CAMERA);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void requestPermissionsForMedia() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        permissions.add(Manifest.permission.CAMERA);
        checkAndRequestPermissions(PERMISSIONS_FOR_MEDIA_REQUEST, permissions);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public boolean isNotificationPermissionGranted() {
        return isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public void requestPermissionsForNotification() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        checkAndRequestPermissions(REQUEST_CADE_FOR_NOTIFICATION, permissions);
    }

    private void checkAndRequestPermissions(int requestCode, ArrayList<String> permissions) {
        if (collectDeniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDeniedPermissions(permissions));
        }
    }

    private String[] collectDeniedPermissions(ArrayList<String> permissions) {
        ArrayList<String> deniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission);
            }
        }

        return deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]);
    }

    private void requestPermissions(int requestCode, String... permissions) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }
}