package com.quickblox.sample.chat.java.utils.imagepick;

import com.quickblox.sample.chat.java.utils.imagepick.fragment.MediaPickHelperFragment;
import com.quickblox.sample.chat.java.utils.imagepick.fragment.MediaSourcePickDialogFragment;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ImagePickHelper {

    public void pickAnImage(FragmentActivity activity, int requestCode) {
        MediaPickHelperFragment mediaPickHelperFragment = MediaPickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), mediaPickHelperFragment);
    }

    private void showImageSourcePickerDialog(FragmentManager fm, MediaPickHelperFragment fragment) {
        MediaSourcePickDialogFragment.show(fm,
                new MediaSourcePickDialogFragment.ImageSourcePickedListener(fragment));
    }
}