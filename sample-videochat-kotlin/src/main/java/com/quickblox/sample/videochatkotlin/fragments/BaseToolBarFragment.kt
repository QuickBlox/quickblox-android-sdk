package com.quickblox.sample.videochatkotlin.fragments

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.quickblox.chat.QBChatService
import com.quickblox.sample.videochatkotlin.R
import java.lang.ref.WeakReference

abstract class BaseToolBarFragment : Fragment() {
    private var TAG = BaseToolBarFragment::class.java.simpleName
    lateinit var mainHandler: Handler
    lateinit var actionBar: ActionBar

    protected abstract val fragmentLayout: Int

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
        actionBar.title = String.format(QBChatService.getInstance().user.login)
        actionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context!!, R.color.black_transparent_50)))
    }


    class FragmentLifeCycleHandler(fragment: Fragment) : Handler() {

        private val fragmentRef: WeakReference<Fragment> = WeakReference(fragment)

        override fun dispatchMessage(msg: Message) {
            val fragment = fragmentRef.get() ?: return
            if (fragment.isAdded && fragment.activity != null) {
                super.dispatchMessage(msg)
            } else {
                Log.d("BaseToolBarFragment", "Fragment under destroying")
            }
        }
    }
}