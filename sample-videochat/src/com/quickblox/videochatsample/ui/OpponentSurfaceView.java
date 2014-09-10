package com.quickblox.videochatsample.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.quickblox.videochatsample.model.utils.DrawThread;

/**
 * Created by igorkhomenko on 9/10/14.
 */
public class OpponentSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private DrawThread drawThread;

    public OpponentSurfaceView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public void render(byte[] videoData){
        drawThread.render(videoData);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        // close thread
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again
            }
        }
    }
}
