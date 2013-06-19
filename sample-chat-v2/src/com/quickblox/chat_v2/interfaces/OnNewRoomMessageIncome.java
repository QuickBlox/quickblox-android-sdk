package com.quickblox.chat_v2.interfaces;

import java.util.ArrayList;

/**
 * Created by andrey on 19.06.13.
 */
public interface OnNewRoomMessageIncome {

    public void incomeMessagePool(ArrayList<String> messagesPool);
}
