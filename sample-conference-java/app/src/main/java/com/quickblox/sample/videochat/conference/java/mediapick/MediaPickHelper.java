package com.quickblox.sample.videochat.conference.java.mediapick;


import com.quickblox.sample.videochat.conference.java.fragments.MediaPickHelperFragment;
import com.quickblox.sample.videochat.conference.java.fragments.MediaSourcePickDialogFragment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class MediaPickHelper {

    public static void pickAnImage(FragmentActivity activity, int requestCode) {
        MediaPickHelperFragment mediaPickHelperFragment = MediaPickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), mediaPickHelperFragment);
    }

    private static void showImageSourcePickerDialog(FragmentManager fm, MediaPickHelperFragment fragment) {
        MediaSourcePickDialogFragment.show(fm,
                new MediaSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment));
    }
}