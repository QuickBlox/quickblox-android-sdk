package com.quickblox.chat_v2.activitys;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import com.quickblox.chat_v2.R;
import com.quickblox.chat_v2.R.layout;
import com.quickblox.chat_v2.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import com.quickblox.chat_v2.fragment.ContactsFragment;
import com.quickblox.chat_v2.fragment.DialogsFragment;
import com.quickblox.chat_v2.fragment.ProfileFragment;
import com.quickblox.chat_v2.fragment.RoomsFragment;

public class MainTabActivity extends FragmentActivity {

    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_tabs);
        mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        mTabHost.addTab(mTabHost.newTabSpec("dialogs").setIndicator(getString(R.string.maintab_tab_1)),
                DialogsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("rooms").setIndicator(getString(R.string.maintab_tab_2)),
                RoomsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("contacts").setIndicator(getString(R.string.maintab_tab_3)),
                ContactsFragment.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("profile").setIndicator(getString(R.string.maintab_tab_4)),
                ProfileFragment.class, null);
    }
	
}
