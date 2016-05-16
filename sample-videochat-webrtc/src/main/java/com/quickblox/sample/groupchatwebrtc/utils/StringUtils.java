package com.quickblox.sample.groupchatwebrtc.utils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 12.05.16.
 */
public class StringUtils {

    public static String makeStringFromUsersFullNames(ArrayList<QBUser> allUsers, List<Integer> selectedUsers) {
        StringifyArrayList<String> usersNames = new StringifyArrayList<>();

        for (Integer i : selectedUsers) {

            for (QBUser usr : allUsers) {
                String userName;

                if (usr.getId().equals(i)) {
                    userName = usr.getFullName();
                } else {
                    userName = String.valueOf(i);
                }

                usersNames.add(userName);
            }
        }

        return usersNames.getItemsAsString();
    }
}
