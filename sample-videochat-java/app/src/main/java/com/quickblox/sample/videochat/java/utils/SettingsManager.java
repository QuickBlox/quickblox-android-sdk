package com.quickblox.sample.videochat.java.utils;

import static com.quickblox.sample.videochat.java.utils.Consts.MAX_OPPONENTS_COUNT;

import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;

/**
 * QuickBlox team
 */
public class SettingsManager {
    public static void applySettings() {
        applyCallSettings();
        applyVideoSettings();
    }

    private static void applyCallSettings() {
        QBRTCMediaConfig.setAudioCodec(QBRTCMediaConfig.AudioCodec.ISAC);
        QBRTCMediaConfig.setUseBuildInAEC(true);
        QBRTCMediaConfig.setAudioProcessingEnabled(true);
        QBRTCMediaConfig.setUseOpenSLES(false);
    }

    private static void applyVideoSettings() {
        QBRTCMediaConfig.setVideoHWAcceleration(true);
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width);
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height);
        QBRTCMediaConfig.setVideoCodec(QBRTCMediaConfig.VideoCodec.VP8);
    }

    public static void applyRTCSettings() {
        QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT);
        QBRTCConfig.setDebugEnabled(true);

        int ANSWER_TIME_INTERVAL = 30;
        QBRTCConfig.setAnswerTimeInterval(ANSWER_TIME_INTERVAL);

        int DISCONNECT_TIME_10_SECONDS = 10;
        QBRTCConfig.setDisconnectTime(DISCONNECT_TIME_10_SECONDS);

        int DIALING_TIME_INTERVAL = 5;
        QBRTCConfig.setDialingTimeInterval(DIALING_TIME_INTERVAL);

        Log.e("ICE_SERVERS: => ", QBRTCConfig.getIceServerList().toString());
    }
}