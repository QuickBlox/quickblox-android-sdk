package com.quickblox.sample.videochat.java.utils;

import android.hardware.Camera;
import android.util.Log;

/**
 * QuickBlox team
 */
public class CameraUtils {

    private static final String TAG = CameraUtils.class.getSimpleName();

    public static Camera.CameraInfo getCameraInfo(int deviceId) {

        Camera.CameraInfo info;

        try {
            info = new Camera.CameraInfo();
            Camera.getCameraInfo(deviceId, info);
        } catch (Exception var3) {
            info = null;
            Log.e(TAG, "getCameraInfo failed on device " + deviceId);
        }
        return info;
    }

    public static boolean isCameraFront(int deviceId) {
        Camera.CameraInfo cameraInfo = getCameraInfo(deviceId);

        return (cameraInfo != null && cameraInfo.facing == 1);
    }
}