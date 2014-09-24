package com.quickblox.videochatsample.ui.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;

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

    public void clear(){
        Canvas canvas = null;
        try {
            canvas = getHolder().lockCanvas();
            canvas.drawColor(Color.BLACK);
        } catch (Exception e) {

        } finally {
            if (canvas != null) {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
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

    private class DrawThread extends Thread {
        private boolean runFlag = false;
        private SurfaceHolder surfaceHolder;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
        }

        private List<byte[]> dataList = new ArrayList<byte[]>();

        public void render(byte[] videoData) {
            dataList.clear();
            dataList.add(videoData);
        }

        public void setRunning(boolean run) {

            runFlag = run;
        }

        @Override
        public void run() {
            while (runFlag) {
                if (dataList.size() == 0) {
                    try {
                        sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                int latestIndex = dataList.size() - 1;

                Canvas canvas = null;
                try {
                    canvas = surfaceHolder.lockCanvas();

                    // run
                    byte[] videoData = dataList.get(latestIndex);
                    Bitmap bmp = BitmapFactory.decodeByteArray(videoData, 0, videoData.length);
                    canvas.drawBitmap(bmp, null, new Rect(0, 0, surfaceHolder.getSurfaceFrame().width(), surfaceHolder.getSurfaceFrame().height()), null);
                    bmp.recycle();

                } catch (Exception e) {

                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                dataList.remove(latestIndex);
            }
        }
    }
}
