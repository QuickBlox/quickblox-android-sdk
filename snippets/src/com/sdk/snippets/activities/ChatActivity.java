package com.sdk.snippets.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.sdk.snippets.R;
import com.sdk.snippets.Snippets;
import com.sdk.snippets.SnippetsList;
import com.sdk.snippets.modules.SnippetsAuth;
import com.sdk.snippets.modules.SnippetsChat;

public class ChatActivity extends BaseSnippetsActivity{

    @Override
    public Snippets onCreateSnippets(Context context) {
        return new SnippetsChat(this);
    }
}