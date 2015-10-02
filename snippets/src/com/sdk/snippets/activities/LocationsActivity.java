package com.sdk.snippets.activities;

import android.app.Activity;
import android.os.Bundle;
import com.sdk.snippets.R;
import com.sdk.snippets.core.SnippetsList;
import com.sdk.snippets.modules.SnippetsLocation;

public class LocationsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        SnippetsLocation snippets = new SnippetsLocation(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);
    }
}