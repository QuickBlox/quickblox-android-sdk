package com.quickblox.sample.chat.ui.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.RelativeLayout;

public class AttachmentPreviewAdapterView extends RelativeLayout {

    private static String TAG = AttachmentPreviewAdapterView.class.getSimpleName();

    private static final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private Adapter adapter;
    private DataSetObserver dataSetObserver;

    public AttachmentPreviewAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                populateWithViewsFromAdapter();
            }

            @Override
            public void onInvalidated() {
                populateWithViewsFromAdapter();
            }
        };
    }

    public void setAdapter(Adapter newAdapter) {
        if (adapter != null) {
            adapter.unregisterDataSetObserver(dataSetObserver);
        }
        adapter = newAdapter;
        adapter.registerDataSetObserver(dataSetObserver);
        populateWithViewsFromAdapter();
    }

    private void populateWithViewsFromAdapter() {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                removeAllViews();
                for (int i = 0; i < adapter.getCount(); i++) {
                    View childView = adapter.getView(i, null, AttachmentPreviewAdapterView.this);
                    addView(childView, i);
                }
            }
        });
    }
}
