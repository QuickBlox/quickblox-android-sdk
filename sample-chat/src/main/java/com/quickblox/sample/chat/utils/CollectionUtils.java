package com.quickblox.sample.chat.utils;

import com.quickblox.chat.model.QBChatDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static List<QBChatDialog> convertMapToList(Map<String, QBChatDialog> incomeMap){
        List<QBChatDialog> list = new ArrayList<>();
        for (String key : incomeMap.keySet()){
            list.add(incomeMap.get(key));
        }

        return list;
    }
}
