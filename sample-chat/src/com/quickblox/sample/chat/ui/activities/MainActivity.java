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
import java.util.Locale;

public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        List<Fragment> tabs = new ArrayList<Fragment>();
        tabs.add(new UsersFragment());
        tabs.add(new RoomsFragment());

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), tabs);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                    .setText(mSectionsPagerAdapter.getPageTitle(i))
                    .setTabListener(this));
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        int position = tab.getPosition();
        mViewPager.setCurrentItem(position);
        switch (position) {
            case 0:
                ((UsersFragment) mSectionsPagerAdapter.getItem(position)).updateData();
                break;
            case 1:
                ((RoomsFragment) mSectionsPagerAdapter.getItem(position)).updateData();
                break;
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        List<Fragment> fragments;

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
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Users";
                case 1:
                    return "Rooms";
            }
            return null;
        }
    }
}
