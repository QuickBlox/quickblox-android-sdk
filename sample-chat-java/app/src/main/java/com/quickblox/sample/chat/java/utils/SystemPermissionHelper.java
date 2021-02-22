package com.quickblox.sample.chat.java.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class SystemPermissionHelper {
    public static final int PERMISSIONS_FOR_SAVE_FILE_IMAGE_REQUEST = 1010;

    private Activity activity;

    public SystemPermissionHelper(Activity activity) {
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