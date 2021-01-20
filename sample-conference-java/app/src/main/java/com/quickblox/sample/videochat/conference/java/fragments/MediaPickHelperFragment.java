package com.quickblox.sample.videochat.conference.java.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.quickblox.sample.videochat.conference.java.mediapick.GetFilePathFromUriTask;
import com.quickblox.sample.videochat.conference.java.mediapick.OnMediaPickedListener;
import com.quickblox.sample.videochat.conference.java.utils.MediaUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class MediaPickHelperFragment extends Fragment {

    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PARENT_FRAGMENT = "parentFragment";

    // Constants are needed in case files-send logic
    private static final String SCHEME_CONTENT = "content";
    private static final String SCHEME_CONTENT_GOOGLE = "content://com.google.android";
    private static final String SCHEME_FILE = "file";

    private static final String TAG = MediaPickHelperFragment.class.getSimpleName();

    private OnMediaPickedListener listener;

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

    private static void stop(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = null;
        if (getArguments() != null) {
            fragment = ((AppCompatActivity) context).getSupportFragmentManager()
                    .findFragmentByTag(getArguments().getString(ARG_PARENT_FRAGMENT));
        }
        if (fragment != null) {
            if (fragment instanceof OnMediaPickedListener) {
                listener = (OnMediaPickedListener) fragment;
            }
        } else {
            if (context instanceof OnMediaPickedListener) {
                listener = (OnMediaPickedListener) context;
            }
        }

        if (listener == null) {
            throw new IllegalStateException("Either activity or fragment should implement OnMediaPickedListener");
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
            if (requestCode == MediaUtils.CAMERA_REQUEST_CODE && (data == null || data.getData() == null)) {
                // Hacky way to get EXTRA_OUTPUT param to work.
                // When setting EXTRA_OUTPUT param in the camera intent there is a chance that data will return as null
                // So we just pass temporary camera file as a data, because RESULT_OK means that photo was written in the file.
                data = new Intent();
                data.setData(Uri.fromFile(MediaUtils.getLastUsedCameraFile(getContext())));
            }

            if (getActivity().getApplicationContext() != null && getArguments() != null) {
                Context context = getActivity().getApplicationContext();
                new GetFilePathFromUriTask(context, getChildFragmentManager(), listener, getArguments().getInt(ARG_REQUEST_CODE)).execute(data);
            }
        } else {
            stop(getChildFragmentManager());
            if (listener != null && getArguments() != null) {
                listener.onMediaPickClosed(getArguments().getInt(ARG_REQUEST_CODE));
            }
        }
    }

    private boolean isResultFromImagePick(int requestCode, int resultCode, Intent data) {
        return resultCode == Activity.RESULT_OK
                && ((requestCode == MediaUtils.CAMERA_REQUEST_CODE)
                || (requestCode == MediaUtils.GALLERY_REQUEST_CODE && data != null));
    }
}