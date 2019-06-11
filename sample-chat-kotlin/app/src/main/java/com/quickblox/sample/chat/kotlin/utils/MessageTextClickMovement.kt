package com.quickblox.sample.chat.kotlin.utils

import android.content.Context
import android.text.Spannable
import android.text.method.ArrowKeyMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.TextView
import com.quickblox.core.helper.Lo
import com.quickblox.sample.chat.kotlin.ui.adapter.listeners.MessageLinkClickListener


class MessageTextClickMovement(listener: MessageLinkClickListener,
                               overrideOnClick: Boolean,
                               context: Context) : ArrowKeyMovementMethod() {

    private lateinit var textView: TextView
    private lateinit var buffer: Spannable
    private var positionInAdapter = -1

    private val overrideOnLinkClick: Boolean = overrideOnClick
    private val gestureDetector: GestureDetector = GestureDetector(context, SimpleOnGestureListener(listener))

    override fun onTouchEvent(widget: TextView, buffer: Spannable, event: MotionEvent): Boolean {
        this.textView = widget
        this.buffer = buffer
        gestureDetector.onTouchEvent(event)

        //Return super method for delegate logic sending intent from Linkify
        //or return 'false' for yourself managing logic by link clicked
        return if (overrideOnLinkClick) {
            false
        } else {
            super.onTouchEvent(widget, buffer, event)
        }
    }

    /**
     * Detects various gestures and events.
     * Notify userList when a particular motion event has occurred.
     */
    inner class SimpleOnGestureListener(private val linkClickListener: MessageLinkClickListener?) : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(event: MotionEvent): Boolean {
            // Notified when a tap occurs.
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            // Notified when a long press occurs.
            linkClickListener?.let {
                val text = buffer.toString()
                Lo.g("Long Click Occurs on TextView with ID: " + textView.id + " Text: " + text)
                linkClickListener.onLongClick(text, positionInAdapter)
            }
        }

        override fun onSingleTapConfirmed(event: MotionEvent): Boolean {
            // Notified when tap occurs.
            val linkText = getLinkText(textView, buffer, event)

            var linkType = QBLinkType.NONE

            when {
                Patterns.PHONE.matcher(linkText).matches() -> linkType = QBLinkType.PHONE
                Patterns.WEB_URL.matcher(linkText).matches() -> linkType = QBLinkType.WEB_URL
                Patterns.EMAIL_ADDRESS.matcher(linkText).matches() -> linkType = QBLinkType.EMAIL_ADDRESS
            }

            linkClickListener?.let {
                Lo.g("Tap Occurs on TextView with ID: " + textView.id +
                        " Link Text: " + linkText +
                        " Link Type: " + linkType +
                        " Position: " + positionInAdapter)

                linkClickListener.onLinkClicked(linkText, linkType, positionInAdapter)
            }
            return false
        }

        private fun getLinkText(widget: TextView, buffer: Spannable, event: MotionEvent): String {
            var x = event.x.toInt()
            var y = event.y.toInt()

            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop

            x += widget.scrollX
            y += widget.scrollY

            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())

            val link = buffer.getSpans(off, off, ClickableSpan::class.java)

            return if (link.isNotEmpty()) {
                buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString()
            } else {
                buffer.toString()
            }
        }
    }

    enum class QBLinkType {

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