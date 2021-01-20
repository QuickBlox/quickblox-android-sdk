package com.quickblox.sample.videochat.conference.java.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.quickblox.sample.videochat.conference.java.R;
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;

public class SettingsUtils {
    private static final String TAG = SettingsUtils.class.getSimpleName();

    public static void setSettingsStrategy(SharedPreferences sharedPref, Context context) {
        setAudioPreferences(sharedPref, context);
        setVideoPreferences(sharedPref, context);
    }

    private static void setAudioPreferences(SharedPreferences sharedPref, Context context) {
        // Get Audio Codec.
        String audioCodecDescription = getPreferenceString(sharedPref, context, R.string.pref_audiocodec_key,
                R.string.pref_audiocodec_def);
        QBRTCMediaConfig.AudioCodec audioCodec = QBRTCMediaConfig.AudioCodec.OPUS.getDescription()
                .equals(audioCodecDescription) ?
                QBRTCMediaConfig.AudioCodec.OPUS : QBRTCMediaConfig.AudioCodec.ISAC;
        Log.e(TAG, "audioCodec =: " + audioCodec.getDescription());
        QBRTCMediaConfig.setAudioCodec(audioCodec);
        Log.v(TAG, "audioCodec = " + QBRTCMediaConfig.getAudioCodec());

        // Get Disable built-in AEC flag.
        boolean disableBuiltInAEC = getPreferenceBoolean(sharedPref, context,
                R.string.pref_disable_built_in_aec_key,
                R.string.pref_disable_built_in_aec_default);
        QBRTCMediaConfig.setUseBuildInAEC(!disableBuiltInAEC);
        Log.v(TAG, "setUseBuildInAEC = " + QBRTCMediaConfig.isUseBuildInAEC());

        // Get Disable Audio Processing flag.
        boolean noAudioProcessing = getPreferenceBoolean(sharedPref, context,
                R.string.pref_noaudioprocessing_key,
                R.string.pref_noaudioprocessing_default);
        QBRTCMediaConfig.setAudioProcessingEnabled(!noAudioProcessing);
        Log.v(TAG, "isAudioProcessingEnabled = " + QBRTCMediaConfig.isAudioProcessingEnabled());

        // Get OpenSL ES enabled flag.
        boolean useOpenSLES = getPreferenceBoolean(sharedPref, context,
                R.string.pref_opensles_key,
                R.string.pref_opensles_default);
        QBRTCMediaConfig.setUseOpenSLES(useOpenSLES);
        Log.v(TAG, "isUseOpenSLES = " + QBRTCMediaConfig.isUseOpenSLES());
    }

    private static void setVideoPreferences(SharedPreferences sharedPref, Context context) {
        // Get HW Codec.
        boolean hwCodec = sharedPref.getBoolean(context.getString(R.string.pref_hwcodec_key),
                Boolean.valueOf(context.getString(R.string.pref_hwcodec_default)));
        Log.v(TAG, "videoHWAcceleration : " + hwCodec);
        QBRTCMediaConfig.setVideoHWAcceleration(hwCodec);

        // Get Video Resolution.
        int resolutionItem = Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_resolution_key),
                "0"));
        Log.e(TAG, "resolutionItem =: " + resolutionItem);
        setVideoQuality(resolutionItem);
        Log.v(TAG, "resolution = " + QBRTCMediaConfig.getVideoHeight() + "x" + QBRTCMediaConfig.getVideoWidth());

        // Get Video start bitrate.
        int startBitrate = getPreferenceInt(sharedPref, context,
                R.string.pref_startbitratevalue_key,
                R.string.pref_startbitratevalue_default);
        Log.e(TAG, "videoStartBitrate =: " + startBitrate);
        QBRTCMediaConfig.setVideoStartBitrate(startBitrate);
        Log.v(TAG, "videoStartBitrate = " + QBRTCMediaConfig.getVideoStartBitrate());

        int videoCodecItem = Integer.parseInt(getPreferenceString(sharedPref, context, R.string.pref_videocodec_key, "0"));
        for (QBRTCMediaConfig.VideoCodec codec : QBRTCMediaConfig.VideoCodec.values()) {
            if (codec.ordinal() == videoCodecItem) {
                Log.e(TAG, "videoCodecItem =: " + codec.getDescription());
                QBRTCMediaConfig.setVideoCodec(codec);
                Log.v(TAG, "videoCodecItem = " + QBRTCMediaConfig.getVideoCodec());
                break;
            }
        }

        // Get FPS.
        int cameraFps = getPreferenceInt(sharedPref, context, R.string.pref_frame_rate_key, R.string.pref_frame_rate_default);
        Log.e(TAG, "cameraFps = " + cameraFps);
        QBRTCMediaConfig.setVideoFps(cameraFps);
        Log.v(TAG, "cameraFps = " + QBRTCMediaConfig.getVideoFps());
    }

    private static void setVideoQuality(int resolutionItem) {
        if (resolutionItem != -1) {
            setVideoFromLibraryPreferences(resolutionItem);
        } else {
            setDefaultVideoQuality();
        }
    }

    private static void setDefaultVideoQuality() {
        QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.width);
        QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.VGA_VIDEO.height);
    }

    private static void setVideoFromLibraryPreferences(int resolutionItem) {
        for (QBRTCMediaConfig.VideoQuality quality : QBRTCMediaConfig.VideoQuality.values()) {
            if (quality.ordinal() == resolutionItem) {
                Log.e(TAG, "resolution =: " + quality.height + ":" + quality.width);
                QBRTCMediaConfig.setVideoHeight(quality.height);
                QBRTCMediaConfig.setVideoWidth(quality.width);
            }
        }
    }

    private static String getPreferenceString(SharedPreferences sharedPref, Context context, int strResKey, int strResDefValue) {
        return sharedPref.getString(context.getString(strResKey), context.getString(strResDefValue));
    }

    private static String getPreferenceString(SharedPreferences sharedPref, Context context, int strResKey, String strResDefValue) {
        return sharedPref.getString(context.getString(strResKey), strResDefValue);
    }

    private static int getPreferenceInt(SharedPreferences sharedPref, Context context, int strResKey, int strResDefValue) {
        return sharedPref.getInt(context.getString(strResKey), Integer.valueOf(context.getString(strResDefValue)));
    }

    private static boolean getPreferenceBoolean(SharedPreferences sharedPref, Context context, int StrRes, int strResDefValue) {
        return sharedPref.getBoolean(context.getString(StrRes), Boolean.valueOf(context.getString(strResDefValue)));
    }
}
