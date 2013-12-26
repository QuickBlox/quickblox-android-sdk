package com.quickblox.sample.chat.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.quickblox.sample.chat.R;
import com.quickblox.sample.chat.ui.fragments.RoomsFragment;
import com.quickblox.sample.chat.ui.fragments.UsersFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        List<Fragment> tabs = new ArrayList<Fragment>();
        tabs.add(UsersFragment.getInstance());
        tabs.add(RoomsFragment.getInstance());

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), tabs);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < sectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(sectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        viewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public static class SectionsPagerAdapter extends FragmentPagerAdapter {

        private static final int POSITION_USER = 0;
        private static final int POSITION_ROOM = 1;
        private List<Fragment> fragments;

        public SectionsPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case POSITION_USER:
                    return "Users";
                case POSITION_ROOM:
                    return "Rooms";
            }
            return null;
        }
    }
}
