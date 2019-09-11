package com.quickblox.sample.videochat.java.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.sample.videochat.java.R;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public abstract class BaseToolBarFragment extends Fragment {

    private static final String TAG = BaseToolBarFragment.class.getSimpleName();
    protected Handler mainHandler;
    protected Toolbar toolbar;
    protected ActionBar actionBar;

    abstract int getFragmentLayout();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mainHandler = new FragmentLifeCycleHandler(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getFragmentLayout(), container, false);
        initActionBar();
        return view;
    }

    private void initActionBar() {
        toolbar = getActivity().findViewById(R.id.toolbar_call);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        actionBar = ((AppCompatActivity) getActivity()).getDelegate().getSupportActionBar();
    }

    static class FragmentLifeCycleHandler extends Handler {

        private WeakReference<Fragment> fragmentRef;

        FragmentLifeCycleHandler(Fragment fragment) {

            this.fragmentRef = new WeakReference<>(fragment);
        }

        @Override
        public void dispatchMessage(Message msg) {
            Fragment fragment = fragmentRef.get();
            if (fragment == null) {
                return;
            }
            if (fragment.isAdded() && (fragment.getActivity() != null)) {
                super.dispatchMessage(msg);
            } else {
                Log.d(TAG, "Fragment under destroying");
            }
        }
    }
}