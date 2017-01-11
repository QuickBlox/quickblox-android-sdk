package com.quickblox.sample.groupchatwebrtc.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.groupchatwebrtc.R;


public class ScreenShareFragment extends BaseToolBarFragment {

    public static final String TAG = ScreenShareFragment.class.getSimpleName();
    private OnSharingEvents onSharingEvents;

    @Override
    int getFragmentLayout() {
        return R.layout.fragment_pager;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        MyAdapter adapter = new MyAdapter(getChildFragmentManager());

        ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        toolbar.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
        return view;
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
                Log.d(TAG, "stop_screen_share");
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
        onSharingEvents = (OnSharingEvents) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSharingEvents = null;
    }

    public interface OnSharingEvents{
        void onStopPreview();
    }

    public static ScreenShareFragment newIntstance() {
        return new ScreenShareFragment();
    }

    public static class MyAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 4;

        private int[] images = {R.drawable.pres_img, R.drawable.p2p, R.drawable.group_call, R.drawable.opponents};

        public MyAdapter(FragmentManager fm) {
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

}
