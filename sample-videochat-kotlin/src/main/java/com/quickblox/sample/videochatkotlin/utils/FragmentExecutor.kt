package com.quickblox.sample.videochatkotlin.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

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

fun popBackStackFragment(fragmentManager: FragmentManager) {
    fragmentManager.popBackStack()
}