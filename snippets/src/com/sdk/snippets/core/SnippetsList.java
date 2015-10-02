package com.sdk.snippets.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SnippetsList extends ListView {

    public void initialize(Snippets snippets) {
        final SnippetsAdapter snippetsAdapter = new SnippetsAdapter(getContext(), snippets);

        setAdapter(snippetsAdapter);

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snippet snippet = snippetsAdapter.getSnippet(i);
                snippet.performExecution();
            }
        });
    }

    public SnippetsList(Context context) {
        super(context);
    }

    public SnippetsList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SnippetsList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}