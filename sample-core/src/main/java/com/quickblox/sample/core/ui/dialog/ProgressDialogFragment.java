package com.quickblox.sample.core.ui.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;

import com.quickblox.sample.core.R;

public class ProgressDialogFragment extends DialogFragment {

    private static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private static final String ARG_MESSAGE_ID = "message_id";

    public static void show(FragmentManager fm) {
        show(fm, R.string.dlg_loading);
    }

    public static void show(FragmentManager fm, @StringRes int messageId) {
        // We're not using dialogFragment.show() method because we may call this DialogFragment
        // in onActivityResult() method and there will be a state loss exception
        Log.d(TAG, "show");
        if (fm.findFragmentByTag(TAG) == null) {
            Log.d(TAG, "fm.findFragmentByTag(TAG) == null");
            fm.beginTransaction().add(newInstance(messageId), TAG).commitAllowingStateLoss();
        }
        Log.d(TAG, "backstack = " + fm.getFragments());
    }

    public static void hide(FragmentManager fm) {
        DialogFragment fragment = (DialogFragment) fm.findFragmentByTag(TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
            Log.d(TAG, "fm.beginTransaction().remove(fragment)" + fragment);
        }
        Log.d(TAG, "backstack = " + fm.getFragments());
    }

    public static ProgressDialogFragment newInstance(@StringRes int messageId) {
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE_ID, messageId);

        ProgressDialogFragment dialog = new ProgressDialogFragment();
        dialog.setArguments(args);
        Log.d(TAG, "newInstance = " + dialog);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getString(getArguments().getInt(ARG_MESSAGE_ID)));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        // Disable the back button
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        };
        dialog.setOnKeyListener(keyListener);

        return dialog;
    }
}