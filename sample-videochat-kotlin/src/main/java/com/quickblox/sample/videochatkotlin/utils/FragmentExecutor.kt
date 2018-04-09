package com.quickblox.sample.videochatkotlin.utils

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager



fun addFragment(fragmentManager: FragmentManager, containerId: Int, fragment: Fragment, tag: String) {
    fragmentManager.beginTransaction().replace(containerId, fragment, tag).commitAllowingStateLoss()
}

fun addFragmentAtTop(fragmentManager: FragmentManager, containerId: Int, fragment: Fragment, tag: String) {
    fragmentManager.beginTransaction().add(containerId, fragment, tag).commitAllowingStateLoss()
}

fun addFragmentWithBackStack(fragmentManager: FragmentManager, containerId: Int, fragment: Fragment, tag: String) {
    fragmentManager.beginTransaction().replace(containerId, fragment, tag).addToBackStack(null).commitAllowingStateLoss()
}

fun removeFragment(fragmentManager: FragmentManager, fragment: Fragment) {
    fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
}