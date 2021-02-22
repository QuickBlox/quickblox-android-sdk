package com.quickblox.sample.chat.java.utils.imagepick.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.sample.chat.java.R;
import com.quickblox.sample.chat.java.utils.ImageUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MediaSourcePickDialogFragment extends DialogFragment {
    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    private static final int POSITION_FILE = 2;

    private OnImageSourcePickedListener onImageSourcePickedListener;

    public static void show(FragmentManager fm, OnImageSourcePickedListener onImageSourcePickedListener) {
        MediaSourcePickDialogFragment fragment = new MediaSourcePickDialogFragment();
        fragment.setOnImageSourcePickedListener(onImageSourcePickedListener);
        fragment.show(fm, MediaSourcePickDialogFragment.class.getSimpleName());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dlg_choose_file_from);
        builder.setItems(R.array.dlg_image_pick, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case POSITION_GALLERY:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY);
                        break;
                    case POSITION_CAMERA:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA);
                        break;
                    case POSITION_FILE:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.FILE_STORAGE);
                        break;
                }
            }
        });

        return builder.create();
    }

    private void setOnImageSourcePickedListener(OnImageSourcePickedListener onImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener;
    }

    public enum ImageSource {
        GALLERY,
        CAMERA,
        FILE_STORAGE
    }

    public static class ImageSourcePickedListener implements OnImageSourcePickedListener {
        private Fragment fragment;

        public ImageSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onImageSourcePicked(ImageSource source) {
            switch (source) {
                case GALLERY:
                    ImageUtils.startMediaPicker(fragment);
                    break;
                case CAMERA:
                    ImageUtils.startCameraForResult(fragment);
                    break;
                case FILE_STORAGE:
                    ImageUtils.startFilePicker(fragment);
            }
        }
    }

    public interface OnImageSourcePickedListener {

        void onImageSourcePicked(ImageSource source);
    }
}