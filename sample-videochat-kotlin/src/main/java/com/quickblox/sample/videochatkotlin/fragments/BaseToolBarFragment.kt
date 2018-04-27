package com.quickblox.sample.videochatkotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.lang.ref.WeakReference

abstract class BaseToolBarFragment : Fragment() {
    private var TAG = BaseToolBarFragment::class.java.simpleName
    lateinit var mainHandler: Handler
    protected var actionBar: ActionBar? = null

    internal abstract val fragmentLayout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mainHandler = FragmentLifeCycleHandler(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(fragmentLayout, container, false)
        initActionBar()
        return view
    }

    private fun initActionBar() {
        actionBar = (activity as AppCompatActivity).delegate.supportActionBar
    }


    inner class FragmentLifeCycleHandler(fragment: Fragment) : Handler() {

        private val fragmentRef: WeakReference<Fragment>

        init {

            this.fragmentRef = WeakReference(fragment)
        }

        override fun dispatchMessage(msg: Message) {
            val fragment = fragmentRef.get() ?: return
            if (fragment.isAdded && fragment.activity != null) {
                super.dispatchMessage(msg)
            } else {
                Log.d(TAG, "Fragment under destroying")
            }
        }
    }
}