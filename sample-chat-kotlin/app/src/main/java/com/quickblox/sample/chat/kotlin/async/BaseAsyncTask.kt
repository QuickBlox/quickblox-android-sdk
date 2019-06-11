package com.quickblox.sample.chat.kotlin.async

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log


abstract class BaseAsyncTask<Params, Progress, Result> : AsyncTask<Params, Progress, Result>() {
    private val TAG = BaseAsyncTask::class.java.simpleName

    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var isExceptionOccurred: Boolean = false

    override fun doInBackground(vararg params: Params): Result? {
        try {
            return performInBackground(*params)
        } catch (e: Exception) {
            isExceptionOccurred = true
            mainThreadHandler.post { onException(e) }
            return null
        }
    }

    override fun onPostExecute(result: Result) {
        if (!isExceptionOccurred) {
            onResult(result)
        }
    }

    @Throws(Exception::class)
    abstract fun performInBackground(vararg params: Params): Result?

    abstract fun onResult(result: Result?)

    open fun onException(e: Exception) {
        Log.w(TAG, e)
    }
}