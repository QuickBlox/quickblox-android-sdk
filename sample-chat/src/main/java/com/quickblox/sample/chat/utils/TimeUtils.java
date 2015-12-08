package com.quickblox.sample.chat.utils;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtils {
    public final static long MILLISECS_IN_SECOND = 1000;
    public final static long SECONDS_IN_MINUTE = 60;
    public final static long MINUTES_IN_HOUR = 60;
    public final static long HOURS_IN_DAY = 24;

    public final static long ONE_MINUTE = MILLISECS_IN_SECOND * SECONDS_IN_MINUTE;
    public final static long ONE_HOUR = ONE_MINUTE * MINUTES_IN_HOUR;
    public final static long ONE_DAY = ONE_HOUR * HOURS_IN_DAY;

    private TimeUtils() {
    }

    public static String getTime(long milliseconds) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        return dateFormat.format(new Date(milliseconds));
    }

    /**
     * converts time (in milliseconds) to human-readable format
     * "<w> days, <x> hours, <y> minutes and (z) seconds"
     */
    public static String millisToLongDHMS(long milliseconds) {
        if (milliseconds > 0) {
            milliseconds = System.currentTimeMillis() - milliseconds;
        }
        if (milliseconds < 0) {
            milliseconds = 0;
        }

        StringBuilder sb = new StringBuilder();
        long tempTimestamp;
        if (milliseconds >= MILLISECS_IN_SECOND) {
            tempTimestamp = milliseconds / ONE_DAY;
            if (tempTimestamp > 0) {
                milliseconds -= tempTimestamp * ONE_DAY;
                sb.append(tempTimestamp)
                        .append(" day")
                        .append(tempTimestamp > 1 ? "s" : "")
                        .append(milliseconds >= ONE_MINUTE ? ", " : "");
            }

            tempTimestamp = milliseconds / ONE_HOUR;
            if (tempTimestamp > 0) {
                milliseconds -= tempTimestamp * ONE_HOUR;
                sb.append(tempTimestamp)
                        .append(" hour")
                        .append(tempTimestamp > 1 ? "s" : "")
                        .append(milliseconds >= ONE_MINUTE ? ", " : "");
            }

            tempTimestamp = milliseconds / ONE_MINUTE;
            if (tempTimestamp > 0) {
                milliseconds -= tempTimestamp * ONE_MINUTE;
                sb.append(tempTimestamp)
                        .append(" minute")
                        .append(tempTimestamp > 1 ? "s" : "");
            }

            if (!TextUtils.isEmpty(sb.toString()) && milliseconds >= MILLISECS_IN_SECOND) {
                sb.append(" and ");
            }

            tempTimestamp = milliseconds / MILLISECS_IN_SECOND;
            if (tempTimestamp > 0) {
                sb.append(tempTimestamp)
                        .append(" second")
                        .append(tempTimestamp > 1 ? "s" : "");
            }
            sb.append(" ago");
            return sb.toString();
        } else {
            return "0 second ago";
        }
    }
}
