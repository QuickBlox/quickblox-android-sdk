package com.quickblox.sample.groupchatwebrtc.utils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FragmentLifeCycleHandler extends Handler {

    private static final String TAG = FragmentLifeCycleHandler.class.getSimpleName();
    private FragmentLifycleListener lifycleListener;

    public FragmentLifeCycleHandler(FragmentLifycleListener lifycleListener) {
        this.lifycleListener = lifycleListener;
    }

    @Override
    public void dispatchMessage(Message msg) {
        if (lifycleListener != null && lifycleListener.isFragmentAlive()) {
            super.dispatchMessage(msg);
        } else {
            Log.d(TAG, "Fragment under destroying");
        }
    }

    public void detach() {
        lifycleListener = null;
    }

    public interface FragmentLifycleListener {
        boolean isFragmentAlive();
    }
}
