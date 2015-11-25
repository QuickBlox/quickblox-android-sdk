package com.quickblox.sample.groupchatwebrtc.util;

import android.app.Fragment;
import android.app.FragmentManager;

/**
 * Created by vadim on 11/9/15.
 */
public class FragmentExecuotr {


    public static void addFragment(FragmentManager fragmentManager, int containerId, Fragment fragment, String tag) {
        fragmentManager.beginTransaction().replace(containerId, fragment, tag).commitAllowingStateLoss();
    }

    public static void removeFragment(FragmentManager fragmentManager, Fragment fragment) {
        fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss();
    }

}
