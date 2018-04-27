package com.quickblox.sample.videochatkotlin.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.quickblox.sample.core.utils.ResourceUtils
import com.quickblox.sample.videochatkotlin.R

class PreviewSharingFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_item_screen_share, container, false)
        Glide.with(this)
                .load(arguments!!.getInt(PREVIEW_IMAGE))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(ResourceUtils.getDimen(R.dimen.pager_image_width),
                        ResourceUtils.getDimen(R.dimen.pager_image_height))
                .into(view.findViewById(R.id.image_preview) as ImageView)
        return view
    }

    companion object {

        val PREVIEW_IMAGE = "preview_image"


        fun newInstance(imageResourceId: Int): Fragment {
            val previewFragment = PreviewSharingFragment()
            val bundle = Bundle()
            bundle.putInt(PREVIEW_IMAGE, imageResourceId)
            previewFragment.arguments = bundle
            return previewFragment

        }
    }
}