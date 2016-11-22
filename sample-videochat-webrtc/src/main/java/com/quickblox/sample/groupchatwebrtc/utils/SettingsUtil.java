package com.quickblox.sample.groupchatwebrtc.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCMediaConfig;

import java.util.List;

/**
 * QuickBlox team
 */
public class SettingsUtil {

    private static final String TAG = SettingsUtil.class.getSimpleName();

    private static void setSettingsForMultiCall(List<Integer> users) {
        if (users.size() <= 4) {
            setDefaultVideoQuality();
        } else {
            //set to minimum settings
            QBRTCMediaConfig.setVideoWidth(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.width);
            QBRTCMediaConfig.setVideoHeight(QBRTCMediaConfig.VideoQuality.QBGA_VIDEO.height);
            QBRTCMediaConfig.setVideoHWAcceleration(false);
            QBRTCMediaConfig.setVideoCodec(null);
        }
    }

    public static void setSettingsStrategy(List<Integer> users, SharedPreferences sharedPref, Context context) {
        setCommonSettings(sharedPref, context);
        if (users.size() == 1) {
            setSettingsFromPreferences(sharedPref, context);
        } else {
            setSettingsForMultiCall(users);
        }
    }

    private static void setCommonSettings(SharedPreferences sharedPref, Context context) {
        String audioCodecDescription = getPreferenceString(sharedPref, context, R.string.pref_audiocodec_key,
                R.string.pref_audiocodec_def);
        QBRTCMediaConfig.AudioCodec audioCodec = QBRTCMediaConfig.AudioCodec.ISAC.getDescription()
                .equals(audioCodecDescription) ?
                QBRTCMediaConfig.AudioCodec.ISAC : QBRTCMediaConfig.AudioCodec.OPUS;
        Log.e(TAG, "audioCodec =: " + audioCodec.getDescription());
        QBRTCMediaConfig.setAudioCodec(audioCodec);
        Log.v(TAG, "audioCodec = " + QBRTCMediaConfig.getAudioCodec());
        // Check Disable built-in AEC flag.
        boolean disableBuiltInAEC = getPreferenceBoolean(sharedPref, context,
                R.string.pref_disable_built_in_aec_key,
                R.string.pref_disable_built_in_aec_default);

        QBRTCMediaConfig.setUseBuildInAEC(!disableBuiltInAEC);
        Log.v(TAG, "setUseBuildInAEC = " + QBRTCMediaConfig.isUseBuildInAEC());
        // Check Disable Audio Processing flag.
        boolean noAudioProcessing = getPreferenceBoolean(sharedPref, context,
                R.string.pref_noaudioprocessing_key,
                R.string.pref_noaudioprocessing_default);
        QBRTCMediaConfig.setAudioProcessingEnabled(!noAudioProcessing);
        Log.v(TAG, "isAudioProcessingEnabled = " + QBRTCMediaConfig.isAudioProcessingEnabled());
        // Check OpenSL ES enabled flag.
        boolean useOpenSLES = getPreferenceBoolean(sharedPref, context,
                R.string.pref_opensles_key,
                R.string.pref_opensles_default);
        QBRTCMediaConfig.setUseOpenSLES(useOpenSLES);
        Log.v(TAG, "isUseOpenSLES = " + QBRTCMediaConfig.isUseOpenSLES());
    }

    private static void setSettingsFromPreferences(SharedPreferences sharedPref, Context context) {

        // Check HW codec flag.
        boolean hwCodec = sharedPref.getBoolean(context.getString(R.string.pref_hwcodec_key),
                Boolean.valueOf(context.getString(R.string.pref_hwcodec_default)));

        QBRTCMediaConfig.setVideoHWAcceleration(hwCodec);

        // Get video resolution from settings.
        int resolutionItem = Integer.parseInt(sharedPref.getString(context.getString(R.string.pref_resolution_key),
                "0"));
        Log.e(TAG, "resolutionItem =: " + resolutionItem);
        setVideoQuality(resolutionItem);
        Log.v(TAG, "resolution = " + QBRTCMediaConfig.getVideoHeight() + "x" + QBRTCMediaConfig.getVideoWidth());

        // Get start bitrate.
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
        // Get camera fps from settings.
        int cameraFps = getPreferenceInt(sharedPref, context, R.string.pref_frame_rate_key, R.string.pref_frame_rate_default);
        Log.e(TAG, "cameraFps = " + cameraFps);
        QBRTCMediaConfig.setVideoFps(cameraFps);
        Log.v(TAG, "cameraFps = " + QBRTCMediaConfig.getVideoFps());
    }

    public static void configRTCTimers(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        long answerTimeInterval = getPreferenceInt(sharedPref, context,
                R.string.pref_answer_time_interval_key,
                R.string.pref_answer_time_interval_default_value);
        QBRTCConfig.setAnswerTimeInterval(answerTimeInterval);
        Log.e(TAG, "answerTimeInterval = " + answerTimeInterval);

        int disconnectTimeInterval = getPreferenceInt(sharedPref, context,
                R.string.pref_disconnect_time_interval_key,
                R.string.pref_disconnect_time_interval_default_value);
        QBRTCConfig.setDisconnectTime(disconnectTimeInterval);
        Log.e(TAG, "disconnectTimeInterval = " + disconnectTimeInterval);

        long dialingTimeInterval = getPreferenceInt(sharedPref, context,
                R.string.pref_dialing_time_interval_key,
                R.string.pref_dialing_time_interval_default_value);
        QBRTCConfig.setDialingTimeInterval(dialingTimeInterval);
        Log.e(TAG, "dialingTimeInterval = " + dialingTimeInterval);
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

    public static int getPreferenceInt(SharedPreferences sharedPref, Context context, int strResKey, int strResDefValue) {
        return sharedPref.getInt(context.getString(strResKey), Integer.valueOf(context.getString(strResDefValue)));
    }

    private static boolean getPreferenceBoolean(SharedPreferences sharedPref, Context context, int StrRes, int strResDefValue) {
        return sharedPref.getBoolean(context.getString(StrRes), Boolean.valueOf(context.getString(strResDefValue)));
    }
}
