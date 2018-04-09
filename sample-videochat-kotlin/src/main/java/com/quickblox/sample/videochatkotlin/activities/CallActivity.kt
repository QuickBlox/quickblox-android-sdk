package com.quickblox.sample.videochatkotlin.activities

import android.os.Bundle
import android.util.Log
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.videochatkotlin.R

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity() {
    val TAG = CallActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        initSuitableFragment()
    }

    fun initSuitableFragment() {
//        val loginFrag = LoginFragment()
//        addFragment(supportFragmentManager, R.id.fragment_container, loginFrag, LOGIN_FRAGMENT)
    }

    fun initOutgoingFragment() {

    }

    fun initIncomingFragment() {

    }

}