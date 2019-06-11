package com.quickblox.sample.chat.kotlin.ui.dialog

import android.app.Dialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.quickblox.sample.chat.kotlin.R

private const val ARG_MESSAGE_ID = "message_id"

class ProgressDialogFragment : DialogFragment() {

    companion object {
        private val TAG = ProgressDialogFragment::class.java.simpleName

        fun show(fragmentManager: FragmentManager) {
            // We're not using dialogFragment.show() method because we may call this DialogFragment
            // in onActivityResult() method and there will be a state loss exception
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                Log.d(TAG, "fragmentManager.findFragmentByTag(TAG) == null")
                val args = Bundle()
                args.putInt(ARG_MESSAGE_ID, R.string.dlg_loading)
                val dialog = ProgressDialogFragment()
                dialog.arguments = args
                Log.d(TAG, "newInstance = $dialog")
                fragmentManager.beginTransaction().add(dialog, TAG).commitAllowingStateLoss()
            }
            Log.d(TAG, "backstack = " + fragmentManager.fragments)
        }

        fun hide(fragmentManager: FragmentManager) {
            val fragment = fragmentManager.findFragmentByTag(TAG)
            fragment?.let {
                fragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
                Log.d(TAG, "fragmentManager.beginTransaction().remove(fragment)$fragment")
            }
            Log.d(TAG, "backstack = " + fragmentManager.fragments)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ProgressDialog(activity)
        dialog.setMessage(getString(arguments?.getInt(ARG_MESSAGE_ID) ?: R.string.dlg_loading))
        dialog.isIndeterminate = true
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        // Disable the back button
        val keyListener = DialogInterface.OnKeyListener {
            dialog,
            keyCode,
            event -> keyCode == KeyEvent.KEYCODE_BACK
        }
        dialog.setOnKeyListener(keyListener)
        return dialog
    }
}