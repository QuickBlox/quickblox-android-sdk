package com.quickblox.sample.groupchatwebrtc.fragments.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class ChooserDialog extends DialogFragment {

    private static final String TITLE = "title";
    private static final String ITEM_VALUES = "values";
    public static String ITEMS = "items";

    private OnItemChooserDialogListener listener;

    public static ChooserDialog newInstance(String title, String[] items, String[] values ) {
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putStringArray(ITEMS, items);
        args.putStringArray(ITEM_VALUES, values);
        ChooserDialog fragment = new ChooserDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] items = getArguments().getStringArray(ITEMS);
        final String[] values = getArguments().getStringArray(ITEM_VALUES);
        String title = getArguments().getString(TITLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (listener != null) {
                    listener.onItemSelected(values[which]);
                }
            }
        });
        return builder.create();
    }

    public void setOnItemChooserDialogListener(OnItemChooserDialogListener listener) {
        this.listener = listener;
    }

    public interface OnItemChooserDialogListener {

        void onItemSelected(String item);

    }
}
