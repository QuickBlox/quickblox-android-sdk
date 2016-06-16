package com.quickblox.sample.groupchatwebrtc.utils;

import android.util.SparseArray;

import com.quickblox.sample.groupchatwebrtc.R;
import com.quickblox.videochat.webrtc.QBRTCTypes;

public class QBRTCSessionUtils {

    private static final SparseArray<Integer> peerStateDescriptions = new SparseArray<>();

    static {
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_PENDING.ordinal(), R.string.opponent_pending);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CONNECTING.ordinal(), R.string.text_status_connect);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CHECKING.ordinal(), R.string.text_status_checking);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CONNECTED.ordinal(), R.string.text_status_connected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_DISCONNECTED.ordinal(), R.string.text_status_disconnected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_CLOSED.ordinal(), R.string.opponent_closed);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_DISCONNECT_TIMEOUT.ordinal(), R.string.text_status_disconnected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_ANSWER.ordinal(), R.string.text_status_no_answer);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_NOT_OFFER.ordinal(), R.string.text_status_no_answer);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_REJECT.ordinal(), R.string.text_status_rejected);
        peerStateDescriptions.put(
                QBRTCTypes.QBRTCConnectionState.QB_RTC_CONNECTION_HANG_UP.ordinal(), R.string.text_status_hang_up);
    }

    public static Integer getStatusDescriptionResource(QBRTCTypes.QBRTCConnectionState connectionState) {
        return peerStateDescriptions.get(connectionState.ordinal());
    }
}