package com.quickblox.chat_v2.utils;

import com.quickblox.chat_v2.core.ChatApplication;
import com.quickblox.module.custom.model.QBCustomObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andrey on 13.06.13.
 */
public class OfflineMessageSeparatorQuery {

    private ChatApplication app;
    private ArrayList<QBCustomObject> mQueryList;
    private Timer mMessageBufferTimer;

    public OfflineMessageSeparatorQuery() {
        app = ChatApplication.getInstance();
        mQueryList = new ArrayList<QBCustomObject>();
        startOfflineMessageBuffer();
    }

    public void addNewQueryElement(int opponentId, String messageBody, int authorId) {

        QBCustomObject custobj = new QBCustomObject();

        custobj.setClassName(GlobalConsts.MESSAGES);

        HashMap<String, Object> fields = new HashMap<String, Object>();

        fields.put(GlobalConsts.AUTHOR_ID, authorId);
        fields.put(GlobalConsts.OPPONENT_ID, opponentId);
        fields.put(GlobalConsts.MESSAGE, messageBody);

        custobj.setFields(fields);

        mQueryList.add(custobj);

    }


    private void startOfflineMessageBuffer() {

        mMessageBufferTimer = new Timer();
        mMessageBufferTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mQueryList.isEmpty()) {

                    for (QBCustomObject co : mQueryList) {
                        app.getQbm().createSingleCustomObject(co);
                    }
                }

                clearQueryList();

            }

            ;
        }, 2000, 2000);
    }

    private void clearQueryList() {
        mQueryList.clear();
        mQueryList.trimToSize();
    }
}
