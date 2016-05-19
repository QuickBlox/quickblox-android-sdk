package com.quickblox.sample.groupchatwebrtc.utils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tereha on 12.05.16.
 */
public class StringUtils {

    public static String makeStringFromUsersFullNames(ArrayList<QBUser> allUsers) {
        StringifyArrayList<String> usersNames = new StringifyArrayList<>();

            for (QBUser usr : allUsers) {
                if (usr.getFullName() != null) {
                    usersNames.add(usr.getFullName());
                } else if (usr.getId() != null) {
                    usersNames.add(String.valueOf(usr.getId()));
                }
            }

        return usersNames.getItemsAsString();
    }
}
