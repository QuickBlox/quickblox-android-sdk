package com.sdk.snippets.activities;

import android.content.Context;
import com.sdk.snippets.core.Snippets;
import com.sdk.snippets.modules.SnippetsCustomObjects;

public class CustomObjectsActivity extends BaseSnippetsActivity {

    @Override
    public Snippets onCreateSnippets(Context context) {
        return new SnippetsCustomObjects(this);
    }

}
