package com.quickblox.sample.videochatkotlin.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quickblox.chat.QBChatService
import com.quickblox.sample.videochatkotlin.R
import java.lang.ref.WeakReference

abstract class BaseToolBarFragment : Fragment() {
    private var TAG = BaseToolBarFragment::class.java.simpleName
    lateinit var mainHandler: Handler
    lateinit var actionBar: ActionBar

    internal abstract val fragmentLayout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        mainHandler = FragmentLifeCycleHandler(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(fragmentLayout, container, false)
        actionBar = (activity as AppCompatActivity).delegate.supportActionBar!!
        initActionBar()
        return view
    }

    open fun initActionBar() {
        actionBar.setTitle(String.format(QBChatService.getInstance().user.login))
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.black_transparent_50)))
        //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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