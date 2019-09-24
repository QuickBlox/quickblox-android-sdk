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
import com.quickblox.users.model.QBUser


class ScreenShareFragment : BaseToolBarFragment() {
    private val TAG = ScreenShareFragment::class.simpleName
    private var onSharingEvents: OnSharingEvents? = null
    private var currentCallStateCallback: CallActivity.CurrentCallStateCallback? = null

    companion object {
        fun newInstance(): ScreenShareFragment = ScreenShareFragment()
    }

    override fun getFragmentLayout(): Int {
        return R.layout.fragment_pager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        val adapter = ImagesAdapter(childFragmentManager)

        val pager = view?.findViewById<View>(R.id.pager) as ViewPager
        pager.adapter = adapter

        val context = activity as Context
        toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.white))

        return view
    }

    override fun onResume() {
        super.onResume()
        currentCallStateCallback = CurrentCallStateCallbackImpl()
        (activity as CallActivity).addCurrentCallStateListener(currentCallStateCallback!!)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.screen_share_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.stop_screen_share -> {
                Log.d(TAG, "stop_screen_share")
                onSharingEvents?.onStopPreview()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context?) {
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
        currentCallStateCallback?.let {
            (activity as CallActivity).removeCurrentCallStateListener(it)
        }
    }

    class ImagesAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        private val images = intArrayOf(R.drawable.pres_img, R.drawable.p2p, R.drawable.group_call, R.drawable.opponents)

        override fun getCount(): Int {
            return images.size
        }

        override fun getItem(position: Int): Fragment {
            return PreviewFragment.newInstance(images[position])
        }
    }

    private inner class CurrentCallStateCallbackImpl : CallActivity.CurrentCallStateCallback {
        override fun onCallStarted() {

        }

        override fun onCallStopped() {

        }

        override fun onOpponentsListUpdated(newUsers: ArrayList<QBUser>) {
        }

        override fun onCallTimeUpdate(time: String) {
            toolbar.title = ""
            val timerTextView = toolbar.findViewById<TextView>(R.id.timer_call)
            timerTextView.visibility = View.VISIBLE
            timerTextView.text = time
        }
    }

    interface OnSharingEvents {
        fun onStopPreview()
    }
}