package com.quickblox.sample.chat.ui.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.HorizontalScrollView;

public class AttachmentPreviewAdapterView extends HorizontalScrollView {

    private Adapter adapter;
    private DataSetObserver dataSetObserver;

    public AttachmentPreviewAdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);

        dataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                populateContainerWithViews();
            }

            @Override
            public void onInvalidated() {
                populateContainerWithViews();
            }
        };
    }

    public void setAdapter(Adapter newAdapter) {
        if (adapter != null) {
            adapter.unregisterDataSetObserver(dataSetObserver);
        }
        adapter = newAdapter;
        adapter.registerDataSetObserver(dataSetObserver);
        populateContainerWithViews();
    }

    private void populateContainerWithViews() {
        removeAllViews();
        for (int i = 0; i < adapter.getCount(); i++) {
            View childView = adapter.getView(i, null, this);
            addView(childView);
        }
    }
}
