package com.quickblox.videochatsample.manager;

import android.media.MediaRecorder;
import android.os.Environment;

import java.io.IOException;

/**
 * Created by igorkhomenko on 9/30/14.
 */
public class VideoRecorder {

    private MediaRecorder recorder;
    private static String mFileName = null;

    VideoRecorder(){
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecord.3gp";

        recorder = new MediaRecorder();
        //
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        //
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(mFileName);
    }

    public void start(){
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start(); // Recording is now started
    }

    public void release(){
        recorder.stop();
        recorder.reset();   // You can reuse the object by going back to setAudioSource() step
        recorder.release(); // Now the object cannot be reused
        recorder = null;
    }
}
