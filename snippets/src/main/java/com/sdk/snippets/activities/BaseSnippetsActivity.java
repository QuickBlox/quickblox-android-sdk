package com.sdk.snippets.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.sdk.snippets.R;
import com.sdk.snippets.core.Snippets;
import com.sdk.snippets.core.SnippetsListView;


public abstract class BaseSnippetsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        Snippets snippets = onCreateSnippets(this);
        SnippetsListView list = (SnippetsListView) findViewById(R.id.list);
        list.initialize(snippets);
    }

    public abstract Snippets onCreateSnippets(Context context);
}