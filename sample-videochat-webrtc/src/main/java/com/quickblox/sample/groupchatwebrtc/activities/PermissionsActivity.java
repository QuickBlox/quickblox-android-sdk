package com.quickblox.sample.groupchatwebrtc.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.quickblox.sample.core.utils.Toaster;
import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.sample.groupchatwebrtc.utils.PermissionsChecker;

public class PermissionsActivity extends AppCompatActivity {
    public static final int PERMISSIONS_GRANTED = 0;
    public static final int PERMISSIONS_DENIED = 1;
    public static final String PERMISSION_GRANT_RESULTS_KEY = "grantResults";

    private static final int PERMISSION_REQUEST_CODE = 0;
    private static final String EXTRA_PERMISSIONS = "extraPermissions";
    private static final String CHECK_ONLY_AUDIO = "checkAudio";

    enum permissionFeatures {
        CAMERA,
        MICROPHONE
    }

    private PermissionsChecker checker;
    private boolean requiresCheck;

    public static void startActivity(Activity activity, boolean onlyAudio, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        intent.putExtra(CHECK_ONLY_AUDIO, onlyAudio);
        ActivityCompat.startActivity(activity, intent, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        setContentView(R.layout.activity_permissions);

        checker = new PermissionsChecker(this);
        requiresCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (requiresCheck) {
            String[] permissions = getPermissions();
            boolean onlyAudio = getOnlyAudio();

            if (onlyAudio) {
                if (checker.lacksPermissions(permissions[1])) {
                    requestPermissions(permissions[1]);
                } else {
                    allPermissionsGranted();
                }
            } else {
                if (checker.lacksPermissions(permissions)) {
                    requestPermissions(permissions);
                } else {
                    allPermissionsGranted();
                }
            }
        } else {
            requiresCheck = true;
        }
    }

    private String[] getPermissions() {
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private boolean getOnlyAudio() {
        return getIntent().getBooleanExtra(CHECK_ONLY_AUDIO, false);
    }

    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    private void allPermissionsGranted() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            requiresCheck = true;
            allPermissionsGranted();
        } else {
            requiresCheck = false;
            showDeniedResponse(grantResults);
            finish();
        }
    }

    private void showDeniedResponse(int[] grantResults) {
        if (grantResults.length > 1) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != 0) {
                    Toaster.longToast(getString(R.string.permission_unavailable, permissionFeatures.values()[i]));
                }
            }
        } else {
            Toaster.longToast(getString(R.string.permission_unavailable, permissionFeatures.MICROPHONE));
        }
    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}