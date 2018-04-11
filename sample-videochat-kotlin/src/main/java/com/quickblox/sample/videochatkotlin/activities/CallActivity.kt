package com.quickblox.sample.videochatkotlin.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.quickblox.sample.core.ui.activity.CoreBaseActivity
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.services.CallService
import com.quickblox.sample.videochatkotlin.utils.COMMAND_LOGOUT
import com.quickblox.sample.videochatkotlin.utils.EXTRA_COMMAND_TO_SERVICE

/**
 * Created by roman on 4/6/18.
 */
class CallActivity : CoreBaseActivity() {
    val TAG = CallActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)
        setActionBarTitle(R.string.title_call_activity)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_call, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_logout_user_done -> {
                startLogout()
                startLoginActivity()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun startLogout() {
        val intent = Intent(this, CallService::class.java)
        intent.putExtra(EXTRA_COMMAND_TO_SERVICE, COMMAND_LOGOUT)
        startService(intent)
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        this.startActivity(intent)
        finish()
    }
}