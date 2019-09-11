package com.quickblox.sample.videochat.java.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.quickblox.sample.videochat.java.App;
import com.quickblox.sample.videochat.java.R;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PreviewFragment extends Fragment {

    public static final String PREVIEW_IMAGE = "preview_image";

    public static Fragment newInstance(int imageResourceId) {
        PreviewFragment previewFragment = new PreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PREVIEW_IMAGE, imageResourceId);
        previewFragment.setArguments(bundle);
        return previewFragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_item_screen_share, container, false);
        Glide.with(getActivity())
                .load(getArguments().getInt(PREVIEW_IMAGE))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override((int) getResources().getDimension(R.dimen.pager_image_width),
                        (int) getResources().getDimension(R.dimen.pager_image_height))
                .into((ImageView) view.findViewById(R.id.image_preview));
        return view;
    }
}