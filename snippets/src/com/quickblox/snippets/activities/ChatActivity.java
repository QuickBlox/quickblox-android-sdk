package com.quickblox.snippets.activities;

import android.app.Activity;
import android.os.Bundle;
import com.quickblox.snippets.R;
import com.quickblox.snippets.SnippetsList;
import com.quickblox.snippets.modules.SnippetsChat;

/**
 * User: Oleg Soroka
 * Date: 11.10.12
 * Time: 14:56
 */
public class ChatActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.snippets_list);

        SnippetsChat snippets = new SnippetsChat(this);
        SnippetsList list = (SnippetsList) findViewById(R.id.list);
        list.initialize(snippets);
    }
}