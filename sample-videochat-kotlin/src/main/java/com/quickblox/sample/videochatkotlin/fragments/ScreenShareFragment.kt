package com.quickblox.sample.videochatkotlin.fragments

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.*
import com.quickblox.sample.videochatkotlin.R

class ScreenShareFragment : BaseToolBarFragment() {
    val TAG = ScreenShareFragment::class.java.simpleName
    private var onSharingEvents: OnSharingEvents? = null

    override val fragmentLayout: Int
        get() = R.layout.fragment_pager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)!!

        val adapter = MyAdapter(childFragmentManager)

        val pager = view.findViewById<View>(R.id.pager) as ViewPager
        pager.adapter = adapter

        return view
    }

    override fun initActionBar() {
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.white)))
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.screen_share_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.stop_screen_share -> {
                Log.d(TAG, "stop_screen_share")
                if (onSharingEvents != null) {
                    onSharingEvents!!.onStopSharingPreview()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onSharingEvents = context as OnSharingEvents?
    }

    override fun onDetach() {
        super.onDetach()
        onSharingEvents = null
    }

    interface OnSharingEvents {
        fun onStopSharingPreview()
    }

    class MyAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val images = intArrayOf(R.drawable.pres_img)

        override fun getCount(): Int {
            return NUM_ITEMS
        }

        override fun getItem(position: Int): Fragment {
            return PreviewSharingFragment.newInstance(images[position])
        }

        companion object {
            private val NUM_ITEMS = 1
        }
    }
}
