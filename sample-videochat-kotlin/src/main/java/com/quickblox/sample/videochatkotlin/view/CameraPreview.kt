package com.quickblox.sample.videochatkotlin.view

import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView

import java.io.IOException
import android.support.v4.view.ViewCompat.getRotation



/**
 * Camera preview that displays a [Camera].
 *
 * Handles basic lifecycle methods to display and stop the preview.
 *
 *
 * Implementation is based directly on the documentation at
 * http://developer.android.com/guide/topics/media/camera.html
 */
class CameraPreview(val activity: Activity, val cameraId: Int) : SurfaceView(activity), SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder
    var mCamera: Camera? = null

    init {
//
//        // Do not initialise if no camera has been set
//        if (mCamera == null || mCameraInfo == null) {
//            return
//        }

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mCamera = Camera.open(cameraId)
        mHolder = holder
        mHolder.addCallback(this)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera!!.setPreviewDisplay(holder)
            mCamera!!.startPreview()
            Log.d(TAG, "Camera preview started.")
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
        stop()
    }

    fun stop() {
        if (null == mCamera) {
            return
        }
        mCamera!!.stopPreview()
        mCamera!!.release()
        mCamera = null
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.surface == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview surface does not exist")
            return
        }

        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
            Log.d(TAG, "Preview stopped.")
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }
        val displayRotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation()
        val orientation = calculatePreviewOrientation(cameraId, displayRotation)
        mCamera!!.setDisplayOrientation(orientation)

        try {
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera!!.startPreview()
            Log.d(TAG, "Camera preview started.")
        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }

    }

    companion object {

        private val TAG = "CameraPreview"

        /**
         * Calculate the correct orientation for a [Camera] preview that is displayed on screen.
         *
         * Implementation is based on the sample code provided in
         * [Camera.setDisplayOrientation].
         */
        fun calculatePreviewOrientation(cameraId:Int, rotation: Int): Int {
            val cameraInfo = Camera.CameraInfo()
            Camera.getCameraInfo(cameraId, cameraInfo)
            var degrees = 0

            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }

            var result: Int
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (cameraInfo.orientation + degrees) % 360
                result = (360 - result) % 360  // compensate the mirror
            } else {  // back-facing
                result = (cameraInfo.orientation - degrees + 360) % 360
            }

            return result
        }
    }
}