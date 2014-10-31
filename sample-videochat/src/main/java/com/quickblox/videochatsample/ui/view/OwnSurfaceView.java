package com.quickblox.videochatsample.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by igorkhomenko on 9/11/14.
 */
public class OwnSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private volatile Camera camera;

    private ProcessDataThread processCameraDataThread;
    private ConcurrentLinkedQueue<Runnable> cameraPreviewCallbackQueue;
    private int currentCameraId;

    private final int IMAGE_QUALITY = 25;
    private int FPS = 4; // by default 4 fps
    private Camera.Size frameSize;

    private Matrix rotationMatrixFront;
    private Matrix rotationMatrixBack;

    private CameraDataListener cameraDataListener;

    private boolean isCreated = false;

    public OwnSurfaceView(Context ctx, AttributeSet attrSet) {
        super(ctx, attrSet);

        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        currentCameraId = (currentCameraId + 1) % Camera.getNumberOfCameras();

        rotationMatrixFront = new Matrix();
        rotationMatrixFront.postRotate(-90);
        rotationMatrixBack = new Matrix();
        rotationMatrixBack.postRotate(90);

        cameraPreviewCallbackQueue = new ConcurrentLinkedQueue<Runnable>();
    }

    public void setCameraDataListener(CameraDataListener cameraDataListener) {
        this.cameraDataListener = cameraDataListener;
    }

    public void setFPS(int FPS) {
        this.FPS = FPS;
    }

    public void setFrameSize(Camera.Size frameSize) {
        this.frameSize = frameSize;
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isCreated = false;

        Log.w("MySurfaceView", "surfaceDestroyed");

        boolean retry = true;
        // close thread
        processCameraDataThread.stopProcessing();
        while (retry) {
            try {
                processCameraDataThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.w("MySurfaceView", "surfaceChanged");
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isCreated = true;

        Log.w("MySurfaceView", "surfaceCreated");

        openCamera();

        processCameraDataThread = new ProcessDataThread();
        processCameraDataThread.start();
    }


    Camera.PreviewCallback cameraPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            Camera.Parameters params = camera.getParameters();
            processCameraData(data, params.getPreviewSize().width, params.getPreviewSize().height);
        }
    };


    public void openCamera() {
        if(!isCreated || camera != null){
            return;
        }

        // Open camera
        //
        try {
            camera = Camera.open(currentCameraId);
        } catch (NoSuchMethodError noSuchMethodError) {
            camera = Camera.open();
        }

        try {
            camera.setPreviewDisplay(getHolder());
            camera.setDisplayOrientation(90);
            camera.setPreviewCallback(cameraPreviewCallback);
        } catch (IOException ignore) {
            ignore.printStackTrace();
        } catch (NullPointerException ignore) {
            ignore.printStackTrace();
        }


        // get camera parameters
        //
        final Camera.Parameters parameters = camera.getParameters();

        // Get Preview Size and FPS
        //
        final List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        final List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();


        // set FPS
        //
        int[] chosenFPSRange = supportedPreviewFpsRange.get(0);
        for (int[] FPSRange : supportedPreviewFpsRange) {
            if (FPS > FPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] && FPS < FPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]) {
                chosenFPSRange = FPSRange;
                break;
            }
        }
        parameters.setPreviewFpsRange(chosenFPSRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                chosenFPSRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);


        // set Preview Size
        //
        if (frameSize != null) {
            parameters.setPreviewSize(frameSize.width, frameSize.height);
        } else {
            int firstElementWidth = supportedPreviewSizes.get(0).width;
            int lastElementWidth = supportedPreviewSizes.get(supportedPreviewSizes.size() - 1).width;
            Camera.Size minPreviewSize = (lastElementWidth > firstElementWidth) ? supportedPreviewSizes.get(0) : supportedPreviewSizes.get(supportedPreviewSizes.size() - 1);
            parameters.setPreviewSize(minPreviewSize.width, minPreviewSize.height);
        }



        // Set parameters and start preview
        //
        try {
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (RuntimeException ignore) {
            ignore.printStackTrace();
        }
    }

    public void switchCamera() {
        if (Camera.getNumberOfCameras() == 2) {
            currentCameraId = (currentCameraId + 1) % Camera.getNumberOfCameras();

            closeCamera();
            openCamera();
        }
    }

    public void closeCamera() {
        if (camera == null) {
            return;
        }
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    public void reuseCamera() {
        closeCamera();
        openCamera();
    }

    private void processCameraData(final byte[] cameraData, final int imageWidth, final int imageHeight) {

        cameraPreviewCallbackQueue.clear();
        boolean offerSuccess = cameraPreviewCallbackQueue.offer(new Runnable() {
            @Override
            public void run() {
                long start = System.nanoTime();

                // Convert data to JPEG and compress
                YuvImage image = new YuvImage(cameraData, ImageFormat.NV21, imageWidth, imageHeight, null);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Rect area = new Rect(0, 0, imageWidth, imageHeight);
                image.compressToJpeg(area, IMAGE_QUALITY, out);
                byte[] jpegVideoFrameData = out.toByteArray();

                // rotate image
                byte[] rotatedCameraData = rotateImage(jpegVideoFrameData, imageWidth, imageHeight, currentCameraId);
                if (rotatedCameraData.length == 0) {
                    return;
                }

                // send data to the opponent
                //
                cameraDataListener.onCameraDataReceive(rotatedCameraData);
                //
                //

                // close stream
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private byte[] rotateImage(byte[] cameraData, final int imageWidth, final int imageHeight, int currentCameraId) {
        Bitmap landscapeCameraDataBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);

        Bitmap portraitBitmap = null;
        if(currentCameraId == getCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT)) { // front camera
            portraitBitmap = Bitmap.createBitmap(landscapeCameraDataBitmap, 0, 0, imageWidth, imageHeight, rotationMatrixFront, true);
        }else{ // back camera
            portraitBitmap = Bitmap.createBitmap(landscapeCameraDataBitmap, 0, 0, imageWidth, imageHeight, rotationMatrixBack, true);
        }

        landscapeCameraDataBitmap.recycle();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if (!portraitBitmap.isRecycled()) {
            portraitBitmap.compress(Bitmap.CompressFormat.JPEG, IMAGE_QUALITY, stream);
            byte[] portraitCameraData = stream.toByteArray();
            portraitBitmap.recycle();
            return portraitCameraData;
        } else {
            return new byte[0];
        }
    }

    private int getCameraId(final int facing) {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int id = 0; id < numberOfCameras; id++) {
            Camera.getCameraInfo(id, info);
            if (info.facing == facing) {
                return id;
            }
        }
        return -1;
    }


    private class ProcessDataThread extends Thread {

        private boolean isRunning;

        public ProcessDataThread() {
            this.isRunning = true;
        }

        @Override
        public void run() {
            while (isRunning) {
                if (!cameraPreviewCallbackQueue.isEmpty()) {

                    Runnable runnable = cameraPreviewCallbackQueue.poll();
                    if (runnable != null) {
                        runnable.run();
                    }

                    try {
                        Thread.sleep(1000 / FPS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void stopProcessing() {
            this.isRunning = false;
        }
    }

    public interface CameraDataListener{
        public void onCameraDataReceive(byte[] data);
    }
}
