package com.quickblox.sample.chat.java.utils.imagepick.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.quickblox.sample.chat.java.utils.ImageUtils;
import com.quickblox.sample.chat.java.utils.imagepick.GetFilepathFromUriTask;
import com.quickblox.sample.chat.java.utils.imagepick.OnImagePickedListener;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class MediaPickHelperFragment extends Fragment {

    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PARENT_FRAGMENT = "parentFragment";

    private static final String TAG = MediaPickHelperFragment.class.getSimpleName();

    private OnImagePickedListener listener;

    public static MediaPickHelperFragment start(Fragment fragment, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        args.putString(ARG_PARENT_FRAGMENT, fragment.getClass().getSimpleName());

        return start(fragment.getActivity().getSupportFragmentManager(), args);
    }

    public static MediaPickHelperFragment start(FragmentActivity activity, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);

        return start(activity.getSupportFragmentManager(), args);
    }

    private static MediaPickHelperFragment start(FragmentManager fm, Bundle args) {
        MediaPickHelperFragment fragment = (MediaPickHelperFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new MediaPickHelperFragment();
            fm.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static void stop(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    public MediaPickHelperFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                .findFragmentByTag(getArguments().getString(ARG_PARENT_FRAGMENT));
        if (fragment != null) {
            if (fragment instanceof OnImagePickedListener) {
                listener = (OnImagePickedListener) fragment;
            }
        } else {
            if (context instanceof OnImagePickedListener) {
                listener = (OnImagePickedListener) context;
            }
        }

        if (listener == null) {
            throw new IllegalStateException("Either activity or fragment should implement OnImagePickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isResultFromImagePick(requestCode, resultCode, data)) {
            if (requestCode == ImageUtils.CAMERA_REQUEST_CODE && (data == null || data.getData() == null)) {
                // Hacky way to get EXTRA_OUTPUT param to work.
                // When setting EXTRA_OUTPUT param in the camera intent there is a chance that data will return as null
                // So we just pass temporary camera file as a data, because RESULT_OK means that photo was written in the file.
                data = new Intent();
                data.setData(Uri.fromFile(ImageUtils.getLastUsedCameraFile(getContext())));
            } else {
                // delete unused file
                if (getContext() != null) {
                    File file = ImageUtils.getLastUsedCameraFile(getContext());
                    if (file != null) {
                        file.delete();
                    }
                }
            }
            new GetFilepathFromUriTask(getChildFragmentManager(), listener, getArguments().getInt(ARG_REQUEST_CODE)).execute(data);
        } else {
            stop(getChildFragmentManager());
            if (listener != null) {
                listener.onImagePickClosed(getArguments().getInt(ARG_REQUEST_CODE));
            }
        }
    }

    public void setListener(OnImagePickedListener listener) {
        this.listener = listener;
    }

    private boolean isResultFromImagePick(int requestCode, int resultCode, Intent data) {
        return resultCode == Activity.RESULT_OK
                && ((requestCode == ImageUtils.CAMERA_REQUEST_CODE)
                || (requestCode == ImageUtils.GALLERY_REQUEST_CODE && data != null));
    }
}