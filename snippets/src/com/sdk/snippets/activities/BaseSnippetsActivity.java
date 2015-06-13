package com.sdk.snippets.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.sdk.snippets.R;
import com.sdk.snippets.Snippets;
import com.sdk.snippets.SnippetsList;
import com.sdk.snippets.modules.SnippetsAuth;

/**
 * Created by Вадим on 13.06.2015.
 */
public abstract class BaseSnippetsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        Snippets snippets = onCreateSnippets(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);
    }

    public abstract Snippets onCreateSnippets(Context context);
}
