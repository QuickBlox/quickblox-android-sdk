package com.quickblox.sample.videochat.conference.java.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

public class PermissionsChecker {
    private final Context context;

    public PermissionsChecker(Context context) {
        this.context = context;
    }

    public boolean missAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (missPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    private boolean missPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED;
    }

}