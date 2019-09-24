package com.quickblox.sample.videochat.java.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.sample.videochat.java.R;
import com.quickblox.sample.videochat.java.activities.CallActivity;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


public class ScreenShareFragment extends BaseToolBarFragment {

    public static final String TAG = ScreenShareFragment.class.getSimpleName();
    private OnSharingEvents onSharingEvents;
    private CallActivity.CurrentCallStateCallback currentCallStateCallback;

    public static ScreenShareFragment newInstance() {
        return new ScreenShareFragment();
    }

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_pager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ImagesAdapter adapter = new ImagesAdapter(getChildFragmentManager());

        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentCallStateCallback = new CurrentCallStateCallbackImpl();
        ((CallActivity) getActivity()).addCurrentCallStateListener(currentCallStateCallback);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.screen_share_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.stop_screen_share:
                Log.d(TAG, "Stop Screen Sharing");
                if (onSharingEvents != null) {
                    onSharingEvents.onStopPreview();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onSharingEvents = (OnSharingEvents) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement OnSharingEvents");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSharingEvents = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (currentCallStateCallback != null) {
            ((CallActivity) getActivity()).removeCurrentCallStateListener(currentCallStateCallback);
        }
    }

    public static class ImagesAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 4;

        private int[] images = {R.drawable.pres_img, R.drawable.p2p, R.drawable.group_call, R.drawable.opponents};

        public ImagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            return PreviewFragment.newInstance(images[position]);
        }
    }

    private class CurrentCallStateCallbackImpl implements CallActivity.CurrentCallStateCallback {
        @Override
        public void onCallStarted() {

        }

        @Override
        public void onCallStopped() {

        }

        @Override
        public void onOpponentsListUpdated(ArrayList<QBUser> newUsers) {

        }

        @Override
        public void onCallTimeUpdate(String time) {
            toolbar.setTitle("");
            TextView timerTextView = toolbar.findViewById(R.id.timer_call);
            timerTextView.setVisibility(View.VISIBLE);
            timerTextView.setText(time);
        }
    }

    public interface OnSharingEvents {
        void onStopPreview();
    }
}