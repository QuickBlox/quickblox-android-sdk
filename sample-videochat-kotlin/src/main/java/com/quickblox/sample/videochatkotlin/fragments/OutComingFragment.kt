package com.quickblox.sample.videochatkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quickblox.sample.videochatkotlin.R
import com.quickblox.sample.videochatkotlin.view.CameraPreview
import android.widget.FrameLayout


class OutComingFragment : Fragment() {
    private val TAG = OutComingFragment::class.java.simpleName

    val cameraFront = 1
    lateinit var cameraPreview: CameraPreview
    lateinit var frameLayout:FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true

        Log.d(TAG, "onCreate() from OutComingFragment")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_outcome_call, container, false)
        frameLayout = view.findViewById(R.id.camera_preview) as FrameLayout
        return view
    }

    override fun onResume() {
        super.onResume()
        startCameraPreview()
    }

    fun startCameraPreview() {
        cameraPreview = CameraPreview(activity, cameraFront)
        frameLayout.addView(cameraPreview)
    }

    fun stopCameraPreview() {
        cameraPreview.stop()
    }

    override fun onPause() {
        super.onPause()
        stopCameraPreview()
    }
}