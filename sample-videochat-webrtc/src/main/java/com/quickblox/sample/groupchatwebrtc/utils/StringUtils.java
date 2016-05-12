package com.quickblox.sample.groupchatwebrtc.utils;

import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created by tereha on 12.05.16.
 */
public class StringUtils {

    public static String makeStringFromUsersFullNames(ArrayList<QBUser> allUsers, ArrayList<Integer> selectedUsers){
        StringBuffer s = new StringBuffer("");

        for (Integer i : selectedUsers) {
            for (QBUser usr : allUsers) {
                if (usr.getId().equals(i)) {
                    if (selectedUsers.indexOf(i) == (selectedUsers.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }
}
