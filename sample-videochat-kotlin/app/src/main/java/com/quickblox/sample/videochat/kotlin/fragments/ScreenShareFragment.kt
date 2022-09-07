package com.quickblox.sample.videochat.kotlin.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.quickblox.sample.videochat.kotlin.R
import com.quickblox.sample.videochat.kotlin.activities.CallActivity

class ScreenShareFragment : BaseToolBarFragment() {
    private val TAG = ScreenShareFragment::class.simpleName
    private var onSharingEvents: OnSharingEvents? = null

    companion object {
        fun newInstance(): ScreenShareFragment = ScreenShareFragment()
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_pager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        view?.let {
            val adapter = ImagesAdapter(childFragmentManager)
            val pager: ViewPager = it.findViewById(R.id.pager)
            pager.adapter = adapter
        }

        toolbar?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))

        return view
    }

    override fun onResume() {
        super.onResume()
        (activity as CallActivity).addCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.screen_share_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.stop_screen_share -> {
                Log.d(TAG, "stop_screen_share")
                onSharingEvents?.onStopPreview()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onSharingEvents = context as OnSharingEvents?
        } catch (e: ClassCastException) {
            throw ClassCastException(activity?.toString() + " must implement OnSharingEvents")
        }
    }

    override fun onDetach() {
        super.onDetach()
        onSharingEvents = null
    }

    override fun onPause() {
        super.onPause()
        (activity as CallActivity).removeCallTimeUpdateListener(CallTimeUpdateListenerImpl(TAG))
    }

    class ImagesAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val images =
            intArrayOf(R.drawable.pres_img, R.drawable.p2p, R.drawable.group_call, R.drawable.opponents)

        override fun getCount(): Int {
            return images.size
        }

        override fun getItem(position: Int): Fragment {
            return PreviewFragment.newInstance(images[position])
        }
    }

    private inner class CallTimeUpdateListenerImpl(val tag: String?) : CallActivity.CallTimeUpdateListener {
        override fun updatedCallTime(time: String) {
            toolbar?.title = ""
            val timerTextView: TextView? = toolbar?.findViewById(R.id.timer_call)
            timerTextView?.visibility = View.VISIBLE
            timerTextView?.text = time
        }

        override fun equals(other: Any?): Boolean {
            if (other is CallTimeUpdateListenerImpl) {
                return tag == other.tag
            }
            return false
        }

        override fun hashCode(): Int {
            var hash = 1
            hash = 31 * hash + tag.hashCode()
            return hash
        }
    }

    interface OnSharingEvents {
        fun onStopPreview()
    }
}