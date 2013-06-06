package com.quickblox.chat_v2.utils;

/**
 * Created by andrey on 06.06.13.
 */
public class SingleChatDialogTable {

    private int firstId;
    private int secondId;

    public void setCoupleDate(int firstId, int secondId) {
        this.firstId = firstId;
        this.secondId = secondId;
    }

    public boolean reviewCoupleIsExist(int firstNew, int secondNew) {
        if (firstNew == firstId && secondNew == secondId || firstNew == secondId && secondNew == firstId) {
            return true;
        } else {
            return false;
        }

    }


}
