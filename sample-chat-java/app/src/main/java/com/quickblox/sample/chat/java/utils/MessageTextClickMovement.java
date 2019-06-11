package com.quickblox.sample.chat.java.utils;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.quickblox.core.helper.Lo;
import com.quickblox.sample.chat.java.ui.adapter.listeners.MessageLinkClickListener;

public class MessageTextClickMovement extends ArrowKeyMovementMethod {

    private final GestureDetector gestureDetector;
    private final boolean overrideOnLinkClick;
    private TextView textView;
    private Spannable buffer;
    private int positionInAdapter = -1;

    public MessageTextClickMovement(final MessageLinkClickListener listener, boolean overrideOnClick, final Context context) {
        this.overrideOnLinkClick = overrideOnClick;
        this.gestureDetector = new GestureDetector(context, new SimpleOnGestureListener(listener));
    }

    public void setPositionInAdapter(int positionInAdapter) {
        this.positionInAdapter = positionInAdapter;
    }

    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
        this.textView = widget;
        this.buffer = buffer;
        gestureDetector.onTouchEvent(event);

        //Return super method for delegate logic sending intent from Linkify
        //or return 'false' for yourself managing logic by link clicked
        if (overrideOnLinkClick) {
            return false;
        } else {
            return super.onTouchEvent(widget, buffer, event);
        }
    }

    /**
     * Detects various gestures and events.
     * Notify users when a particular motion event has occurred.
     */
    class SimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        private final MessageLinkClickListener linkClickListener;

        public SimpleOnGestureListener(MessageLinkClickListener linkClickListener) {
            this.linkClickListener = linkClickListener;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            // Notified when a tap occurs.
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Notified when a long press occurs.
            final String text = buffer.toString();

            if (linkClickListener != null) {
                Lo.g("Long Click Occurs on TextView with ID: " + textView.getId() +
                        " Text: " + text);

                linkClickListener.onLongClick(text, positionInAdapter);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // Notified when tap occurs.
            final String linkText = getLinkText(textView, buffer, event);

            LinkType linkType = LinkType.NONE;

            if (Patterns.PHONE.matcher(linkText).matches()) {
                linkType = LinkType.PHONE;
            } else if (Patterns.WEB_URL.matcher(linkText).matches()) {
                linkType = LinkType.WEB_URL;
            } else if (Patterns.EMAIL_ADDRESS.matcher(linkText).matches()) {
                linkType = LinkType.EMAIL_ADDRESS;
            }

            if (linkClickListener != null) {
                Lo.g("Tap Occurs on TextView with ID: " + textView.getId() +
                        " Link Text: " + linkText +
                        " Link Type: " + linkType +
                        " Position: " + positionInAdapter);

                linkClickListener.onLinkClicked(linkText, linkType, positionInAdapter);
            }

            return false;
        }

        private String getLinkText(final TextView widget, final Spannable buffer, final MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                return buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString();
            }

            return buffer.toString();
        }
    }

    public enum LinkType {

        /**
         * Indicates that phone link was clicked
         */
        PHONE,

        /**
         * Identifies that URL was clicked
         */
        WEB_URL,

        /**
         * Identifies that Email Address was clicked
         */
        EMAIL_ADDRESS,

        /**
         * Indicates that none of above mentioned were clicked
         */
        NONE
    }
}