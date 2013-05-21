package com.quickblox.chat_v2.core;

import android.widget.CompoundButton;

/**
 * Created with IntelliJ IDEA.
 * User: andrey
 * Date: 21.05.13
 * Time: 15:43
 * To change this template use File | Settings | File Templates.
 */
public class CustomCheckBoxListener implements CompoundButton.OnCheckedChangeListener {

    private int position;

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
