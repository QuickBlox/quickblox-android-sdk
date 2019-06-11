package com.quickblox.sample.videochatkotlin.fragments

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.quickblox.sample.videochatkotlin.R
import kotlinx.android.synthetic.main.fragment_pager.*

class ScreenShareFragment : BaseToolBarFragment() {
    val TAG = ScreenShareFragment::class.java.simpleName
    private var onSharingEvents: OnSharingEvents? = null

    override val fragmentLayout: Int
        get() = R.layout.fragment_pager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = MyAdapter(childFragmentManager)
        pager.adapter = adapter
    }

    override fun initActionBar() {
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.white)))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.screen_share_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.stop_screen_share -> {
                Log.d(TAG, "stop_screen_share")
                if (onSharingEvents != null) {
                    onSharingEvents!!.onStopSharingPreview()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        onSharingEvents = context as OnSharingEvents
    }

    override fun onDetach() {
        super.onDetach()
        onSharingEvents = null
    }

    interface OnSharingEvents {
        fun onStopSharingPreview()
    }

    companion object {
        fun newInstance() = ScreenShareFragment()
    }

    class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val images = intArrayOf(R.drawable.pres_img, R.drawable.splash_screen, R.drawable.users_screen)

        override fun getCount(): Int {
            return images.size
        }

        override fun getItem(position: Int): Fragment {
            return PreviewSharingFragment.newInstance(images[position])
        }
    }
}