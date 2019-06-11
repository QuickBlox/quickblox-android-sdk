package com.quickblox.sample.chat.java.ui.adapter.listeners;

import android.widget.TextView;

import com.quickblox.sample.chat.java.utils.MessageTextClickMovement;


/**
 * Interface used to handle Long clicks on the {@link TextView} and taps
 * on the phone, web, mail links inside of {@link TextView}.
 */
public interface MessageLinkClickListener {

    /**
     * This method will be invoked when user press and hold
     * finger on the {@link TextView}
     *
     * @param linkText          Text which contains link on which user presses.
     * @param linkType          Type of the link can be one of {@link MessageTextClickMovement.LinkType} enumeration
     * @param positionInAdapter Index of item with this TextView in message adapter
     */
    void onLinkClicked(final String linkText, final MessageTextClickMovement.LinkType linkType, int positionInAdapter);

    /**
     * @param text              Whole text of {@link TextView}
     * @param positionInAdapter Index of item with this TextView in message adapter
     */
    void onLongClick(final String text, int positionInAdapter);
}