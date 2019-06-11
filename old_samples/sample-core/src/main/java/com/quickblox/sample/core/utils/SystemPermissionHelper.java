package com.quickblox.sample.core.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class SystemPermissionHelper {
    public static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1;
    public static final int PERMISSIONS_FOR_TAKE_PHOTO_REQUEST = 2;
    public static final int PERMISSIONS_FOR_CALL_REQUEST = 3;

    private Activity activity;
    private Fragment fragment;

    public SystemPermissionHelper(Activity activity) {
        this.activity = activity;
    }

    public SystemPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    public boolean isSaveImagePermissionGranted() {
        return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public boolean isCameraPermissionGranted() {
        return isPermissionGranted(Manifest.permission.CAMERA);
    }

    public boolean isCallPermissionsGranted() {
        return isPermissionGranted(Manifest.permission.RECORD_AUDIO) && isPermissionGranted(Manifest.permission.CAMERA);
    }

    private boolean isPermissionGranted(String permission) {
        if (fragment != null) {
            return ContextCompat.checkSelfPermission(fragment.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void requestPermissionsForCallByType() {
        checkAndRequestPermissions(PERMISSIONS_FOR_CALL_REQUEST, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
    }

    public void requestPermissionsForSaveFileImage() {
        checkAndRequestPermissions(PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public void requestPermissionsTakePhoto() {
        checkAndRequestPermissions(PERMISSIONS_FOR_TAKE_PHOTO_REQUEST, Manifest.permission.CAMERA);
    }

    private void checkAndRequestPermissions(int requestCode, String... permissions) {
        if (collectDeniedPermissions(permissions).length > 0) {
            requestPermissions(requestCode, collectDeniedPermissions(permissions));
        }
    }

    private String[] collectDeniedPermissions(String... permissions) {
        ArrayList<String> deniedPermissionsList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                deniedPermissionsList.add(permission);
            }
        }

        return deniedPermissionsList.toArray(new String[deniedPermissionsList.size()]);
    }

    private void requestPermissions(int requestCode, String... permissions) {
        if (fragment != null) {
            fragment.requestPermissions(permissions, requestCode);
        } else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }
}