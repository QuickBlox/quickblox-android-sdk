package com.quickblox.sample.videochat.conference.java.mediapick;

import java.io.File;

public interface OnMediaPickedListener {

    void onMediaPicked(int requestCode, File file);

    void onMediaPickError(int requestCode, Exception e);

    void onMediaPickClosed(int requestCode);
}