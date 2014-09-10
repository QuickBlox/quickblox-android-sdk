package com.quickblox.videochatsample.model.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorkhomenko on 9/10/14.
 */
public class DrawThread extends Thread{
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
    public void run(){
        while (runFlag) {
            if(dataList.size() == 0){
                try {
                    sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            int latestIndex = dataList.size()-1;

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
