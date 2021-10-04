package com.quickblox.sample.conference.kotlin.presentation.utils

import android.R
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
private const val ANIMATION_DURATION_HIDE = 1800L
private const val ANIMATION_DURATION_SHOW = 250L

fun View.setOnClick(doClick: (View) -> Unit) = setOnClickListener(DebouchingOnClickListener(doClick = doClick))

fun View.hideWithAnimation() {
    val fadeOut = AnimationUtils.loadAnimation(this.context, R.anim.fade_out)
    fadeOut.interpolator = DecelerateInterpolator()
    fadeOut.duration = ANIMATION_DURATION_HIDE
    this.startAnimation(fadeOut)
    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            // empty
        }

        override fun onAnimationEnd(animation: Animation) {
            this@hideWithAnimation.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation) {
            // empty
        }
    })
}

fun View.showWithAnimation() {
    val fadeIn = AnimationUtils.loadAnimation(this.context, R.anim.fade_in)
    fadeIn.interpolator = DecelerateInterpolator()
    fadeIn.duration = ANIMATION_DURATION_SHOW
    this.startAnimation(fadeIn)
    fadeIn.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            this@showWithAnimation.visibility = View.VISIBLE
        }

        override fun onAnimationEnd(animation: Animation) {
            // empty
        }

        override fun onAnimationRepeat(animation: Animation) {
            // empty
        }
    })
}