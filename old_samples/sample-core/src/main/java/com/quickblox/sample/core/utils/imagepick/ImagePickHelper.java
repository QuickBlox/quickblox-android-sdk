package com.quickblox.sample.core.utils.imagepick;

import com.quickblox.sample.core.utils.imagepick.fragment.ImagePickHelperFragment;
import com.quickblox.sample.core.utils.imagepick.fragment.ImageSourcePickDialogFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ImagePickHelper {

    public void pickAnImage(Fragment fragment, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(fragment, requestCode);
        showImageSourcePickerDialog(fragment.getChildFragmentManager(), imagePickHelperFragment);
    }

    public void pickAnImage(FragmentActivity activity, int requestCode) {
        ImagePickHelperFragment imagePickHelperFragment = ImagePickHelperFragment.start(activity, requestCode);
        showImageSourcePickerDialog(activity.getSupportFragmentManager(), imagePickHelperFragment);
    }

    private void showImageSourcePickerDialog(FragmentManager fm, ImagePickHelperFragment fragment) {
        ImageSourcePickDialogFragment.show(fm,
                new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(fragment));
    }
}