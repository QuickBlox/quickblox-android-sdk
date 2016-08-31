package com.quickblox.sample.core.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.sample.core.R;


public class CompoundChatTextView extends RelativeLayout {
    public CompoundChatTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        int[] attrss = { R.attr.msgTextColor, android.R.attr.background, android.R.attr.textSize, R.attr.timeTextColor};
        TypedArray ta = context.obtainStyledAttributes(R.style.CompoundChatTextViewStyle, attrss);

// Fetch the text from your style like this.
        float size = ta.getDimension(2, 20);

// Fetching the colors defined in your style
        int textColor = ta.getColor(0, Color.BLACK);
        int backgroundColor = ta.getColor(1, Color.BLACK);

        int timeTextColor = ta.getColor(3, Color.BLACK);

// Do some logging to see if we have retrieved correct values
        Log.i("Retrieve size:", Float.toString(size));
        Log.i("Retrieve txtClr as hex:", Integer.toHexString(textColor));
        Log.i("Retrieve bckgr as hex:", Integer.toHexString(backgroundColor));

// OH, and don't forget to recycle the TypedArray
        ta.recycle();



//        TypedArray a = context.obtainStyledAttributes(attrs,
//                R.styleable.CompoundChatTextView, 0, 0);
//        String titleText = a.getString(R.styleable.CompoundChatTextView_titleText);
//        boolean outcomming = a.getBoolean(R.styleable.CompoundChatTextView_outcoming, false);
//        Drawable bubble = a.getDrawable(R.styleable.CompoundChatTextView_bubble);
//        a.recycle();

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.item_text_message_compound, this, true);

        TextView textMsg = (TextView) getRootView().findViewById(R.id.message_textview);
        textMsg.setTextSize(size);
        textMsg.setTextColor(textColor);

        TextView textTimeMsg = (TextView) getRootView().findViewById(R.id.time_text_message_textview);
        textTimeMsg.setTextColor(timeTextColor);
    }
}