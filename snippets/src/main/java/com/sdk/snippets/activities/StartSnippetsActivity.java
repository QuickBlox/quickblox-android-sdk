package com.sdk.snippets.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TabHost;
import com.sdk.snippets.R;

public class StartSnippetsActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        TabHost.TabSpec auth = tabHost.newTabSpec("tab1");
        TabHost.TabSpec users = tabHost.newTabSpec("tab2");
        TabHost.TabSpec locations = tabHost.newTabSpec("tab3");
        TabHost.TabSpec content = tabHost.newTabSpec("tab4");
        TabHost.TabSpec messages = tabHost.newTabSpec("tab5");
        TabHost.TabSpec customObjects = tabHost.newTabSpec("tab6");
        TabHost.TabSpec chat = tabHost.newTabSpec("tab8");

        auth.setIndicator("Auth")
                .setContent(new Intent(this, AuthActivity.class));

        users.setIndicator("Users")
                .setContent(new Intent(this, UsersActivity.class));

        locations.setIndicator("Locations")
                .setContent(new Intent(this, LocationsActivity.class));

        content.setIndicator("Content")
                .setContent(new Intent(this, ContentActivity.class));

        messages.setIndicator("Push notifications")
                .setContent(new Intent(this, PushNotificationsActivity.class));

        customObjects.setIndicator("Custom Objects")
                .setContent(new Intent(this, CustomObjectsActivity.class));

        chat.setIndicator("Chat")
                .setContent(new Intent(this, ChatActivity.class));

        tabHost.addTab(auth);
        tabHost.addTab(chat);
        tabHost.addTab(customObjects);
        tabHost.addTab(messages);
        tabHost.addTab(content);
        tabHost.addTab(users);
        tabHost.addTab(locations);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}