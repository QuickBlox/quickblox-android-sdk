package com.quickblox.sample.videochat.kotlin.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.quickblox.sample.videochat.kotlin.R
import java.lang.ref.WeakReference

abstract class BaseToolBarFragment : Fragment() {
    protected lateinit var actionBar: ActionBar
    protected var toolbar: Toolbar? = null

    protected var mainHandler: Handler? = null

    internal abstract fun getFragmentLayout(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
        mainHandler = FragmentLifeCycleHandler(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getFragmentLayout(), container, false)
        initActionBar()
        return view
    }

    private fun initActionBar() {
        toolbar = activity?.findViewById(R.id.toolbar_call)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        actionBar = (activity as AppCompatActivity).delegate.supportActionBar as ActionBar
    }

    internal class FragmentLifeCycleHandler(fragment: Fragment) : Handler() {

        private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment)

        override fun dispatchMessage(msg: Message) {
            val fragment = fragmentRef.get() ?: return
            if (fragment.isAdded && fragment.activity != null) {
                super.dispatchMessage(msg)
            }
        }
    }
}