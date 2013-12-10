package com.quickblox.snippets.activities;

import android.app.Activity;
import android.os.Bundle;
import com.quickblox.snippets.R;
import com.quickblox.snippets.SnippetsList;
import com.quickblox.snippets.modules.SnippetsCustomObjectFiles;
import com.quickblox.snippets.modules.SnippetsCustomObjects;

/**
 * Created by vfite on 06.12.13.
 */
public class CustomObjectFilesActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        SnippetsCustomObjectFiles snippets = new SnippetsCustomObjectFiles(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);
    }
}
