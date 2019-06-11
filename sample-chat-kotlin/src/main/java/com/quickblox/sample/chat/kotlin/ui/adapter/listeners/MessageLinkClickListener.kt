package com.quickblox.sample.chat.kotlin.ui.adapter.listeners

import android.widget.TextView
import com.quickblox.sample.chat.kotlin.utils.MessageTextClickMovement

/**
 * Interface used to handle Long clicks on the {@link TextView} and taps
 * on the phone, web, mail links inside of {@link TextView}.
 */
interface MessageLinkClickListener {

    /**
     * This method will be invoked when user press and hold
     * finger on the [TextView]
     *
     * @param linkText          Text which contains link on which user presses.
     * @param linkType          Type of the link can be one of [MessageTextClickMovement.QBLinkType] enumeration
     * @param positionInAdapter Index of item with this TextView in message adapter
     */
    fun onLinkClicked(linkText: String, linkType: MessageTextClickMovement.QBLinkType, positionInAdapter: Int)

    /**
     * @param text              Whole text of [TextView]
     * @param positionInAdapter Index of item with this TextView in message adapter
     */
    fun onLongClick(text: String, positionInAdapter: Int)
}