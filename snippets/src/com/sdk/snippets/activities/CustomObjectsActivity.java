package com.sdk.snippets.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.sdk.snippets.R;
import com.sdk.snippets.Snippets;
import com.sdk.snippets.SnippetsList;
import com.sdk.snippets.modules.SnippetsContent;
import com.sdk.snippets.modules.SnippetsCustomObjects;

public class CustomObjectsActivity extends BaseSnippetsActivity {

    @Override
    public Snippets onCreateSnippets(Context context) {
        return new SnippetsCustomObjects(this);
    }

}
