package com.quickblox.android.framework.snippets.activities;

import android.app.Activity;
import android.os.Bundle;
import com.quickblox.android.framework.snippets.R;
import com.quickblox.android.framework.snippets.SnippetsList;
import com.quickblox.android.framework.snippets.modules.SnippetsContent;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 14:56
 */
public class ContentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        SnippetsContent snippets = new SnippetsContent(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);
    }
}