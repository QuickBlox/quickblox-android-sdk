package com.quickblox.sample.chat.utils;

import java.util.Date;

public class TimeUtils {
    public final static long ONE_SECOND = 1000;
    public final static long SECONDS_IN_MINUTE = 60;

    public final static long ONE_MINUTE = ONE_SECOND * SECONDS_IN_MINUTE;
    public final static long MINUTES_IN_HOUR = 60;

    public final static long ONE_HOUR = ONE_MINUTE * MINUTES_IN_HOUR;
    public final static long HOURS_IN_DAY = 24;

    public final static long ONE_DAY = ONE_HOUR * HOURS_IN_DAY;

    private TimeUtils() {
    }

    /**
     * converts time (in milliseconds) to human-readable format
     * "<w> days, <x> hours, <y> minutes and (z) seconds"
     */
    public static String millisToLongDHMS(long duration) {
        if (duration > 0) {
            duration = new Date().getTime() - duration;
        }
        if (duration < 0) {
            duration = 0;
        }

        StringBuilder sb = new StringBuilder();
        long temp;
        if (duration >= ONE_SECOND) {
            temp = duration / ONE_DAY;
            if (temp > 0) {
                duration -= temp * ONE_DAY;
                sb.append(temp).append(" day").append(temp > 1 ? "s" : "")
                        .append(duration >= ONE_MINUTE ? ", " : "");
            }

            temp = duration / ONE_HOUR;
            if (temp > 0) {
                duration -= temp * ONE_HOUR;
                sb.append(temp).append(" hour").append(temp > 1 ? "s" : "")
                        .append(duration >= ONE_MINUTE ? ", " : "");
            }

            temp = duration / ONE_MINUTE;
            if (temp > 0) {
                duration -= temp * ONE_MINUTE;
                sb.append(temp).append(" minute").append(temp > 1 ? "s" : "");
            }

            if (!sb.toString().equals("") && duration >= ONE_SECOND) {
                sb.append(" and ");
            }

            temp = duration / ONE_SECOND;
            if (temp > 0) {
                sb.append(temp).append(" second").append(temp > 1 ? "s" : "");
            }
            sb.append(" ago");
            return sb.toString();
        } else {
            return "0 second ago";
        }
    }
}
