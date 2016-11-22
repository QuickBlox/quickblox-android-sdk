package com.quickblox.sample.groupchatwebrtc.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * QuickBlox team
 */
public class FragmentExecuotr {

    public static void addFragment(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().replace(containerId, fragment, tag).commitAllowingStateLoss();
    }

    public static void addFragmentAtTop(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().add(containerId, fragment, tag).commitAllowingStateLoss();
    }

    public static void addFragmentWithBackStack(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().replace(containerId, fragment, tag).addToBackStack(null).commitAllowingStateLoss();
    }

    public static void removeFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
    }
}
